package com.luxf.sharding.cache;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.connection.DataType;

import java.lang.annotation.*;

/**
 * 扩展{@link CachePut} 注解、
 * <p>
 * 必须要有{@link CachePut}存在此注解上, 否则无法被扫描到、即无法被{@link org.springframework.cache.interceptor.CacheInterceptor}拦截.
 *
 * @author 小66
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@CachePut
public @interface ExtendCachePut {

    /**
     * expire time of redis cache.
     * <p>
     * {@link  java.util.concurrent.TimeUnit#SECONDS}
     */
    long duration() default -1;

    /**
     * SpEL表达式.
     * <p>
     * redis hash -> field.
     * if {@link DataType#HASH} need assign a valid value.
     */
    String hashKey() default "";

    /**
     * redis 数据类型.
     */
    DataType dataType() default DataType.HASH;

    /**
     * Alias for {@link CachePut#value}.
     */
    @AliasFor(annotation = CachePut.class)
    String[] value() default {};

    /**
     * Alias for {@link CachePut#cacheNames}.
     */
    @AliasFor(annotation = CachePut.class)
    String[] cacheNames() default {};

    /**
     * Alias for {@link CachePut#key}.
     */
    @AliasFor(annotation = CachePut.class)
    String key() default "";

    /**
     * Alias for {@link CachePut#keyGenerator}.
     */
    @AliasFor(annotation = CachePut.class)
    String keyGenerator() default "";

    /**
     * Alias for {@link CachePut#cacheManager}.
     */
    @AliasFor(annotation = CachePut.class)
    String cacheManager() default "";

    /**
     * Alias for {@link CachePut#cacheResolver}.
     */
    @AliasFor(annotation = CachePut.class)
    String cacheResolver() default "";

    /**
     * Alias for {@link CachePut#condition}.
     */
    @AliasFor(annotation = CachePut.class)
    String condition() default "";

    /**
     * Alias for {@link CachePut#unless}.
     */
    @AliasFor(annotation = CachePut.class)
    String unless() default "";
}
