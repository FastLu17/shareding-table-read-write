package com.luxf.sharding.config;

import cn.hutool.core.lang.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 小66
 * @create 2020-12-18 20:36
 **/
@Configuration
public class ApplicationConfig {

    @Value("${config.worker-id:0}")
    private Long workerId;

    @Value("${config.data-center-id:0}")
    private Long dataCenterId;

    @Bean
    public Snowflake snowflake() {
        return new Snowflake(workerId, dataCenterId);
    }
}
