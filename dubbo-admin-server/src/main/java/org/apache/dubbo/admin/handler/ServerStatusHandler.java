package org.apache.dubbo.admin.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.admin.registry.config.GovernanceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.dubbo.admin.common.util.Constants.SP_COLON;

/**
 * @author xiansuo.dai by 2023/3/17 14:55
 */

@Component
public class ServerStatusHandler {

    // 存储服务禁用启用状态
    public static final String DUBBO_CONFIG_STATUS = "/dubbo/config/status";
    // 存储KEY
    public static final String SERVER_STATUS_KEY = "servers";

    @Autowired
    protected GovernanceConfiguration dynamicConfiguration;

    public Set<String> fetchConfig() {
        // 查询配置，key包含时间信息
        Set<String> keys = fetchConfigWrapDate();

        // 截取数据
        return keys.stream().map(key -> {
            String[] ars = key.split(SP_COLON);
            return ars[0];
        }).collect(Collectors.toSet());

    }

    public Set<String> fetchConfigWrapDate() {

        // 查询配置
        String config = dynamicConfiguration.getConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY);
        if (StringUtils.isNotEmpty(config)) {
            // 数据转换
            return Arrays.stream(config.split(",")).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }


    /**
     * 更新配置
     *
     * @param keys 服务实例 - 唯一KEY值
     */
    public void updateConfig(Set<String> keys) {

        // 先删除
        Set<String> oldKeys = fetchConfigWrapDate();
        if (!CollectionUtils.isEmpty(oldKeys)) {
            dynamicConfiguration.deleteConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY);
        }

        // 再添加
        if (!CollectionUtils.isEmpty(keys)) {

            // 遍历keys，包装时间戳
            Set<String> wrapDateKeys = wrapDate(oldKeys, keys);
            // 逗号分隔
            String config = String.join(",", wrapDateKeys);
            dynamicConfiguration.setConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY, config);
        }
    }


    private Set<String> wrapDate(Set<String> oldKeys, Set<String> newKeys) {

        // 之前不存在 所有都添加时间
        if (CollectionUtils.isEmpty(oldKeys)) {
            return newKeys.stream().map(key -> key + SP_COLON + System.currentTimeMillis())
                    .collect(Collectors.toSet());
        } else { // 兼容

            return newKeys.stream().map(newKey -> {
                // 查询之前是否存在，
                Optional<String> optional = oldKeys.stream().filter(oldKey -> oldKey.contains(newKey)).findFirst();
                return optional.orElseGet(() -> newKey + SP_COLON + System.currentTimeMillis());

            }).collect(Collectors.toSet());
        }
    }

}
