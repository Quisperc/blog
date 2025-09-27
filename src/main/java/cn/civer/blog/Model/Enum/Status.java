package cn.civer.blog.Model.Enum;

import lombok.Getter;

@Getter
public enum Status {
        UNKNOWN_ERROR(-1, "未知错误"),
        SUCCESS(200, "成功"),
        USER_NOT_EXIST(200, "用户不存在"),
        USER_IS_EXISTS(200, "用户已存在"),
        DATA_IS_NULL(200, "数据为空"),
        ;
        private Integer code;
        private String msg;

        Status(Integer code, String msg) {
                this.code = code;
                this.msg = msg;
        }
}