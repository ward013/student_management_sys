package com.example.demo.common;

// Result<T> 是统一接口返回格式。
// T 表示 data 字段的数据类型，例如 Student、List<Student>、Integer、String 等。
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功返回：有 data 的情况。
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 成功返回：自定义 message 和 data。
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败返回：只有错误信息，没有 data。
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    // 失败返回：自定义 code 和 message。
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
