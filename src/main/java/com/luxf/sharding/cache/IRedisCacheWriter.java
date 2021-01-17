package com.luxf.sharding.cache;

import com.luxf.sharding.utils.ExtendCacheHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * copy of org.springframework.data.redis.cache.DefaultRedisCacheWriter
 *
 * @author 小66
 **/
@SuppressWarnings("all")
@Slf4j
public class IRedisCacheWriter implements RedisCacheWriter {

    private final RedisConnectionFactory connectionFactory;
    private final Duration sleepTime;

    /**
     * add field to serialize.
     */
    private final RedisSerializer<String> hashKeySerializer = new StringRedisSerializer();

    /**
     * @param connectionFactory must not be {@literal null}.
     */
    IRedisCacheWriter(RedisConnectionFactory connectionFactory) {
        this(connectionFactory, Duration.ZERO);
    }

    /**
     * @param connectionFactory must not be {@literal null}.
     * @param sleepTime         sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
     *                          to disable locking.
     */
    IRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime) {

        Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
        Assert.notNull(sleepTime, "SleepTime must not be null!");

        this.connectionFactory = connectionFactory;
        this.sleepTime = sleepTime;
    }

    /**
     * 重写put()方法.
     *
     * @param name
     * @param key
     * @param value
     * @param ttl
     */
    @Override
    public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(value, "Value must not be null!");

        execute(name, connection -> {
            // TODO: bug -> seconds, datatype 均为 null.
            /**
             * 已处理: 先执行了get()方法, 没有命中结果就执行了{@link ExtendCacheHolder#clear()}, 导致执行put()方法时, ExtendCacheHolder中的数据全是null.
             */
            Long seconds = ExtendCacheHolder.getDuration();
            Duration finalTtl = getFinalTtl(seconds, ttl);
            DataType dataType = ExtendCacheHolder.getDataType();
            if (DataType.HASH.equals(dataType)) {
                // TODO: 使用lua脚本可以保证原子性、
                String hashKey = ExtendCacheHolder.getHashKey();
                if (isEmpty(hashKey)) {
                    return null;
                }
                // ExtendCacheHolder.clear();
                connection.hSet(key, hashKeySerializer.serialize(hashKey), value);
                if (shouldExpireWithin(finalTtl)) {
                    connection.expire(key, finalTtl.getSeconds());
                }
                return "OK";
            }

            if (shouldExpireWithin(finalTtl)) {
                connection.set(key, value, Expiration.from(finalTtl.toMillis(), TimeUnit.MILLISECONDS), RedisStringCommands.SetOption.upsert());
            } else {
                connection.set(key, value);
            }
            return "OK";
        });
    }

    /**
     * 重写get()方法.
     *
     * @param name
     * @param key
     * @return
     */
    @Override
    public byte[] get(String name, byte[] key) {

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");

        return execute(name, connection -> {
            DataType dataType = ExtendCacheHolder.getDataType();
            if (DataType.HASH.equals(dataType)) {
                String hashKey = ExtendCacheHolder.getHashKey();
                if (isEmpty(hashKey)) {
                    return null;
                }
                /**
                 * 解决BUG: 在put()方法处, 获取ExtendCacheHolder中的值全是null. --> 由于execute()方法, 先执行get(), 没有命中时, 才会执行put()、
                 * 因此: 不能在get()方法就调用{@link ExtendCacheHolder#clear()}, 需要在{@link org.aspectj.lang.annotation.Around}方法的finally代码块中释放ThreadLocal资源.
                 * @see CacheAspectSupport#execute(CacheOperationInvoker, java.lang.reflect.Method, CacheAspectSupport.CacheOperationContexts)
                 */
                // ExtendCacheHolder.clear();
                byte[] hGet = connection.hGet(key, hashKeySerializer.serialize(hashKey));
                return hGet;
            }
            return connection.get(key);
        });
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(value, "Value must not be null!");

        return execute(name, connection -> {

            if (isLockingCacheWriter()) {
                doLock(name, connection);
            }

            try {
                if (connection.setNX(key, value)) {

                    if (shouldExpireWithin(ttl)) {
                        connection.pExpire(key, ttl.toMillis());
                    }
                    return null;
                }

                return connection.get(key);
            } finally {

                if (isLockingCacheWriter()) {
                    doUnlock(name, connection);
                }
            }
        });
    }

    /**
     * 重写remove()方法.
     *
     * @param name
     * @param key
     * @return
     */
    @Override
    public void remove(String name, byte[] key) {

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");

        execute(name, connection -> {
            DataType dataType = ExtendCacheHolder.getDataType();
            if (DataType.HASH.equals(dataType)) {
                String hashKey = ExtendCacheHolder.getHashKey();
                if (isEmpty(hashKey)) {
                    return null;
                }
                // ExtendCacheHolder.clear();
                connection.hDel(key, hashKeySerializer.serialize(hashKey));
                return null;
            }
            return connection.del(key);
        });
    }

    @Override
    public void clean(String name, byte[] pattern) {

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(pattern, "Pattern must not be null!");

        execute(name, connection -> {

            boolean wasLocked = false;

            try {

                if (isLockingCacheWriter()) {
                    doLock(name, connection);
                    wasLocked = true;
                }

                byte[][] keys = Optional.ofNullable(connection.keys(pattern)).orElse(Collections.emptySet())
                        .toArray(new byte[0][]);

                if (keys.length > 0) {
                    connection.del(keys);
                }
            } finally {

                if (wasLocked && isLockingCacheWriter()) {
                    doUnlock(name, connection);
                }
            }

            return "OK";
        });
    }

    void lock(String name) {
        execute(name, connection -> doLock(name, connection));
    }

    void unlock(String name) {
        executeLockFree(connection -> doUnlock(name, connection));
    }

    private Boolean doLock(String name, RedisConnection connection) {
        return connection.setNX(createCacheLockKey(name), new byte[0]);
    }

    private Long doUnlock(String name, RedisConnection connection) {
        return connection.del(createCacheLockKey(name));
    }

    boolean doCheckLock(String name, RedisConnection connection) {
        return connection.exists(createCacheLockKey(name));
    }

    private boolean isLockingCacheWriter() {
        return !sleepTime.isZero() && !sleepTime.isNegative();
    }

    private <T> T execute(String name, Function<RedisConnection, T> callback) {

        RedisConnection connection = connectionFactory.getConnection();
        try {

            checkAndPotentiallyWaitUntilUnlocked(name, connection);
            return callback.apply(connection);
        } finally {
            connection.close();
        }
    }

    private void executeLockFree(Consumer<RedisConnection> callback) {

        RedisConnection connection = connectionFactory.getConnection();

        try {
            callback.accept(connection);
        } finally {
            connection.close();
        }
    }

    private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {

        if (!isLockingCacheWriter()) {
            return;
        }

        try {

            while (doCheckLock(name, connection)) {
                Thread.sleep(sleepTime.toMillis());
            }
        } catch (InterruptedException ex) {

            // Re-interrupt current thread, to allow other participants to react.
            Thread.currentThread().interrupt();

            throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name),
                    ex);
        }
    }

    private static boolean shouldExpireWithin(@Nullable Duration ttl) {
        return ttl != null && !ttl.isZero() && !ttl.isNegative();
    }

    private static byte[] createCacheLockKey(String name) {
        return (name + "~lock").getBytes(StandardCharsets.UTF_8);
    }

    private Duration getFinalTtl(Long seconds, @Nullable Duration ttl) {
        Duration finalTtl = ttl;
        if (seconds != null) {
            Duration duration = Duration.ofSeconds(seconds);
            boolean shouldExpire = shouldExpireWithin(duration);
            if (shouldExpire) {
                finalTtl = duration;
            }
        }
        return finalTtl;
    }

    private boolean isEmpty(String hashKey) {
        if (hashKey == null || hashKey.trim().length() == 0) {
            log.error("ExtendCacheHolder.getHashKey() is empty.");
            return true;
        }
        return false;
    }
}
