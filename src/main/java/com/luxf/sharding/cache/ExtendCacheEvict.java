package com.luxf.sharding.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.connection.DataType;

import java.lang.annotation.*;

/**
 * 扩展{@link CacheEvict} 注解、
 * <p>
 * 必须要有{@link CacheEvict}存在此注解上, 否则无法被扫描到、即无法被{@link org.springframework.cache.interceptor.CacheInterceptor}拦截.
 *
 * @author 小66
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@CacheEvict
public @interface ExtendCacheEvict {

    /**
     * SpEL表达式.
     * <p>
     * redis hash -> field.
     * if {@link DataType#HASH} need assign a valid value. not allowed remove all values of this {@link #key()}.
     */
    String hashKey() default "";

    /**
     * redis 数据类型.
     */
    DataType dataType() default DataType.HASH;

    /**
     * Alias for {@link CacheEvict#value}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String[] value() default {};

    /**
     * Alias for {@link CacheEvict#cacheNames}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String[] cacheNames() default {};

    /**
     * Alias for {@link CacheEvict#key}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String key() default "";

    /**
     * Alias for {@link CacheEvict#keyGenerator}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String keyGenerator() default "";

    /**
     * Alias for {@link CacheEvict#cacheManager}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String cacheManager() default "";

    /**
     * Alias for {@link CacheEvict#cacheResolver}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String cacheResolver() default "";

    /**
     * Alias for {@link CacheEvict#condition}.
     */
    @AliasFor(annotation = CacheEvict.class)
    String condition() default "";


    /**
     * Alias for {@link CacheEvict#allEntries}.
     */
    @AliasFor(annotation = CacheEvict.class)
    boolean allEntries() default false;

    /**
     * Alias for {@link CacheEvict#beforeInvocation}.
     */
    @AliasFor(annotation = CacheEvict.class)
    boolean beforeInvocation() default false;
}
