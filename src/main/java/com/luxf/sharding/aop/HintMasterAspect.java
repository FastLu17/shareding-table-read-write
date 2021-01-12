package com.luxf.sharding.aop;

import com.luxf.sharding.annotations.HintShardingStrategy;
import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 使用更全面的注解{@link HintShardingStrategy}代替{@link com.luxf.sharding.annotations.HintMasterOnly}
 *
 * @author 小66
 * @create 2021-01-09 17:25
 * @see HintShardingStrategy
 * @see HintShardingStrategyAspect
 **/
@Aspect
@Component
@Deprecated
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
