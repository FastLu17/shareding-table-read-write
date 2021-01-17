package com.luxf.sharding.aop;

import com.luxf.sharding.cache.ExtendCacheable;
import com.luxf.sharding.utils.ExpressionParseUtils;
import com.luxf.sharding.utils.ExtendCacheHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.DataType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 当前AOP顺序必须需要在{@link org.springframework.cache.interceptor.CacheInterceptor}之前. 即当前AOP的order小于{@link EnableCaching#order()}
 * <p>
 * 否则ThreadLocal中获取{@link ExtendCacheHolder#getDataType()}结果是null.
 * <p>
 * TODO: 当前AOP的Order如果是{@link Ordered#HIGHEST_PRECEDENCE}, 则会抛出异常. Required to bind 2 arguments, but only bound 1 (JoinPointMatch was NOT bound in invocation)
 *
 * @author 小66
 * @see ProxyCachingConfiguration#cacheAdvisor()  --> 获取{@link EnableCaching#order()}、
 * @see ProxyCachingConfiguration#cacheInterceptor()
 * @see com.luxf.sharding.cache.IRedisCacheWriter#get(String, byte[])
 **/
@Aspect
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class ExtendCacheAspect {

    // TODO: 需要处理类似的ExtendCachePut、ExtendCacheEvict

    @Pointcut(value = "@annotation(com.luxf.sharding.cache.ExtendCacheable)")
    public void pointCut() {
    }

    @Around(value = "pointCut() && @annotation(extendCacheable)")
    public Object doAround(ProceedingJoinPoint pjp, ExtendCacheable extendCacheable) throws Throwable {
        try {
            if (DataType.HASH.equals(extendCacheable.dataType())) {
                Method method = ((MethodSignature) pjp.getSignature()).getMethod();
                String hashKey = ExpressionParseUtils.getParseValue(extendCacheable.hashKey(), method, pjp.getArgs());
                ExtendCacheHolder.setDataType(extendCacheable.dataType());
                ExtendCacheHolder.setHashKey(hashKey);
            }
            long duration = extendCacheable.duration();
            if (duration != -1) {
                ExtendCacheHolder.setDuration(duration);
            }
            return pjp.proceed();
        } finally {
            /**
             * 在finally代码块中统一释放ThreadLocal资源、
             * 解决{@link com.luxf.sharding.cache.IRedisCacheWriter#put(String, byte[], byte[], Duration)}中, 获取不到值的bug.
             */
            ExtendCacheHolder.clear();
        }
    }
}
