package com.luxf.sharding.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.cache.ExtendCacheable;
import com.luxf.sharding.mapper.UserMapper;
import com.luxf.sharding.resp.UserAnswerDTO;
import org.springframework.stereotype.Service;

import java.io.Serializable;
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
    public User getById(Serializable id) {
        // HintManager instance = HintManager.getInstance();
        // instance.setMasterRouteOnly();
        User user = super.getById(id);
        // instance.close();
        return user;
    }

    @Override
    // @Cacheable(value = "User", key = "#userId") --> 只能操作 redis string、
    @ExtendCacheable(value = "User", key = "#userId", hashKey = "'answer'", duration = 10 * 60)
    public List<UserAnswerDTO> getUserAnswerByUserId(Long userId) {
        return baseMapper.getUserAnswerByUserId(userId);
    }
}
