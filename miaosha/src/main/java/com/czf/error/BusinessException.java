package com.czf.error;

/**
 * @author czf
 * @Date 2020/3/9 3:37 下午
 */

// 包装器业务异常类实现
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError, String ErrMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(ErrMsg);
    }

    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
