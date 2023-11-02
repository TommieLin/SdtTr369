package com.sdt.diagnose.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @ProjectName: SkySystemServer
 * @Package: com.skyworthdigital.common
 * @ClassName: NetWorkSpeedUtils
 * @Description: java类作用描述
 * @Author: pengdeping
 * @CreateDate: 2020/12/30 12:11
 * @UpdateUser: pengdeping
 * @UpdateDate: 2020/12/30 12:11
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class NetWorkSpeedUtils {
    private static final String TAG = "NetWorkSpeedUtils";
    private static long lastTotalRxBytes = 0;
    private static long lastTotalTxBytes = 0;
    private static long lastRxTimeStamp = 0;
    private static long lastTxTimeStamp = 0;

    private static long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED
                ? 0 : (TrafficStats.getTotalRxBytes()); // 单位: KB
    }

    private static long getTotalTxBytes() {
        return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED
                ? 0 : (TrafficStats.getTotalTxBytes()); // 单位: KB
    }

    public static int calcDownSpeed() {
        long nowTimeStamp = System.currentTimeMillis();
        long nowTotalRxBytes = getTotalRxBytes();
        if (nowTotalRxBytes <= lastTotalRxBytes) return 0;
        else if (nowTimeStamp <= lastRxTimeStamp) return 0;
        float downSpeed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000f / (nowTimeStamp - lastRxTimeStamp));
        lastTotalRxBytes = nowTotalRxBytes;
        lastRxTimeStamp = nowTimeStamp;
        return (int) downSpeed; // 单位: b/s
    }

    public static int calcUpSpeed() {
        long nowTimeStamp = System.currentTimeMillis();
        long nowTotalTxBytes = getTotalTxBytes();
        if (nowTotalTxBytes <= lastTotalTxBytes) return 0;
        else if (nowTimeStamp <= lastTxTimeStamp) return 0;
        float upSpeed = ((nowTotalTxBytes - lastTotalTxBytes) * 1000f / (nowTimeStamp - lastTxTimeStamp));
        lastTxTimeStamp = nowTimeStamp;
        lastTotalTxBytes = nowTotalTxBytes;
        return (int) upSpeed; // 单位: b/s
    }

    public static String getNetSpeed(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            //无连接
            return null;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || ! networkInfo.isAvailable()) {
            //无连接
            return null;
        }
        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_WIFI) {
            return wifiInfo.getLinkSpeed() + wifiInfo.LINK_SPEED_UNITS;
        } else if (type == ConnectivityManager.TYPE_ETHERNET) {
            return runShellCommand("cat /sys/class/net/eth0/speed").trim() + "Mbps";
        }
        return null;
    }

    //执行shell命令
    public static String runShellCommand(String command) {
        Runtime runtime;
        Process proc = null;
        StringBuilder stringBuffer = null;
        try {
            runtime = Runtime.getRuntime();
            proc = runtime.exec(command);
            stringBuffer = new StringBuilder();
            if (proc.waitFor() != 0) {
                Log.e(TAG, "runShellCommand exit value: " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));

            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }

        } catch (Exception e) {
            Log.e(TAG, "runShellCommand exit error: " + e.getMessage());
        } finally {
            try {
                if (proc != null) proc.destroy();
            } catch (Exception e2) {
                Log.e(TAG, "runShellCommand destroy error: " + e2.getMessage());
            }
        }
        return (stringBuffer != null) ? stringBuffer.toString() : "";
    }
}
