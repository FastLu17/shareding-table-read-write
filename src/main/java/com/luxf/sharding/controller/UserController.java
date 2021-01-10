package com.luxf.sharding.controller;

import cn.hutool.core.lang.Snowflake;
import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;

@RestController
@Api(value = "用户管理", tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private Snowflake idWorder;

    @GetMapping("/users")
    @ApiOperation("获取所有用户")
    @HintMasterOnly
    public Object list() {
        return userService.list();
    }

    @PostMapping("/multi")
    @ApiOperation("添加10个用户")
    public Object add() {
        ArrayList<User> users = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            User user = new User();
            user.setId(idWorder.nextId());
            user.setCity("深圳");
            user.setName("李四");
            users.add(user);
        }
        userService.saveBatch(users);
        return "success";
    }

    /**
     * 通过自定义注解{@link HintMasterOnly}, 即可完成HintManager的强制读取主库的数据的策略、
     * <p>
     * 只有该接口强制master、 在controller层控制即可、
     *
     * @param id
     * @return
     */
    @GetMapping("/users/master/{id}")
    @ApiOperation("从Master获取数据")
    @HintMasterOnly
    public Object getFromMaster(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * TODO: 没有被HintMasterOnly拦截, 默认查询slave、
     *
     * @param id
     * @return
     */
    @GetMapping("/users/slave/{id}")
    @ApiOperation("从Slave获取数据")
    public Object getFromSlave(@PathVariable Long id) {
        return userService.getById(id);
    }

}
