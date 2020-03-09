package com.czf.controller.viewobject;

import lombok.Data;

/**
 * @author czf
 * @Date 2020/3/9 1:13 下午
 */
@Data
public class UserVO {
    private  Integer uid;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
}
