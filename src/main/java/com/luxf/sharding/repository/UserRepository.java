package com.luxf.sharding.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxf.sharding.po.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface UserRepository extends BaseMapper<User> {
	
	Long addUser(User user);
	
	List<User> list();
	
	User findById(Long id);
	
	User findByName(String name);
}
