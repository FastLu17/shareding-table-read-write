package com.luxf.sharding.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分表
 *
 * @author 小66
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = -1205226416664488559L;

    private Long id;

    private String city;

    private String name;
}
