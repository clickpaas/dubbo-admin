package org.apache.dubbo.admin.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.admin.common.util.Constants;
import org.apache.dubbo.admin.common.util.SyncUtils;
import org.apache.dubbo.admin.handler.ServerStatusHandler;
import org.apache.dubbo.admin.model.UrlDetailRequest;
import org.apache.dubbo.admin.model.domain.Override;
import org.apache.dubbo.admin.model.domain.Provider;
import org.apache.dubbo.admin.model.dto.ServerStatusDTO;
import org.apache.dubbo.admin.model.dto.UrlDTO;
import org.apache.dubbo.admin.service.ProviderService;
import org.apache.dubbo.admin.service.ServerService;
import org.apache.dubbo.common.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.dubbo.admin.common.util.Constants.*;

/**
 * 提供者 服务粒度 查询实现
 *
 * @author xiansuo.dai by 2023/3/15 15:12
 */

@Component
public class ServerServiceImpl extends AbstractService implements ServerService {

    // dynamicadmin 兼容
    public static final String DYN_SERVER_KEY = "com.bizcloud.architect.dynamicadmin.service.DynaBeanService";

    @Autowired
    ProviderService providerService;

    @Autowired
    ServerStatusHandler serverStatusHandler;


    @java.lang.Override
    public Page<ServerStatusDTO> getServers(String type, String keyword, Pageable pageable) {

        // 提供者列表
        List<Provider> providers = new ArrayList<>();

        // 查询条件是否为空，获取所有 提供者信息
        if (StringUtils.isEmpty(keyword)) {
            providers = providerService.findAll();
        } else {
            // 根据 应用名称查询
            if (SERVER_QUERY_BY_APPLICATION.equals(type)) {
                providers = providerService.findByApplication(keyword);
            }
            // 根据 地址查询
            if (SERVER_QUERY_BY_IP.equals(type)) {
                providers = providerService.findByAddress(keyword);
            }
            // 根据 TAG查询
            if (SERVER_QUERY_BY_TAG.equals(type)) {
                providers = providerService.findByTag(keyword);
            }
        }

        // 没有提供者 直接返回
        if (CollectionUtils.isEmpty(providers)) {
            return Page.empty();
        }

        // 根据 application 分组数据
        Map<String, List<Provider>> applications = providers.stream().collect(Collectors.groupingBy(this::groupKey));
        // 聚合数据为 应用级别
        Set<ServerStatusDTO> servers = new HashSet<>();
        applications.forEach((app, ps) -> {

            ServerStatusDTO dto = new ServerStatusDTO();
            Provider provider = ps.get(0);
            // 应用名称
            dto.setApplication(provider.getApplication());
            // Tag
            dto.setTag(provider.getTag());
            // IP、端口
            String[] arr = provider.getAddress().split(COLON);
            dto.setIp(arr[0]);
            dto.setPort(Integer.valueOf(arr[1]));
            // 服务暴露数量
            dto.setExportServices(ps.size());
            servers.add(dto);

        });

        // 内存分页
        final int total = servers.size();
        final List<ServerStatusDTO> result =
                servers.stream()
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .collect(Collectors.toList());

        // 设置状态
        Set<String> statusConfig = serverStatusHandler.fetchConfig();
        result.forEach(server -> {
            // 包含即为禁用, true = 开启； false = 禁用
            server.setStatus(!statusConfig.contains(server.uniqueKey()));
        });

        return new PageImpl<>(result, pageable, total);
    }


