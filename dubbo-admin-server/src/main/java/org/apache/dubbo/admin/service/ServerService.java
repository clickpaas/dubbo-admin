package org.apache.dubbo.admin.service;

import org.apache.dubbo.admin.model.dto.ServerStatusDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author xiansuo.dai by 2023/3/15 14:48
 */
public interface ServerService {

    /**
     * 提供者服务列表 （应用粒度）
     *
     * @param type    查询类型：application、address
     * @param keyword 查询关键字
     * @return Page<ServerStatusDTO>
     */
    Page<ServerStatusDTO> getServers(String type, String keyword, Pageable pageable);


    boolean updateStatus(ServerStatusDTO serverStatus);
}
