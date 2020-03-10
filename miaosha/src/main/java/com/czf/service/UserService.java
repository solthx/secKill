package com.czf.service;

import com.czf.error.BusinessException;
import com.czf.service.model.UserModel;

/**
 * @author czf
 * @Date 2020/3/9 12:41 下午
 */
public interface UserService {
    /**
     * 通过用户ID获取用户对象
     * @param id
     * @return
     */
    UserModel getUserById(Integer id);

    /**
     * 用户注册
     * @param userModel
     * @throws BusinessException
     */
    void register(UserModel userModel) throws BusinessException;

    /**
     * 使用手机号+密码，实现用户登陆
     * @param telphone
     * @param encrptPassword
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}
