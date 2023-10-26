package org.aggregateframework.dashboard.controller;


import org.aggregateframework.dashboard.config.DashboardProperties;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.model.LoginDto;
import org.aggregateframework.dashboard.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author huabao.fang
 * @Date 2022/6/6 11:22
 **/
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardProperties dashboardProperties;

    @PostMapping("/login")
    @ResponseBody
    public ResponseDto<Map<String, Object>> login(@RequestBody LoginDto request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        Map<String, Object> loginedResult = new HashMap<>();
        loginedResult.put("token", token);
        loginedResult.put("username", request.getUsername());
        loginedResult.put("connectionMode", dashboardProperties.getConnectionMode().name());
        return ResponseDto.returnSuccess(loginedResult);
    }
}
