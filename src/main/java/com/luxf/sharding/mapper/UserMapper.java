package com.luxf.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxf.sharding.bean.User;
import com.luxf.sharding.resp.UserAnswerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserAnswerDTO> getUserAnswerByUserId(Long userId);

    boolean deleteByIdCond(@Param("id") long parseLong);
}
