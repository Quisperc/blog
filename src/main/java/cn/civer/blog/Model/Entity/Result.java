package cn.civer.blog.Model.Entity;

import cn.civer.blog.Model.Enum.Status;
import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    /**
     * 全参数返回
     * @param code 状态码
     * @param message 信息
     * @param data 数据
     * @return 封装结果
     * @param <T> 泛型
     */
    public static<T> Result<T> response(Integer code,String message, T data){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }
    // 错误（Data）
    public static<T> Result<T> error(T Data){
        Result<T> result = new Result<>();
        result.setCode(Status.UNKNOWN_ERROR.getCode());
        result.setMsg(Status.UNKNOWN_ERROR.getMsg());
        result.setData(Data);
        return result;
    }
    // 错误（null）
    public static<T> Result<T> error(){
        Result<T> result = new Result<>();
        result.setCode(Status.UNKNOWN_ERROR.getCode());
        result.setMsg(Status.UNKNOWN_ERROR.getMsg());
        result.setData(null);
        return result;
    }
    // 错误（msg,Data）
    public static<T> Result<T> error(String msg,T Data){
        Result<T> result = new Result<>();
        result.setCode(Status.UNKNOWN_ERROR.getCode());
        result.setMsg(msg);
        result.setData(Data);
        return result;
    }
    // 错误（msg,Data）
    public static<T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.setCode(Status.UNKNOWN_ERROR.getCode());
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    // 正确(Data)
    public static<T> Result<T> Success(T Data){
        Result<T> result = new Result<>();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());
        result.setData(Data);
        return result;
    }
    // 成功（null）
    public static<T> Result<T> Success(){
        Result<T> result = new Result<>();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());
        result.setData(null);
        return result;
    }
    // 成功（msg,Data）
    public static<T> Result<T> Success(String msg,T Data){
        Result<T> result = new Result<>();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(msg);
        result.setData(Data);
        return result;
    }
    // 成功（msg）
    public static<T> Result<T> Success(String msg){
        Result<T> result = new Result<>();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
