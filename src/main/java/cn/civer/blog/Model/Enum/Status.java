package cn.civer.blog.Model.Enum;

import lombok.Getter;

@Getter
public enum Status {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    USER_NOT_EXIST(404, "用户不存在"),
    DATA_IS_NULL(404, "数据为空"),
    USER_IS_EXISTS(409, "用户已存在"),
    OPERATION_ERROR(500, "操作失败"),
    UNKNOWN_ERROR(500, "未知错误");

    private final Integer code;
    private final String msg;

    Status(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}