package com.czf.service.impl;

import com.czf.dao.UserDOMapper;
import com.czf.dao.UserPasswordDOMapper;
import com.czf.dataobject.UserDO;
import com.czf.dataobject.UserPasswordDO;
import com.czf.service.UserService;
import com.czf.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author czf
 * @Date 2020/3/9 12:42 下午
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        //根据用户id获取userDO
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO==null) // 用户不存在
            return null;
        // 通过用户id获取用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);
        return convertFromDataObject(userDO,userPasswordDO);
    }

    /**
     * 组装UserModel
     * @param userDO
     * @param userPasswordDO
     * @return
     */
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO == null)
            return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null )
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        return userModel;
    }
}
