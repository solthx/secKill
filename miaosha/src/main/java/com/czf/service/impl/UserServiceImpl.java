package com.czf.service.impl;

import com.czf.dao.UserDOMapper;
import com.czf.dao.UserPasswordDOMapper;
import com.czf.dataobject.UserDO;
import com.czf.dataobject.UserPasswordDO;
import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.service.UserService;
import com.czf.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     * 用户注册
     * @param userModel
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel==null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        if (StringUtils.isEmpty(userModel.getName())
                || userModel.getAge()==null
                || userModel.getGender()==null
                || StringUtils.isEmpty(userModel.getTelphone())){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"注册参数缺失或不合法！");
        }
        // 实现model->dao
        UserDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            System.out.println("....");
            ex.printStackTrace();
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已注册");
        }
        userModel.setUid(userDO.getUid());
        UserPasswordDO userPasswordDO = convertPassWordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }


    /**
     * 使用手机号+密码，实现用户登陆
     * @param telphone
     * @param encrptPassword
     * @return
     * @throws BusinessException
     */
    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        // 获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO==null)
            throw  new BusinessException(EmBusinessError.USER_LOGIN_FAIL,"用户手机号或密码错误");
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getUid());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

        // 比对用户信息内加密的密码是否和传输来进来的密码相匹配
        if (!com.alibaba.druid.util.StringUtils.equals(encrptPassword, userPasswordDO.getEncrptPassword()))
            throw  new BusinessException(EmBusinessError.USER_LOGIN_FAIL,"用户手机号或密码错误");
        return userModel;
    }

    private UserPasswordDO convertPassWordFromModel(UserModel userModel){
        if (userModel==null)
            return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUid(userModel.getUid());
        return userPasswordDO;
    }

    // model->dao
    private UserDO convertFromModel(UserModel userModel){
        if ( userModel==null )
            return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
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