    @java.lang.Override
    public boolean updateStatus(ServerStatusDTO serverStatus) {

        // 查询对应提供者信息
        List<Provider> providers = providerService.findByApplicationAndTagAndAddress(serverStatus.getApplication(),
                serverStatus.getTag(), serverStatus.getIp() + COLON + serverStatus.getPort());
        if (CollectionUtils.isEmpty(providers)) {
            return true;
        }

        // 遍历每个 Provider，按个注销，然后再注册！！！
        providers.forEach(provider -> {

            try {

                // 兼容2.6.x 版本
                Override override = new Override();
                override.setApplication(serverStatus.getApplication());
                override.setTag(serverStatus.getTag());
                override.setAddress(provider.getAddress());
                override.setService(provider.getService());
                override.setEnabled(true);
                override.setParams(Constants.DISABLED_KEY + "=true");
                URL url = override.toUrl();

                // 当前状态 = true 表示为启用状态，则需要禁用操作
                if (serverStatus.getStatus()) {
                    registry.register(url);
                } else {
                    registry.unregister(url);
                }

            } catch (Exception e) {
                logger.error("provider:" + JSON.toJSONString(provider) +
                        (serverStatus.getStatus() ? ", disabled" : ", enabled") + "error:", e);
            }
        });

        // 标识状态
        Set<String> config = serverStatusHandler.fetchConfig();
        if (serverStatus.getStatus()) {
            config.add(serverStatus.uniqueKey());
        } else {
            config.remove(serverStatus.uniqueKey());
        }
        // 更新配置
        serverStatusHandler.updateConfig(config);

        return true;
    }

    @java.lang.Override
    public UrlDTO searchUrl(UrlDetailRequest request) {
        // URL信息
        UrlDTO dto = new UrlDTO();
        // 组装过滤条件
        Map<String, String> filter = assembleFilter(request);
        // 查询数据
        Map<String, URL> urlMap = SyncUtils.filterFromCategory(getRegistryCache(), filter);
        // 理论上查询结果只有一条记录
        if (urlMap.size() == 1) {
            urlMap.forEach((key, value) -> {
                dto.setUrl(value.toFullString());
                dto.setAddress(value.getAddress());
                dto.setSide(request.getSide());
                dto.setApplication(value.getParameter(APPLICATION));
                dto.setParameters(value.getParameters());
            });
        }
        return dto;
    }


    private Map<String, String> assembleFilter(UrlDetailRequest request) {
        // 查询条件
        Map<String, String> filter = new HashMap<String, String>();
        // 应用名称
        filter.put(Constants.APPLICATION, request.getApplication());
        // 接口名称
        filter.put(SyncUtils.SERVICE_FILTER_KEY, request.getService());
        // tag
        if (!StringUtils.isEmpty(request.getTag())) {
            filter.put(Constants.APPLICATION_TAG, request.getTag());
        }
        // 分组
        if (!StringUtils.isEmpty(request.getGroup())) {
            if (DYN_SERVER_KEY.equals(request.getService())) {
                // 修改 service
                filter.put(SyncUtils.SERVICE_FILTER_KEY, request.getGroup() + PATH_SEPARATOR + request.getService());
            } else {
                filter.put(GROUP_KEY, request.getGroup());
            }
        }
        // 版本不为空，拼接
        if (!StringUtils.isEmpty(request.getVersion())) {
            filter.put(SyncUtils.SERVICE_FILTER_KEY, request.getService() + COLON + request.getVersion());
        }
        // 消费者
        if (CONSUMER_SIDE.equals(request.getSide())) {
            filter.put(Constants.CATEGORY_KEY, CONSUMERS_CATEGORY);
            filter.put(SyncUtils.ADDRESS_FILTER_KEY, request.getIp());
        } else { // 提供者
            filter.put(Constants.CATEGORY_KEY, PROVIDERS_CATEGORY);
            filter.put(SyncUtils.ADDRESS_FILTER_KEY, request.getIp() + COLON + request.getPort());
        }
        return filter;
    }

    /**
     * 组装分组键
     *
     * @param provider 提供者信息
     * @return String
     */
    private String groupKey(Provider provider) {
        return provider.getApplication() + PATH_SEPARATOR +
                provider.getAddress() + PATH_SEPARATOR +
                (StringUtils.isEmpty(provider.getTag()) ? "" : provider.getTag());
    }


}
