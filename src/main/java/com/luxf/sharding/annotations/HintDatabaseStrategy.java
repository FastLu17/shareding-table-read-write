package com.luxf.sharding.annotations;

import java.lang.annotation.*;

/**
 * Add sharding value for table.
 * {@link org.apache.shardingsphere.api.hint.HintManager#addDatabaseShardingValue(String, Comparable)}
 *
 * @author 小66
 * @create 2021-01-09 17:24
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HintDatabaseStrategy {
    /**
     * logic table name
     */
    String logicTable();

    /**
     * sharding value. value >= 0. (需要类似Spring Cache一样, 集成EL表达式动态赋值.)
     * 使用SpEL解析、
     */
    String spelValue();

    /**
     * 进行 mod operation 的除数、取余运算. value > 0
     */
    int divisor();
}
