package com.sdt.android.tr369.Bean;

public class MqttConfigsResponseBean {
    private String message;
    private String mqttServer;
    private CaCertBean caCert;
    private ClientCertBean clientCert;
    private boolean enable;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMqttServer() {
        return mqttServer;
    }

    public void setMqttServer(String mqttServer) {
        this.mqttServer = mqttServer;
    }

    public CaCertBean getCaCert() {
        return caCert;
    }

    public void setCaCert(CaCertBean caCert) {
        this.caCert = caCert;
    }

    public ClientCertBean getClientCert() {
        return clientCert;
    }

    public void setClientCert(ClientCertBean clientCert) {
        this.clientCert = clientCert;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "MqttConfigsResponseBean{" +
                "message='" + message + '\'' +
                ", mqttServer='" + mqttServer + '\'' +
                ", caCert=" + caCert +
                ", clientCert=" + clientCert +
                ", enable=" + enable +
                '}';
    }
}
