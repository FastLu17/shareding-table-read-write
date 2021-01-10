package com.luxf.sharding.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 分表
 *
 * @author 小66
 */
@Data
public class Answer implements Serializable {

    private Long id;

    private Long userId;

    private String text;

    private String result;
}
