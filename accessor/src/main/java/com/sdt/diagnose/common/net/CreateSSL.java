package com.sdt.diagnose.common.net;

import android.text.TextUtils;

import com.sdt.diagnose.common.log.LogUtils;
import com.sdt.opentr369.OpenTR369Native;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * @Author Outis
 * @Date 2022/1/10 11:32
 * @Version 1.0
 */
public class CreateSSL {
    private static final String TAG = "CreateSSL";
    //证书文件路径
    String path = "vendor/etc/security/certs/";
    private static final String CA = "cacert.pem";
    private static final String CERTIFICATE = "clientcert.pem";
    private static final String CERTKEY = "clientkey.pem";
    private final String CLIENT_CER_PASSWORD = "sky@0123";
    private static OkHttpClient mOkHttpClient = null;
    private String ca = getUsefulCertInfo(getCertString(path + CA));
    private String certKey = getUsefulCertInfo(getCertString(path + CERTKEY));
    private String certificate = getUsefulCertInfo(getCertString(path + CERTIFICATE));

    public OkHttpClient getCheckedOkHttpClient() {
        SSLSocketFactory ssl = null;
        try {
            checkCertExist();
            ssl = SSLHelper.getSslSocketFactory(ca, certKey, certificate, CLIENT_CER_PASSWORD);
        } catch (Exception e) {
            LogUtils.e(TAG, "Get checked ssl exception: " + e.getMessage());
            ssl = SSLHelper.getNoCheckSSLSocketFactory();
        }
        X509TrustManager trustManager =
                TextUtils.isEmpty(ca) ? SSLHelper.getTrustManager() : SSLHelper.getTrustManager(ca);

        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS)
                    .readTimeout(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS)
                    .writeTimeout(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(5, 2, TimeUnit.MINUTES))
                    .sslSocketFactory(ssl, trustManager)
                    .hostnameVerifier((hostname, session) -> true)
                    .retryOnConnectionFailure(true)
                    .build();
        }
        return mOkHttpClient;
    }

    public String getCertString(String fileName) {
        File file = new File(fileName);
        FileReader fileReader = null;
        StringBuilder sb = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            sb = new StringBuilder();
            String temp = "";
            while ((temp = br.readLine()) != null) {
                // 拼接换行符
                sb.append(temp + "\n");
            }
            br.close();
        } catch (Exception e) {
            LogUtils.d(TAG, "getCertString call failed, " + e.getMessage());
        }

        return (sb != null) ? sb.toString() : "";
    }

    public SSLSocketFactory getSSLSocketFactory() {
        try {
            checkCertExist();
            return SSLHelper.getSslSocketFactory(ca, certKey, certificate, CLIENT_CER_PASSWORD);
        } catch (Exception e) {
            LogUtils.e(TAG, "getSslSocketFactory call failed, " + e.getMessage());
        }
        return null;
    }

    //获取证书信息
    public String getUsefulCertInfo(String certificate) {
        String startingSubstring = "-----BEGIN";
        int startIndex = certificate.indexOf(startingSubstring);
        if (startIndex != -1) {
            return certificate.substring(startIndex);
        } else {
            return "";
        }
    }

    private void checkCertExist() {
        if (ca.isEmpty()) {
            ca = OpenTR369Native.GetCACertString();
        }

        if (certificate.isEmpty()) {
            certificate = OpenTR369Native.GetDevCertString();
        }

        if (certKey.isEmpty()) {
            certKey = OpenTR369Native.GetDevKeyString();
        }
    }
}
