package com.luxf.sharding.controller;

import cn.hutool.core.lang.Snowflake;
import com.luxf.sharding.po.User;
import com.luxf.sharding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(value = "用户管理", tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private Snowflake idWorder;

    @GetMapping("/users")
    @ApiOperation("获取所有用户")
    public Object list() {
        return userService.list();
    }

    @PostMapping("/multi")
    @ApiOperation("添加10个用户")
    public Object add() {
        long id = idWorder.nextId();
        for (long i = 0; i < 10; i++) {
            User user = new User();
            user.setId(id + i);
            user.setCity("深圳");
            user.setName("李四");
            try {
                userService.add(user);
            } catch (Exception e) {
                String format = String.format("error. id//2 = %d, errorMessage = {%s}", (user.getId() % 2), e.getMessage());
                System.out.println(format);
                throw e;
            }
        }
        return "success";
    }

    @GetMapping("/users/master/{id}")
    @ApiOperation("从Master获取数据")
    public Object getFromMaster(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/users/slave/{id}")
    @ApiOperation("从Slave获取数据")
    public Object getFromSlave(@PathVariable Long id) {
        return userService.findById(id);
    }

}
