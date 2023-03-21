package org.apache.dubbo.admin.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.admin.model.domain.Provider;
import org.apache.dubbo.admin.service.ProviderService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * @author xiansuo.dai by 2022/10/9 17:18
 */

@Component
public class ProviderOfflineHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProviderOfflineHandler.class);

    private static final String USER = "admin";
    private static final String PASSWORD = "dubbo.clickpaas.tech";

    public static final Integer FAIL = 500;
    public static final Integer SUCCESS = 200;

    @Autowired
    private Registry registry;

    @Autowired
    private ProviderService providerService;


    public Integer podEventTriggerDestroy(String node, String podName, Long timestamp, String username, String password) {

        // 简单认证 offline specify provider node service
        if (!StringUtils.equals(USER, username) || !StringUtils.equals(PASSWORD, password)) {
            logger.warn("offline provider auth fail, node: " + node + ", username: " + username + ", password: " + password);
            return FAIL;
        }

        try {

            // timestamp is used for network delay verification
            long current = System.currentTimeMillis();
            // delay second
            long second = Math.abs((current - timestamp) / 1000);
            if (second > 3) {

                logger.warn("network delay, current: " + current + ", send-time: " + timestamp + ", second: " + second);

                // check node
                String[] arr = node.split(":");
                if (arr.length != 2) {
                    logger.warn("node: " + node + ", invalid param.");
                    return FAIL;
                }

                // ip、port
                String ip = arr[0];
                int port = StringUtils.isEmpty(arr[1]) ? 20880 : Integer.parseInt(arr[1]);

                // check connect
                if (isHostConnectable(ip, port)) {
                    return SUCCESS;
                }

            }

            // 根据 IP地址 查询服务提供者列表
            List<Provider> providers = providerService.findByAddress(node);
            logger.info("offline provider auth success, node: " + node + ", podName: " + podName + ", username: " + username + ", password: " + password + ", network delay second: " + second + ", providers: " + providers.size());

            // 不为空，删除注册中心中对应的提供者服务信息
            if (!CollectionUtils.isEmpty(providers)) {
                // 遍历删除 提供者
                providers.forEach(provider -> {
                    // 兼容处理低版本dubbo 注册的url
                    URL url = compatibleLowVersion(provider.toUrl());
                    registry.unregister(url);
                    logger.info("oomkill event remove success, provider url: " + url.toFullString());
                });
            }

        } catch (Exception e) {
            logger.error("provider offline error: ", e);
        }

        return SUCCESS;
    }


    /**
     * 兼容低版本dubbo参数问题
     * <p>
     * dubbo 2.5.6 版本想要执行 线程池的调度策略，可用的指定方式为：-Ddubbo.provider.dispatcher=message
     * 然后这种方式注册到 zookeeper 没有问题，可以生效。
     * <p>
     * 问题在于，当前高版本 dubbo-admin，（实测）订阅从zookeeper中读取数据时，从字符串反向解析成url时，会变成两个参数 default.dispatcher
     * 与 dispatcher 。然而，registry.unregister(url) 底层是 url 全路径匹配，所以会造成删除不了 zookeeper 当中的数据。
     * <p>
     * 所以在此 做兼容处理。
     * <p>
     * * @param url url
     */
    private URL compatibleLowVersion(URL url) {
        if (url.getParameter("default.dispatcher") != null && url.getParameter("dispatcher") != null) {
            return url.removeParameter("dispatcher");
        }
        return url;
    }


    /**
     * 端口连接检测
     *
     * @param host 主机
     * @param port 端口
     * @return boolean
     */
    public boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
