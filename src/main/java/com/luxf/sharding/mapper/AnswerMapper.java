package com.luxf.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxf.sharding.bean.Answer;
import com.luxf.sharding.bean.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
	
}
