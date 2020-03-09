package com.czf.service;

import com.czf.service.model.UserModel;

/**
 * @author czf
 * @Date 2020/3/9 12:41 下午
 */
public interface UserService {
    // 通过用户ID获取用户对象
    UserModel getUserById(Integer id);
}
