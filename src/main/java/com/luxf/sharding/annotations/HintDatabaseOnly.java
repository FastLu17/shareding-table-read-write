package com.luxf.sharding.annotations;

import java.lang.annotation.*;

/**
 * Set sharding value for database sharding only.
 * {@link org.apache.shardingsphere.api.hint.HintManager#setDatabaseShardingValue(Comparable)}
 *
 * @author 小66
 * @create 2021-01-09 17:24
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HintDatabaseOnly {
    /**
     * sharding value for database sharding only.(只分库)
     */
    boolean databaseShardingOnly() default false;

    /**
     * sharding value. (只分库的情况下才赋值. value >= 0)
     */
    long value() default -1;
}
