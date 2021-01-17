package com.luxf.sharding.controller;

import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.annotations.HintShardingStrategy;
import com.luxf.sharding.service.AnswerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api(value = "答案管理", tags = "答案管理")
@RequestMapping("/answer")
public class AnswerController {

    @Resource
    private AnswerService answerService;

    @PostMapping("/action/delete/{id}")
    @ApiOperation("删除答案")
    @HintShardingStrategy(masterRouteOnly = true)
    public String delete(@PathVariable Long id) {
        answerService.removeById(id);
        return "success";
    }

    @GetMapping("/answers")
    @ApiOperation("获取所有答案")
    @HintMasterOnly
    public Object list() {
        return answerService.list();
    }

    /**
     * 通过自定义注解{@link HintMasterOnly}, 即可完成HintManager的强制读取主库的数据的策略、
     * <p>
     * 只有该接口强制master、 在controller层控制即可、
     *
     * @param id
     * @return
     */
    @GetMapping("/answers/master/{id}")
    @ApiOperation("从Master获取数据")
    @HintMasterOnly
    public Object getFromMaster(@PathVariable Long id) {
        return answerService.getById(id);
    }

}
