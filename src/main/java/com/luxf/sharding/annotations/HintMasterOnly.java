package com.luxf.sharding.annotations;

import java.lang.annotation.*;

/**
 * 使用更全面的注解{@link HintShardingStrategy}代替.
 *
 * @author 小66
 * @Description
 * @create 2021-01-09 17:24
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Deprecated
public @interface HintMasterOnly {
}
