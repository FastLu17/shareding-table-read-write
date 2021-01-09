package com.luxf.sharding.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC配置、
 * @author 小66
 */
@Configuration
public class WebDataConvertConfig implements WebMvcConfigurer {

    /**
     * 处理雪花算法的ID传到前端数据精度丢失的问题.
     * <p>
     * 直接使用converters.add(MappingJackson2HttpMessageConverter),无法生效、
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 已经自定义枚举的反序列化器和序列化器, 无需此配置(配置后也不生效)

        // 处理自定义枚举, 在ResponseBody中, 使用的是Enum#name()返回的问题、
        // simpleModule.addSerializer(DescriptionEnum.class, ToStringSerializer.instance);

        converters.forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter messageConverter = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = messageConverter.getObjectMapper();
                objectMapper.registerModule(simpleModule);
            }
        });
    }
}