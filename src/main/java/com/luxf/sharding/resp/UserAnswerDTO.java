package com.luxf.sharding.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 小66
 **/
@Data
public class UserAnswerDTO implements Serializable {
    private Long id;

    private String city;

    private String name;

    private Long answerId;

    private String text;

    private String result;
}
