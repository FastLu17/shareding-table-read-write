package com.luxf.sharding.utils;

import org.springframework.core.NamedThreadLocal;
import org.springframework.data.redis.connection.DataType;
import org.springframework.util.Assert;

/**
 * 用于传递参数、扩展spring redis cache.
 * <p>
 * 无需使用{@link org.springframework.core.NamedInheritableThreadLocal}
 *
 * @author 小66
 **/
public class ExtendCacheHolder {
    /**
     * redis data type
     */
    private static final ThreadLocal<DataType> DATA_TYPE = new NamedThreadLocal<>("redis data type.");

    /**
     * redis hash field
     */
    private static final ThreadLocal<String> HASH_KEY = new NamedThreadLocal<>("redis hash field.");

    /**
     * expire time of redis cache
     */
    private static final ThreadLocal<Long> DURATION = new NamedThreadLocal<>("expire time of redis cache");

    public static DataType getDataType() {
        return DATA_TYPE.get();
    }

    public static void setDataType(DataType type) {
        Assert.notNull(type, "DataType must not be null.");
        DATA_TYPE.set(type);
    }

    public static String getHashKey() {
        return HASH_KEY.get();
    }

    public static void setHashKey(String hashKey) {
        Assert.isTrue(hashKey != null && hashKey.trim().length() > 0, "hashKey must not be empty.");
        HASH_KEY.set(hashKey);
    }

    public static Long getDuration() {
        return DURATION.get();
    }

    /**
     * allow negative
     * @param duration ttl
     */
    public static void setDuration(Long duration) {
        Assert.isTrue(duration != null && duration != 0, "duration must not be zero.");
        DURATION.set(duration);
    }

    public static void clear() {
        DATA_TYPE.remove();
        HASH_KEY.remove();
        DURATION.remove();
    }
}
