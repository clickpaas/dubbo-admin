package org.apache.dubbo.admin.schduler;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.admin.handler.ServerStatusHandler;
import org.apache.dubbo.admin.model.domain.Override;
import org.apache.dubbo.admin.model.domain.Provider;
import org.apache.dubbo.admin.model.dto.ServerStatusDTO;
import org.apache.dubbo.admin.service.OverrideService;
import org.apache.dubbo.admin.service.ProviderService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.apache.dubbo.admin.common.util.Constants.COLON;
import static org.apache.dubbo.admin.common.util.Constants.SP_COLON;

/**
 * 确认服务部署实例的唯一性的 四元组(application、ip、port、tag)
 * <p>
 * 作用：清理由于 服务重启，导致 ip、port、tag 三者变化后，而在重启之前禁用操作产生的 脏数据。
 * <p>
 * 两种情况：
 * <p>
 * 1、宿主机部署，重新启动四元组信息也不会发生变化（除非需要修改），所以之前操作的禁用功能在 zookeeper 中所产生的数据 在服务重启之后还是存在作用，不需要清理。
 * <p>
 * 2. k8s集群部署，pod经常会由于资源不足，被 cGroups 触发 SING-KILL 杀掉，重启调度拉起，此时四元组信息一般会发生变化。
 * 如果在没有重启之前，操作了禁用功能，则分别会在以下两个目录写入数据。由于重启之后四元组信息发生变化，则之前的数据变为了脏数据，所以需要清理掉。
 * a、/dubbo/service name/configurators/
 * b、/dbbo/config/status/servers
 *
 * @author xiansuo.dai by 2023/3/17 17:56
 */

@Component
public class ServerStatusClear {

    private static final Logger logger = LoggerFactory.getLogger(ServerStatusClear.class);

    // 单位 s
    public static final long SERVER_RESTART_CHECK = 60 * 15;

    @Autowired
    private Registry registry;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private OverrideService overrideService;

    @Autowired
    private ServerStatusHandler serverStatusHandler;

    @Scheduled(cron = "0 * * * * ?")
    public void clear() {

        // 查询所有禁用服务
        Set<String> serverKeys = serverStatusHandler.fetchConfigWrapDate();
        if (CollectionUtils.isEmpty(serverKeys)) {
            return;
        }

        // 当前时间
        long currentTimeMillis = System.currentTimeMillis();
        // 记录key大小
        int keySize = serverKeys.size();

        // 遍历清理重启导致四元组信息改变 导致出现的脏数据！
        Iterator<String> iterator = serverKeys.iterator();
        while (iterator.hasNext()) {
            // 唯一KEY
            String serverKey = iterator.next();
            // 转换服务信息
            ServerStatusDTO server = ServerStatusDTO.uniqueWarpDateKeyToDTO(serverKey);
            // 操作禁用时间
            long operationDate = fetchDateInfo(serverKey);
            // 禁用操作截止到当前的时间差: 秒数
            long secondDiff = (currentTimeMillis - operationDate) / 1000;

            // 判断 是否超过阈值
            if (secondDiff > SERVER_RESTART_CHECK) {

                // 查询对应提供者信息
                List<Provider> providers = providerService.findByApplicationAndTagAndAddress(server.getApplication(),
                        server.getTag(), server.getIp() + COLON + server.getPort());

                // 提供者为空，说明当前 server 实例已经被重启！！
                if (CollectionUtils.isEmpty(providers)) {

                    // 查询是否存在 禁用配置
                    List<Override> overrides = overrideService.findByApplicationAndAddressAndTag(server.getApplication(),
                            server.getAddress(), server.getTag());

                    // 存在，即为脏数据，注销
                    if (!CollectionUtils.isEmpty(overrides)) {
                        // 遍历注销，清除脏数据
                        overrides.forEach(override -> {
                            try {
                                URL url = override.toUrl();
                                registry.unregister(url);
                                logger.info("server status clear, override:" + JSON.toJSONString(override) + "unregister success.");
                            } catch (Exception e) {
                                logger.error("server status clear, override:" + JSON.toJSONString(override) + "unregister error:", e);
                            }
                        });

                        // 删除元素
                        iterator.remove();
                    }
                }
            }
        }

        // 更新服务操作状态
        if (keySize > serverKeys.size()){
            serverStatusHandler.updateConfig(serverKeys);
        }
    }


    private long fetchDateInfo(String key) {
        if (StringUtils.isNotEmpty(key)) {
            return Long.parseLong(key.split(SP_COLON)[1]);
        }
        return System.currentTimeMillis();
    }

}
