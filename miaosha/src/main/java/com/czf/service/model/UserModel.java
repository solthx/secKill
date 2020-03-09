package com.czf.service.model;

import lombok.Data;

/**
 * @author czf
 * @Date 2020/3/9 12:45 下午
 */
@Data
public class UserModel {
    private Integer uid;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
    private String registerMode;
    private String thirdPartyId;

    private String encrptPassword;
}
