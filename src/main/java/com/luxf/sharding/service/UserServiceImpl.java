package com.luxf.sharding.service;

import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.po.User;
import com.luxf.sharding.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public List<User> list() {
        return userRepository.list();
    }

    @Override
    public Long add(User user) {
        return userRepository.addUser(user);
    }

    /**
     * 通过自定义注解{@link HintMasterOnly}, 即可完成HintManager的强制读取主库的数据的策略、
     *
     * @param id
     * @return
     */
    @Override
    @HintMasterOnly
    public User findById(Long id) {
        // HintManager instance = HintManager.getInstance();
        // instance.setMasterRouteOnly();
        User user = userRepository.findById(id);
        // instance.close();
        return user;
    }

    @Override
    public User findByName(String name) {
        return userRepository.findByName(name);
    }

}
