package com.czf.controller;

import com.czf.controller.viewobject.UserVO;
import com.czf.error.BusinessException;
import com.czf.error.CommonError;
import com.czf.error.EmBusinessError;
import com.czf.response.CommonReturnType;
import com.czf.service.UserService;
import com.czf.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author czf
 * @Date 2020/3/9 12:38 下午
 */
@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        // 调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        if ( userModel==null )
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);

        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    /**
     * 将核心领域模型用户对象转换为可供UI使用的viewobject
     * @param userModel
     * @return
     */
    private UserVO convertFromModel(UserModel userModel) {
        if (userModel==null)
            return null;
        UserVO userVO = new UserVO();
        // 将变量名相同，变量类型相同的值 拷贝过去！ 第一个拷贝给第二个
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
