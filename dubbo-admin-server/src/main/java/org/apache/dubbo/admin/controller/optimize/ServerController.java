package org.apache.dubbo.admin.controller.optimize;

import org.apache.dubbo.admin.annotation.Authority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiansuo.dai by 2023/3/15 09:36
 */


@Authority(needLogin = true)
@RestController
@RequestMapping("/api/{env}/optimize/app")
public class ApplicationController {






}
