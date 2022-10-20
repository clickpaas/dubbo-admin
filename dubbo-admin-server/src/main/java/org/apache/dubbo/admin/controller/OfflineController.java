package org.apache.dubbo.admin.controller;

import org.apache.dubbo.admin.handler.ProviderOfflineHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiansuo.dai by 2022/10/18 17:32
 */

@RestController
@RequestMapping("/api/{env}/offline")
public class OfflineController {


    @Autowired
    private ProviderOfflineHandler providerOfflineHandler;


    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public String provider(@RequestParam String node, @RequestParam String podName, @RequestParam String username, @RequestParam String password) {
        return providerOfflineHandler.podEventTriggerDestroy(node, podName, username, password);
    }

}
