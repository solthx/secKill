package com.czf.validator;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author czf
 * @Date 2020/3/10 8:20 下午
 */
@Data
public class ValidationResult {
    // 校验结果是否有错
    private boolean hasErrors=false;

    // 存放错误信息的map
    private Map<String, String> errorMsgMap;

    public ValidationResult(){
        errorMsgMap = new HashMap<>();
    }

    // 实现通用的通过格式化字符串信息获取错误结果的msg方法
    public String getErrMsg(){
        return StringUtils.join(errorMsgMap.values().toArray(), ",");
    }
}
