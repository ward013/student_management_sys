package com.example.demo.exception;

// BusinessException 用来表示业务层可预期的失败，例如学生不存在、id 已存在。
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
