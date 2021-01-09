package com.luxf.sharding.aop;

import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author 小66
 * @Description
 * @create 2021-01-09 17:25
 **/
@Aspect
@Component
public class HintMasterAspect {

    @Pointcut(value = "@annotation(com.luxf.sharding.annotations.HintMasterOnly)")
    public void pointCut() {

    }

    @Around(value = "pointCut()")
    public Object doAround(ProceedingJoinPoint pjp) {
        // try with resource 会自动执行close()方法、
        try (HintManager instance = HintManager.getInstance()) {
            instance.setMasterRouteOnly();
            return pjp.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
