package com.luxf.sharding.annotations;

import java.lang.annotation.*;

/**
 * @author 小66
 * @Description
 * @create 2021-01-09 17:24
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HintMasterOnly {
}
