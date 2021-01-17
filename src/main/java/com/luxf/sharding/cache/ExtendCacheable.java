package com.luxf.sharding.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.connection.DataType;

import java.lang.annotation.*;

/**
 * 扩展{@link Cacheable} 注解、
 * <p>
 * 必须要有{@link Cacheable}存在此注解上, 否则无法被扫描到、即无法被{@link org.springframework.cache.interceptor.CacheInterceptor}拦截.
 * <p>
 * TODO: 类似扩展{@link CachePut,CacheEvict}
 *
 * @author 小66
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable
public @interface ExtendCacheable {

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
     * Alias for {@link Cacheable#value}.
     */
    @AliasFor(annotation = Cacheable.class)
    String[] value() default {};

    /**
     * Alias for {@link Cacheable#cacheNames}.
     */
    @AliasFor(annotation = Cacheable.class)
    String[] cacheNames() default {};

    /**
     * Alias for {@link Cacheable#key}.
     */
    @AliasFor(annotation = Cacheable.class)
    String key() default "";

    /**
     * Alias for {@link Cacheable#keyGenerator}.
     */
    @AliasFor(annotation = Cacheable.class)
    String keyGenerator() default "";

    /**
     * Alias for {@link Cacheable#cacheManager}.
     */
    @AliasFor(annotation = Cacheable.class)
    String cacheManager() default "";

    /**
     * Alias for {@link Cacheable#cacheResolver}.
     */
    @AliasFor(annotation = Cacheable.class)
    String cacheResolver() default "";

    /**
     * Alias for {@link Cacheable#condition}.
     */
    @AliasFor(annotation = Cacheable.class)
    String condition() default "";

    /**
     * Alias for {@link Cacheable#unless}.
     */
    @AliasFor(annotation = Cacheable.class)
    String unless() default "";

    /**
     * Alias for {@link Cacheable#sync}.
     */
    @AliasFor(annotation = Cacheable.class)
    boolean sync() default false;
}
