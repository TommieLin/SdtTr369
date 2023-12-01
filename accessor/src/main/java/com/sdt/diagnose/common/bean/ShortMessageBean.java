package com.sdt.diagnose.common.bean;

import androidx.annotation.NonNull;

/**
 * @Author Outis
 * @Date 2023/11/30 17:50
 * @Version 1.0
 */
public class ShortMessageBean {
    private String deviceId;
    private String model;
    private Integer operatorId;
    private String messageId;
    private String type;
    private String content;
    private String title;
    private String richTextUrl;
    private String imageUrl;
    private String position;
    private Integer showTime;

    public Integer getShowTime() {
        return showTime;
    }

    public void setShowTime(Integer showTime) {
        this.showTime = showTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRichTextUrl() {
        return richTextUrl;
    }

    public void setRichTextUrl(String richTextUrl) {
        this.richTextUrl = richTextUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @NonNull
    public String toString() {
        return "ShortMessageBean {"
                + "deviceId='"
                + deviceId
                + '\''
                + ", model='"
                + model
                + '\''
                + ", operatorId="
                + operatorId
                + ", messageId='"
                + messageId
                + '\''
                + ", type='"
                + type
                + '\''
                + ", content='"
                + content
                + '\''
                + ", title='"
                + title
                + '\''
                + ", richTextUrl='"
                + richTextUrl
                + '\''
                + ", imageUrl='"
                + imageUrl
                + '\''
                + ", showTime="
                + showTime
                + '}';
    }
}
