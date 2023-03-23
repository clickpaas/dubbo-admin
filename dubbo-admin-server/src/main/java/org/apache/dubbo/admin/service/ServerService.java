package org.apache.dubbo.admin.service;

import org.apache.dubbo.admin.model.UrlDetailRequest;
import org.apache.dubbo.admin.model.dto.ServerStatusDTO;
import org.apache.dubbo.admin.model.dto.ServiceDTO;
import org.apache.dubbo.admin.model.dto.UrlDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

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


    /**
     * 禁用/启用
     *
     * @param serverStatus 服务状态信息
     * @return
     */
    boolean updateStatus(ServerStatusDTO serverStatus);


    /**
     * 查询 URL 信息
     *
     * @param request 查询请求参数
     * @return UrlDTO
     */
    UrlDTO searchUrl(UrlDetailRequest request);

}
