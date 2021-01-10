package com.luxf.sharding.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxf.sharding.annotations.HintMasterOnly;
import com.luxf.sharding.bean.Answer;
import com.luxf.sharding.mapper.AnswerMapper;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {
}
