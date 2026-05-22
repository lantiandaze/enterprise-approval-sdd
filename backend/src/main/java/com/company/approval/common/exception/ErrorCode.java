package com.company.approval.common.exception;

public enum ErrorCode {

    BAD_REQUEST("BAD_REQUEST", "请求参数错误"),
    VALIDATION_FAILED("VALIDATION_FAILED", "参数校验失败"),
    UNAUTHORIZED("UNAUTHORIZED", "请先登录"),
    FORBIDDEN("FORBIDDEN", "无权访问"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "账号或密码错误"),
    BUSINESS_ERROR("BUSINESS_ERROR", "业务处理失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

