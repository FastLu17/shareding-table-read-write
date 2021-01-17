package com.luxf.sharding.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.cache.ExtendCacheEvict;
import com.luxf.sharding.cache.ExtendCachePut;
import com.luxf.sharding.cache.ExtendCacheable;
import com.luxf.sharding.mapper.UserMapper;
import com.luxf.sharding.resp.UserAnswerDTO;
import org.springframework.data.redis.connection.DataType;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 不应该在Service层使用{@link HintMasterOnly}, 不是所有调用该getById()地方的都需要强制走master
     *
     * @param id
     * @return
     */
    @Override
    // @HintMasterOnly
    @ExtendCacheable(value = "User", key = "#id", duration = 10 * 60, dataType = DataType.STRING)
    public User getById(Serializable id) {
        // HintManager instance = HintManager.getInstance();
        // instance.setMasterRouteOnly();
        User user = super.getById(id);
        // instance.close();
        return user;
    }

    @Override
    @ExtendCacheEvict(value = "User", key = "#id", dataType = DataType.STRING)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    /**
     * 由于方法返回值不是{@link User}, 因此无法使用{@link org.springframework.cache.annotation.CachePut}处理(the value of Redis is boolean)、
     * <p>
     * 两种方案:
     * 1、重写接口. 将实体类返回.
     * 2、继续扩展{@link ExtendCachePut}, 将实体类放入ThreadLocal(需必须保证引用地址不发生修改),
     * 执行{@link com.luxf.sharding.cache.IRedisCacheWriter#put(String, byte[], byte[], Duration)}方法时, 替换value.
     * <p>
     * 正常应该是重写接口, 因为MybatisPlus自带的CURD方法, 也无法直接使用{@link ExtendCacheable,ExtendCachePut,ExtendCacheEvict}进行拦截.
     *
     * @param entity User
     * @return boolean
     */
    @Override
    @ExtendCachePut(value = "User", key = "#entity.id", dataType = DataType.STRING)
    public boolean save(User entity) {
        return super.save(entity);
    }

    @Override
    // @Cacheable(value = "User", key = "#userId") --> 只能操作 redis string、
    @ExtendCacheable(value = "User_Hash", key = "#userId", hashKey = "'answer'", duration = 10 * 60)
    public List<UserAnswerDTO> getUserAnswerByUserId(Long userId) {
        return baseMapper.getUserAnswerByUserId(userId);
    }
}
