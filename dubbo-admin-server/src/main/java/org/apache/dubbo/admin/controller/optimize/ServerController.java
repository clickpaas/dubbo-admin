package org.apache.dubbo.admin.controller.optimize;

import org.apache.dubbo.admin.annotation.Authority;
import org.apache.dubbo.admin.model.dto.ServerStatusDTO;
import org.apache.dubbo.admin.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiansuo.dai by 2023/3/15 09:36
 */

@Authority(needLogin = true)
@RestController
@RequestMapping("/api/{env}/optimize")
public class ServerController {

    private final ServerService serverService;

    @Autowired
    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }


    @RequestMapping(value = "/servers", method = RequestMethod.GET)
    public Page<ServerStatusDTO> searchService(@RequestParam String type,
                                               @RequestParam String keyword,
                                               Pageable pageable, @PathVariable String env) {
        return serverService.getServers(type, keyword, pageable);
    }


    @RequestMapping(value = "/server/status", method = RequestMethod.PUT)
    public boolean status(@RequestBody ServerStatusDTO dto, @PathVariable String env) {
        return serverService.updateStatus(dto);
    }


}
