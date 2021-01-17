package com.luxf.sharding.controller;

import cn.hutool.core.lang.Snowflake;
import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.annotations.HintShardingStrategy;
import com.luxf.sharding.annotations.HintTableStrategy;
import com.luxf.sharding.bean.Answer;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.resp.UserAnswerDTO;
import com.luxf.sharding.service.AnswerService;
import com.luxf.sharding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(value = "用户管理", tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private AnswerService answerService;

    @Resource
    private Snowflake idWorker;

    @GetMapping("/get/{id}")
    @ApiOperation("根据ID查询用户")
    @HintShardingStrategy(masterRouteOnly = true)
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * TODO: 新增 使用了HintStrategy, 但是没有指定分表参数、 就会每个真实表都插入相同的数据.
     * @return
     */
    @PostMapping("/action/save")
    @ApiOperation("新增用户")
    public User save() {
        User user = new User();
        long userId = 123L;
        user.setId(userId);
        user.setCity("深圳");
        user.setName("李四");
        Answer answer = new Answer();
        answer.setId(idWorker.nextId());
        answer.setUserId(user.getId());
        answer.setText("123");
        answer.setResult("2");
        userService.save(user);
        answerService.save(answer);
        return user;
    }

    /**
     * 删除接口. 出现 Table 'ds_1.user' doesn't exist. (为什么是逻辑表名? 不是和新增一样, 每个表都删一次.)
     * @param id
     * @return
     */
    @PostMapping("/action/delete/{id}")
    @ApiOperation("删除用户")
    @HintShardingStrategy(masterRouteOnly = true)
    public String delete(@PathVariable Long id) {
        userService.removeById(id);
        return "success";
    }

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
        ArrayList<Answer> answers = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            User user = new User();
            user.setId(idWorker.nextId());
            user.setCity("深圳");
            user.setName("李四");
            users.add(user);
            Answer answer = new Answer();
            answer.setId(idWorker.nextId());
            answer.setUserId(user.getId());
            answer.setText("123");
            answer.setResult(String.valueOf(i % 2));
            answers.add(answer);
        }
        userService.saveBatch(users);
        answerService.saveBatch(answers);
        return "success";
    }

    /**
     * 在没有分表的情况下, 该方法对应的SQL查询的结果只有一条数据、 分表之后有4条.
     * TODO: 这种情况需要如何处理？  理论上 -> 绑定表可以处理、(绑定表之间的分片键/分区键要完全相同)
     * <p>
     * 通过hint分片的策略是否可以解决呢？
     * <p>
     * TODO: 自定义注解灵活使用{@link org.apache.shardingsphere.api.hint.HintManager}, 需要对{@link HintTableStrategy#spelValue()}使用SpEL表达式进行动态赋值、
     *
     * @param id
     * @return
     */
    @GetMapping("/users/answer/{id}")
    @ApiOperation("多表关联查询user-answer")
    @HintShardingStrategy(masterRouteOnly = true, tableShardingValues = {
            @HintTableStrategy(logicTable = "user", spelValue = "#id", divisor = 4),
            @HintTableStrategy(logicTable = "answer", spelValue = "#id", divisor = 4)
    })
    public List<UserAnswerDTO> getUserAnswerByUserId(@PathVariable Long id) {
        List<UserAnswerDTO> dtoList = userService.getUserAnswerByUserId(id);
        System.out.println("dtoList = " + dtoList);
        return dtoList;
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
