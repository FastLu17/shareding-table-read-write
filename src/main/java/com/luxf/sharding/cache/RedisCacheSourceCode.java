package com.luxf.sharding.cache;

import com.luxf.sharding.utils.ExtendCacheHolder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

/**
 * Spring cache. 源码
 *
 * @author 小66
 * @see TransactionAwareCacheDecorator 事务类型的Cache装饰器  {@link TransactionAwareCacheDecorator#put(Object, Object)} 核心方法: put(),evict(),clear().
 * @see AbstractTransactionSupportingCacheManager {@link RedisCacheManager}实现此类
 * @see RedisCacheWriter cacheWriter. 通过{@link RedisConnection}执行原生的Redis命令、
 **/
public interface RedisCacheSourceCode {

    /**
     * redis原生的方式, 操作hash. list. set. zset 等、 自定义cacheWriter, 可以处理自定义扩展后的{@link Cacheable,CacheEvict,CachePut}
     * 实现对hash,list,set,zset等数据结构的缓存, 而不是只能操作string的数据结构、(需要大量重写, 或者利用ThreadLocal, 减少重写.--> {@link ExtendCacheHolder})
     * TODO: 主要问题 只存在{@link RedisConnection#hSet(byte[], byte[], byte[])}方法, 无法同时设置过期时间. 需要使用lua脚本的方式执行.
     *
     * 简单一点的方式, 通过AOP的方式, 直接使用{@link RedisTemplate}, 不使用原生RedisConnection, 拦截自定义的注解. 完成缓存操作、
     *
     * @param connection redis连接
     * @param key        hashKey
     * @return hashKeys (redis中map.keySet())
     */
    default Set<byte[]> hKeys(RedisConnection connection, String key) {
        RedisListCommands listCommands = connection.listCommands();
        RedisHashCommands hashCommands = connection.hashCommands();
        // RedisConnection也可以直接使用hash,list命令、
        Set<byte[]> bytes = connection.hKeys(key.getBytes());
        // 使用lua脚本connection.eval();
        return hashCommands.hKeys(key.getBytes());
    }
}
