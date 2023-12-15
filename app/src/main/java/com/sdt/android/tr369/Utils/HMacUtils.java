package com.sdt.android.tr369.Utils;

import android.text.TextUtils;

import com.sdt.diagnose.common.log.LogUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMacUtils {
    private static final String TAG = "HMacUtils";
    private static final String ALGORITHM = "HmacSHA256";

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String calculateHMac(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance(ALGORITHM);

        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        sha256_HMAC.init(secret_key);

        return byteArrayToHex(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String generatePassword(String dev_mac, String dev_sn) {
        String ret = "";
        if (TextUtils.isEmpty(dev_mac) || TextUtils.isEmpty(dev_sn)) {
            LogUtils.e(TAG, "Parameters are empty");
            return ret;
        }

        // generate HMAC_SHA256 of dev_mac and dev_sn.
        try {
            // set time zone
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
            Date date = new Date();
            date.setTime(date.getTime() + timeZone.getRawOffset());
            //get date string of today.
            String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
            ret = HMacUtils.calculateHMac(dev_mac, dev_sn + "/" + today);
        } catch (Exception e) {
            LogUtils.e(TAG, "generatePassword error, " + e.getMessage());
        }
        return ret;
    }

}
