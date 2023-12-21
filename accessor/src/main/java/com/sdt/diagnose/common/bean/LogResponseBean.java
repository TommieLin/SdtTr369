package com.sdt.diagnose.common.bean;

/**
 * @Author Outis
 * @Date 2023/11/30 9:38
 * @Version 1.0
 */
public class LogResponseBean {
    private Integer code;
    private String message;
    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LogResponseBean{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
