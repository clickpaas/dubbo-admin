package org.apache.dubbo.admin.controller;

import org.apache.dubbo.admin.handler.ProviderOfflineHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xiansuo.dai by 2022/10/18 17:32
 */

@RestController
@RequestMapping("/api/{env}/offline")
public class OfflineController {


    @Autowired
    private ProviderOfflineHandler providerOfflineHandler;


    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public String provider(@RequestParam String node, @RequestParam String podName, @RequestParam Long timestamp, @RequestParam String username, @RequestParam String password, @PathVariable String env) {
        return providerOfflineHandler.podEventTriggerDestroy(node, podName, timestamp, username, password);
    }

}
