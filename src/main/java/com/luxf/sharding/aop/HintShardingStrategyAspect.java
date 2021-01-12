package com.luxf.sharding.aop;

import com.luxf.sharding.annotations.HintDatabaseOnly;
import com.luxf.sharding.annotations.HintDatabaseStrategy;
import com.luxf.sharding.annotations.HintShardingStrategy;
import com.luxf.sharding.annotations.HintTableStrategy;
import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * TODO: 可以单独兼容{@link HintShardingStrategy}内部的每一个注解{@link HintDatabaseOnly,HintDatabaseStrategy,HintTableStrategy}、
 * <p>
 * 注意：同一线程内, HintManager只能被创建一次.
 *
 * @author 小66
 * @Description
 * @create 2021-01-09 17:25
 * @see HintManager#getInstance()
 **/
@Aspect
@Component
public class HintShardingStrategyAspect {

    @Pointcut(value = "@annotation(com.luxf.sharding.annotations.HintShardingStrategy)")
    public void pointCut() {
    }

    /**
     * 可以直接获取注解参数、不用通过反射获取.
     * '@annotation(hintStrategy)'中的值, 需要和doAround()方法中的注解参数名称相同(必须相同,但是名称任意).
     */
    @Around(value = "pointCut() && @annotation(hintStrategy)")
    public Object doAround(ProceedingJoinPoint pjp, HintShardingStrategy hintStrategy) throws Throwable {
        try (HintManager instance = HintManager.getInstance()) {
            boolean masterRouteOnly = hintStrategy.masterRouteOnly();
            if (masterRouteOnly) {
                instance.setMasterRouteOnly();
            }
            HintDatabaseOnly databaseOnly = hintStrategy.databaseShardingOnly();
            if (databaseOnly.databaseShardingOnly() && databaseOnly.value() >= 0) {
                instance.setDatabaseShardingValue(databaseOnly.value());
            } else {
                HintTableStrategy[] tableValues = hintStrategy.tableShardingValues();
                for (HintTableStrategy tableValue : tableValues) {
                    instance.addTableShardingValue(tableValue.logicTable(), tableValue.value() % tableValue.divisor());
                }
                HintDatabaseStrategy[] databaseValues = hintStrategy.databaseShardingValues();
                for (HintDatabaseStrategy databaseValue : databaseValues) {
                    instance.addDatabaseShardingValue(databaseValue.logicTable(), databaseValue.value() % databaseValue.value());
                }
            }
            return pjp.proceed();
        }
    }
}
