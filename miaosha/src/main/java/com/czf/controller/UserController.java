package com.czf.controller;

import com.czf.controller.viewobject.UserVO;
import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.response.CommonReturnType;
import com.czf.service.UserService;
import com.czf.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;
import sun.security.rsa.RSASignature;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author czf
 * @Date 2020/3/9 12:38 下午
 */
@Controller("user_controller")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*") // 解决ajax的跨域请求问题
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据id获取user实体
     * @param id
     * @return
     * @throws BusinessException
     */
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

    /**
     * 输入手机号，获取验证码
     * @param telphone
     * @return
     */
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        // 生成OTP验证码，生成随机数 [10000,100000)
        Random random = new Random();
        int randomInt = random.nextInt(90000);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        // 将OTP验证码绑定到对应当前用户的Session里 ( 分布式场景下里是保存在redis里并定时失效，这里先粗糙的实现一下..
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        // 将OTP验证码通过短信通道发送给用户，需要第三方平台，先省略
        System.out.println("telphone = " + telphone + " & OtpCode = " + otpCode);
        return CommonReturnType.create(null);
    }

    /**
     * 注册用户信息
     * @param telphone
     * @param otpCode
     * @param name
     * @param gender
     * @param age
     * @param password
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/register", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam("telphone")String telphone,
                                     @RequestParam("otpCode")String otpCode,
                                     @RequestParam("name")String name,
                                     @RequestParam("gender")Integer gender,
                                     @RequestParam("age")Integer age,
                                     @RequestParam("password") String password
    ) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 先验证otpCode是否符合
        String inSessionOtpCode = (String)httpServletRequest.getSession().getAttribute(telphone);
        if ( !com.alibaba.druid.util.StringUtils.equals(inSessionOtpCode,otpCode) )
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不正确");
        // 用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setName(name);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("bytelphone");
        //userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes()));
        userModel.setEncrptPassword(this.EncodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    /**
     * 给字符串进行md5加密
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    private String EncodeByMD5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // 确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        // 加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }


    @RequestMapping(value = "/login", method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam("telphone")String telphone,
                                  @RequestParam("password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 入参校验
        if ( org.apache.commons.lang3.StringUtils.isEmpty(telphone)
            || StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号或密码为空");
        }

        // 用户登陆服务
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMD5(password));

//        将登陆凭证加入到用户登陆成功的session内, 下面已改为使用Token
//        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
//        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        // 用户登陆成功后，将对应的登陆信息和登陆凭证一起存入redis中
        // 生成登陆凭证token, UUID
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");
        // 建立token和用户态之间的联系
        redisTemplate.opsForValue().set(uuidToken, userModel);
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);

        // 下发了token
        return CommonReturnType.create(uuidToken);
    }
}
