package com.luxf.sharding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.resp.UserAnswerDTO;

import java.util.List;

public interface UserService extends IService<User> {

    List<UserAnswerDTO> getUserAnswerByUserId(Long userId);
}
