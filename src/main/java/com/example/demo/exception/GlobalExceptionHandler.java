package com.example.demo.exception;

import com.example.demo.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理：把分散的异常统一转换成前端更容易处理的 JSON 结构。
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 处理业务异常，例如学生不存在、id 冲突等。
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }
    // 处理 @Valid 校验失败异常，例如 name 为空、age 小于 0。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.fail(400, message);
    }
    // 处理路径参数、请求参数上的约束异常，例如 /students/0。
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.fail(400, ex.getMessage());
    }

    // 兜底处理：避免异常直接抛给前端，先统一成失败响应。
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        return Result.fail(500, ex.getMessage());
    }
}
