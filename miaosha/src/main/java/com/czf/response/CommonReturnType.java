package com.czf.response;

import lombok.Data;

/**
 * @author czf
 * @Date 2020/3/9 3:14 下午
 */
@Data
public class CommonReturnType {
    // state 取值范围 { "success", "fail" }
    private String status;
    // 若state为success, 则data为返回结果
    // 若state为fail，则data内使用通用的错误码格式
    private Object data;

    // 创建一个通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus(status);
        return type;
    }

}
