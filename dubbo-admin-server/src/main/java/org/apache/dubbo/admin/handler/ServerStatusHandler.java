package org.apache.dubbo.admin.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.admin.registry.config.GovernanceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

        // 查询配置
        String config = dynamicConfiguration.getConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY);
        if (StringUtils.isNotEmpty(config)) {
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
        String oldConfig = dynamicConfiguration.getConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY);
        if (StringUtils.isNotEmpty(oldConfig)){
            dynamicConfiguration.deleteConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY);
        }

        // 再添加
        if (!CollectionUtils.isEmpty(keys)) {
            // 逗号分割
            String config = String.join(",", keys);
            dynamicConfiguration.setConfig(DUBBO_CONFIG_STATUS, SERVER_STATUS_KEY, config);
        }
    }


}
