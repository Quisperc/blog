package cn.civer.blog.Model.Entity;

import cn.civer.blog.Model.Enum.Status;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ---------------- 成功 ----------------
    public static <T> Result<T> success(T data) {
        log.info(String.valueOf(data));
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(String msg, T data) {
        log.info(String.valueOf(data));
        return new Result<>(Status.SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> successMsg(String msg) {
        log.info(msg);
        return new Result<>(Status.SUCCESS.getCode(), msg, null);
    }

    // ---------------- 错误 ----------------
    public static <T> Result<T> error(T data) {
        log.error(String.valueOf(data));
        return new Result<>(Status.OPERATION_ERROR.getCode(), Status.OPERATION_ERROR.getMsg(), data);
    }

    public static <T> Result<T> error() {
        return new Result<>(Status.UNKNOWN_ERROR.getCode(), Status.UNKNOWN_ERROR.getMsg(), null);
    }

    public static <T> Result<T> error(String msg, T data) {
        log.error(String.valueOf(data));
        return new Result<>(Status.OPERATION_ERROR.getCode(), msg, data);
    }

    public static <T> Result<T> errorMsg(String msg) {
        log.error(msg);
        return new Result<>(Status.OPERATION_ERROR.getCode(), msg, null);
    }
}
