package com.sdt.android.tr369.Bean;

public class CaCertBean {
    private String privateKey;
    private String certContent;
    private boolean isGzip;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCertContent() {
        return certContent;
    }

    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    public boolean isGzip() {
        return isGzip;
    }

    public void setGzip(boolean gzip) {
        isGzip = gzip;
    }

    @Override
    public String toString() {
        return "CaCertBean{" +
                "privateKey='" + privateKey + '\'' +
                ", certContent='" + certContent + '\'' +
                ", isGzip=" + isGzip +
                '}';
    }
}
