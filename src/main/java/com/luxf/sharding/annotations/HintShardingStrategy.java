package com.luxf.sharding.annotations;

import org.apache.shardingsphere.api.hint.HintManager;

import java.lang.annotation.*;

/**
 * 使用HINT分片, 简化代码入侵、
 *
 * @author 小66
 * @Description
 * @create 2021-01-09 17:24
 * @see HintManager
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HintShardingStrategy {
    /**
     * 是否强制路由到主库. force route to master database
     */
    boolean masterRouteOnly();

    /**
     * 是否只分库、默认false、
     * {@link HintManager#setDatabaseShardingValue(Comparable)}
     */
    HintDatabaseOnly databaseShardingOnly() default @HintDatabaseOnly;

    /**
     * Add sharding value for table. （添加分表策略.）
     * {@link HintManager#addTableShardingValue(String, Comparable)}
     */
    HintTableStrategy[] tableShardingValues() default {};

    /**
     * Add sharding value for database. （添加分库策略.）
     * {@link HintManager#addDatabaseShardingValue(String, Comparable)}
     */
    HintDatabaseStrategy[] databaseShardingValues() default {};
}
