package com.czf.error;

/**
 * @author czf
 * @Date 2020/3/9 3:27 下午
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
