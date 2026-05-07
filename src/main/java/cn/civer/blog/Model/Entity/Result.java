package cn.civer.blog.Model.Entity;

import cn.civer.blog.Model.Enum.Status;
import lombok.Data;

import java.io.Serializable;

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

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(Status.SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> successMsg(String msg) {
        return new Result<>(Status.SUCCESS.getCode(), msg, null);
    }

    public static <T> Result<T> error(T data) {
        return new Result<>(Status.OPERATION_ERROR.getCode(), Status.OPERATION_ERROR.getMsg(), data);
    }

    public static <T> Result<T> error() {
        return new Result<>(Status.UNKNOWN_ERROR.getCode(), Status.UNKNOWN_ERROR.getMsg(), null);
    }

    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(Status.OPERATION_ERROR.getCode(), msg, data);
    }

    public static <T> Result<T> errorMsg(String msg) {
        return new Result<>(Status.OPERATION_ERROR.getCode(), msg, null);
    }
}
