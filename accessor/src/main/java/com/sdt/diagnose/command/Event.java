package com.sdt.diagnose.command;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.common.ApplicationUtil;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.ScreenRecordActivity;
import com.sdt.diagnose.common.ScreenRecordService;
import com.sdt.diagnose.common.ScreenShot;
import com.sdt.diagnose.common.ScreenShot2;
import com.sdt.diagnose.common.ShellUtils;
import com.sdt.diagnose.common.bean.AppIntent;
import com.sdt.diagnose.common.bean.SysIntent;
import com.sdt.diagnose.common.net.CreateSSL;
import com.sdt.diagnose.common.net.HttpUtils;
import com.sdt.diagnose.common.net.HttpsUtils;
import com.sdt.diagnose.database.DbManager;
import com.skyworth.scrrtcsrv.Device;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Event {
    private static final String TAG = "TR369 Event";
    private static final String REBOOT = "Reboot";
    private static final String FACTORYRESET = "FactoryReset";
    private static final String UPLOADFILE = "uploadfile";
    private static final String DOWNLOADFILE = "downloadfile";
    private static final String REBOOT_XMPP = "REBOOT_XMPP";
    private static final String CHECK_DOWNLOAD_SPEED = "check_download_speed";
    private static final String IPTV_PLAY_CHECK = "iptv_play_check";
    private static final String SYSTEM_UPGRADE_CHECK = "system_upgrade_check";
    private static final String ADDOBJECT = "AddObject";
    private static final String DELETEOBJECT = "DeleteObject";
    private static final String PING = "tr369_ping";
    private static final String TRACEROUTE = "tr069_traceroute";
    private static final String SCREENCAP = "ScreenCap";
    private static final String UPGRADE_APPLICATION = "Upgrade_application";
    private static final String LOADBLELIST = "load_bluetoothlist";
    private static final String UNPAIR_BLE_DEVICE = "Unpair_ble_device";
    private static final String UPGRADEFILE = "upgradeFile";
    private static final String SYS_UPGRADE_TYPE = "Device.X_Skyworth.Upgrade.FWForceUpgradeType";
    private static final String APK_UPGRADE_TYPE = "Device.X_Skyworth.Upgrade.APKForceUpgradeType";
    private static final String SCREENSHOT_TYPE = "X Skyworth Screenshot File";
    private static final String VIDEO_TYPE = "X Skyworth Video File";
    private static final String APP_ICON_TYPE = "X Skyworth App Icon File";
    private static final String Config_File = "1 Vendor Configuration File";
    private static final String Log_File = "2 Vendor Log File";
    public static final String OTA_SYS_DATA = "otaSys";
    public static final String OTA_APP_DATA = "otaApp";
    private static final String ACTION_BOOT_EXTERNAL_SYS = "com.skw.ota.update.ExternalSysUpdate";
    private static final String ACTION_BOOT_EXTERNAL_APP = "com.skw.ota.update.ExternalAppUpdate";
    private static final String OTA_NEW_PARAMS = "newParams";

    private static final String SPLIT = "###";
    private static final int INDEX_COMMAND = 0;
    private static final int INDEX_PARAM_1 = 1;
    private static final int INDEX_PARAM_2 = 2;
    private static final int INDEX_PARAM_3 = 3;
    private static final int INDEX_PARAM_4 = 4;

    private static final int SYS_UPGRADE_FORCE = 0;
    private static final int SYS_UPGRADE_REMINDER = 1;

    private static final int APP_UPGRADE_FORCE = 2;
    private static final int APP_UPGRADE_REMINDER = 1;

    private static final String FILE_TYPE_LOG = "uploadLog";
    private static final String FILE_TYPE_CONFIG = "uploadConfig";
    private static final String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat df = new SimpleDateFormat(format);
    public static String RAW_LOG_FILE = "logcat.log";
    private static final String LOG_SOURCE_DIR_PATH = "/data/tcpdump/";
    private static final String LOG_SOURCE_FILE_PATH = "/data/tcpdump/logcat_tr369.log";


    @Tr369Set("skyworth.tr369.event")
    public boolean SK_TR369_SetEventParams(String path, String value) {
        Log.d(TAG, "skyworth.tr369.event path: " + path + ", value: " + value);
        String[] strings = split(value);
        if (strings == null || strings.length == 0) return false;

        switch (strings[INDEX_COMMAND]) {
            case REBOOT:
                ((PowerManager) GlobalContext.getContext().getSystemService(Context.POWER_SERVICE)).reboot("");
                break;
            case FACTORYRESET:
                factoryReset();
                break;
            case UPLOADFILE:
                upload(strings[INDEX_PARAM_4], strings[INDEX_PARAM_2], strings[INDEX_PARAM_3]);
                break;
            case DOWNLOADFILE:
                downloadFile(strings);
                break;
            case CHECK_DOWNLOAD_SPEED:
                calcNetSpeed();
            case UPGRADEFILE:
                //发送升级广播
                upgradeSw(strings);
                break;
            case REBOOT_XMPP:
                rebootXmpp();
                break;
            case IPTV_PLAY_CHECK:
                checkIptvPlay();
                break;
            case SYSTEM_UPGRADE_CHECK:
                checkSystemUpgrade();
                break;
            case ADDOBJECT:
//                addObject(strings[1]);
                break;
            case DELETEOBJECT:
//                deleteObject(strings[1]);
                break;
            case PING:
                try {
                    ping();
                } catch (Exception e) {
                    Log.e(TAG, "tr369 ping error, " + e.getMessage());
                }
                break;
            case TRACEROUTE:
//                TraceRoute traceroute = TraceRouteManager.getInstance().getTraceRoute();
//                SkyDiagnoseServiceManager.getInstance().deleteMultiObject("Device.IP.Diagnostics.TraceRoute.RouteHops.%d.", traceroute.traces.size());
//                traceroute.getTraces().clear();
//
//                String host = DbManager.getDBParam(Tr069Param.DEVICE_IP_DIAGNOSE_TRACEROUTE_HOST);
//                traceroute.executeTraceroute();
                break;
            case SCREENCAP:
                screenCap();
                break;
            case UPGRADE_APPLICATION:
                ApplicationUtil.upgradeApplication(strings[INDEX_PARAM_1]);
            case UNPAIR_BLE_DEVICE:
//                BluetoothUtil.unpair(strings[INDEX_PARAM_1]);
                break;
            default:
                Log.d(TAG, "Not Implemented, skyworth.tr369.event: " + value);
        }

        return true;
    }

    private void calcNetSpeed() {
        CountDownLatch lock = new CountDownLatch(1);
        String url = DbManager.getDBParam("Device.IP.Diagnostics.DownloadDiagnostics.DownloadURL");
        String duration = DbManager.getDBParam("Device.IP.Diagnostics.DownloadDiagnostics.TimeBasedTestDuration");
        Log.d(TAG, "calcNetSpeed url = " + url + ", duration = " + duration);
        long exc_duration;
        if (TextUtils.isEmpty(duration)) {
            exc_duration = 10000;
        } else {
            exc_duration = Long.parseLong(duration) * 1000 - 500;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        long startTime = System.currentTimeMillis();

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                long endTime = System.currentTimeMillis();
                DbManager.setDBParam("Device.IP.Diagnostics.DownloadDiagnostics.EOMTime", df.format(endTime));
                Log.e(TAG, "Failed to calculate network speed. Failure Message: " + e.getMessage());
                call.cancel();
                lock.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                if (response.body() == null) {
                    Log.e(TAG, "calcNetSpeed error: response.body returns a null pointer");
                    return;
                }
                // 下载 outputStream inputStream
                InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                //文件的总长度
                long maxLen = Objects.requireNonNull(response.body()).contentLength();
                Log.d(TAG, "calcNetSpeed onResponse: maxLen = " + maxLen);
                byte[] bytes = new byte[1024];

                int readLength = 0;
                long cureeLength = 0;

                long startRx = TrafficStats.getTotalRxBytes();
                long endTime = System.currentTimeMillis();

                while ((readLength = inputStream.read(bytes)) != - 1) {
                    cureeLength += readLength;
                    endTime = System.currentTimeMillis();
                    if (cureeLength < maxLen) {
                        if (endTime - startTime >= exc_duration) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                DbManager.setDBParam("Device.IP.Diagnostics.DownloadDiagnostics.EOMTime", df.format(endTime));
                Log.d(TAG, "calcNetSpeed onResponse: cureeLength = " + cureeLength);
                long endRx = TrafficStats.getTotalRxBytes();
                Log.d(TAG, "calcNetSpeed onResponse: Rx length = " + (endRx - startRx));
                DbManager.setDBParam("Device.IP.Diagnostics.DownloadDiagnostics.TestBytesReceived",
                        String.valueOf(cureeLength));
                call.cancel();
                Log.d(TAG, "calcNetSpeed onResponse: TestBytesReceived = " + cureeLength);
                Log.d(TAG, "calcNetSpeed call.isCanceled: " + call.isCanceled());
                inputStream.close();
                lock.countDown();
            }
        });

        DbManager.setDBParam("Device.IP.Diagnostics.DownloadDiagnostics.BOMTime", df.format(startTime));
        try {
            lock.await(exc_duration + 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "calcNetSpeed lock.await call failed, " + e.getMessage());
        }
        DbManager.setDBParam("Device.IP.Diagnostics.DownloadDiagnostics.DiagnosticsState", "Complete");

    }

    private String[] split(String value) {
        if (TextUtils.isEmpty(value)) return null;
        return value.split(SPLIT);
    }

    private void deleteObject(String path) {
//        DbManager.deleteObject(path);
        int l1 = path.lastIndexOf(".");
        int l2 = path.lastIndexOf(".", l1 - 1);
        String numPath = path.substring(0, path.length() - (l1 - l2) - 1) + "NumberOfEntries";
        DbManager.setDBParam(numPath, String.valueOf(Math.max((Integer.parseInt(DbManager.getDBParam(numPath)) - 1), 0)));
    }

    private void addObject(String path) {
        String numPath = path.substring(0, path.length() - 1) + "NumberOfEntries";
        if (TextUtils.isEmpty(DbManager.getDBParam(numPath))) {
            DbManager.setDBParam(numPath, "1");
        } else {
            DbManager.setDBParam(numPath, String.valueOf(Integer.parseInt(DbManager.getDBParam(numPath)) + 1));
        }
    }

    private void checkSystemUpgrade() {
    }

    private void checkIptvPlay() {
    }

    private void rebootXmpp() {
        Log.d(TAG, "rebootXmpp called.");
        Intent rebootIntent = new Intent("skyworth.intent.action.REBOOT_XMPP"/*Intent.ACTION_FACTORY_RESET*/);
        rebootIntent.setPackage("com.skyworth.xmppservice");
        GlobalContext.getContext().sendBroadcast(rebootIntent);
    }

    private void upgradeSw(String[] params) {
        int paramLen = params.length;
        String upgradeUrl;
        String upgradeFileSize;
        String upgradeFileName;
        Map<String, Object> upgradeData = new HashMap<>();
        Log.d(TAG, "upgradeSw: params.lenth = " + params.length);

        if (paramLen > INDEX_PARAM_3) {
            upgradeUrl = params[INDEX_PARAM_1];
            upgradeFileName = params[INDEX_PARAM_2];
            upgradeFileSize = params[INDEX_PARAM_3];
            Log.d(TAG, "upgradeSw: fileUrl>> " + upgradeUrl + ", fileSize>> " + upgradeFileSize);
            Intent intent = new Intent();
            intent.setPackage("com.sdt.ota");
            //如果是.zip结尾表示是系统升级,.apk结尾表示是app升级
            if (upgradeUrl.contains(".zip")) {
                SysIntent sysIntent = new SysIntent();
                SysIntent.SysBean bean = new SysIntent.SysBean();
                String force_upgrade = DbManager.getDBParam(SYS_UPGRADE_TYPE);
                bean.setFileUrl(upgradeUrl);
                bean.setFileName(upgradeFileName);
                bean.setFileSize(Long.parseLong(upgradeFileSize));
                bean.setUpgradeType(2);

                if (force_upgrade.equals("0")) {
                    bean.setUpgradeMode(SYS_UPGRADE_REMINDER);
                } else {
                    bean.setUpgradeMode(SYS_UPGRADE_FORCE);
                }
                sysIntent.setBeanList(Collections.singletonList(bean));
                intent.setAction(ACTION_BOOT_EXTERNAL_SYS);
                intent.putExtra(OTA_NEW_PARAMS, upgradeUrl);
            } else if (upgradeUrl.contains(".apk")) {
                AppIntent appIntent = new AppIntent();
                AppIntent.AppBean appBean = new AppIntent.AppBean();
                String force_upgrade = DbManager.getDBParam(APK_UPGRADE_TYPE);
                appBean.setFileUrl(upgradeUrl);
                appBean.setFileName(upgradeFileName);
                appBean.setFileSize(Long.parseLong(upgradeFileSize));
                if (force_upgrade.equals("0")) {
                    appBean.setUpgradeMode(APP_UPGRADE_REMINDER);
                } else {
                    appBean.setUpgradeMode(APP_UPGRADE_FORCE);
                }
                appBean.setUpgradeType(1);
                appIntent.setBeanList(Collections.singletonList(appBean));
                intent.setAction(ACTION_BOOT_EXTERNAL_APP);
                intent.putExtra(OTA_NEW_PARAMS, upgradeUrl);
            }
            GlobalContext.getContext().sendBroadcast(intent);
        }

    }


    private void downloadFile(String[] params) {

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d(TAG, "HttpLoggingInterceptor message = " + message);
            }
        }));
        Request request = new Request.Builder()
                .url(params[2])
                .build();
        Call call = okHttpClientBuilder.build().newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "Failed to download file. Failure Message: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (200 == response.code()) {
                        if (response.body() == null) {
                            Log.e(TAG, "downloadFile error: response.body() is a null pointer");
                            return;
                        }
                        File file = new File("/cache/", "update.zip");
                        FileOutputStream fos = new FileOutputStream(file);
                        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                        byte[] buffer = new byte[1024 * 4];
                        int len = 0;
                        float count = 0;
                        long contentLength = Objects.requireNonNull(response.body()).contentLength();
                        while ((len = inputStream.read(buffer)) != - 1) {
                            count += len;
                            fos.write(buffer, 0, len);
                            float l = (float) (count * 100 / contentLength * 1.0);
                            if (l % (1024 * 1024 * 10) == 0) {
                                Log.d(TAG, "downloadFile result: " + (l * 1.0 / contentLength) * 100 + ("%"));
                            }
                        }

                        fos.flush();
                        fos.close();
                        inputStream.close();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Call to download function failed. Failure Message: " + e.getMessage());
        }

    }

    public static void uploadLogFile(String uploadUrl, String filePath, int fileCount) {
        try {
            Log.i(TAG, "uploadLogFile: Start uploading files: " + filePath + ", count: " + fileCount);
            URL url = new URL(uploadUrl);
            if (url.toString().contains("https")) {
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                uploadLogFileByHttps(con, filePath, fileCount);
                con.disconnect();
            } else {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                uploadLogFileByHttp(con, filePath, fileCount);
                con.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "uploadLogFile: Failed to upload log file, " + e.getMessage());
        }
    }

    private static void uploadLogFileByHttps(HttpsURLConnection con, String filePath, int fileCount) {
        try {
            // 允许Input、Output，不使用Cache
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            SSLSocketFactory sslSocketFactory = new CreateSSL().getSSLSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.equals(DbManager.getDBParam("Device.ManagementServer.URL").split(":")[1].replace("//", ""));
                }
            });
            con.setConnectTimeout(50000);
            con.setReadTimeout(50000);
            // 设置传送的method=POST
            con.setRequestMethod("POST");
            //在一次TCP连接中可以持续发送多份数据而不会断开连接
            con.setRequestProperty("Connection", "Keep-Alive");
            //设置编码
            con.setRequestProperty("Charset", "UTF-8");
            //text/plain能上传纯文本文件的编码格式
            con.setRequestProperty("Content-Type", "text/plain");

            // 指定剩余待上传文件
            String remainingFileCount = String.valueOf(fileCount);
            con.setRequestProperty("RemainingFileCount", remainingFileCount);
            // 指定当前上传的文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            con.setRequestProperty("Filename", fileName);

            if (fileCount > 0) {
                // 设置DataOutputStream
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                // 取得文件的FileInputStream
                FileInputStream fStream = new FileInputStream(filePath);
                // 设置每次写入1024bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int length = - 1;
                // 从文件读取数据至缓冲区
                while ((length = fStream.read(buffer)) != - 1) {
                    // 将资料写入DataOutputStream中 对于有中文的文件需要使用GBK编码格式
                    // ds.write(new String(buffer, 0, length).getBytes("GBK"));
                    ds.write(buffer, 0, length);
                }
                ds.flush();
                fStream.close();
                ds.close();
            }

            if (con.getResponseCode() == 200) {
                Log.i(TAG, "uploadLogFileByHttps: File uploaded successfully! File path: " + filePath);
//                Intent intent = new Intent();
//                intent.setAction("com.skw.diagnose.UploadSuccess");
//                intent.setPackage(ParsePackageName.getPackageName(intent));
//                intent.putExtra("status", 0);
//                intent.putExtra("reason", "upload success");
//                GlobalContext.getContext().sendBroadcast(intent);
                // 上传成功，只删除分段保存的那些文件
                if (! filePath.contains(RAW_LOG_FILE)) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        Log.d(TAG, "uploadLogFileByHttps: wait to delete file: " + filePath);
                        file.delete();
                    }
                }
            } else {
                Log.e(TAG, "uploadLogFileByHttps: Get response code: " + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "uploadLogFileByHttps: Failed to upload log file, " + e.getMessage());
        }
    }


    private static void uploadLogFileByHttp(HttpURLConnection con, String filePath, int fileCount) {
        try {
            // 允许Input、Output，不使用Cache
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(50000);
            con.setReadTimeout(50000);
            // 设置传送的method=POST
            con.setRequestMethod("POST");
            //在一次TCP连接中可以持续发送多份数据而不会断开连接
            con.setRequestProperty("Connection", "Keep-Alive");
            //设置编码
            con.setRequestProperty("Charset", "UTF-8");
            //text/plain能上传纯文本文件的编码格式
            con.setRequestProperty("Content-Type", "text/plain");

            // 指定剩余待上传文件
            String remainingFileCount = String.valueOf(fileCount);
            con.setRequestProperty("RemainingFileCount", remainingFileCount);
            // 指定当前上传的文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            con.setRequestProperty("Filename", fileName);

            if (fileCount > 0) {
                // 设置DataOutputStream
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());

                // 取得文件的FileInputStream
                FileInputStream fStream = new FileInputStream(filePath);
                // 设置每次写入1024bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int length = - 1;
                // 从文件读取数据至缓冲区
                while ((length = fStream.read(buffer)) != - 1) {
                    // 将资料写入DataOutputStream中
                    //ds.write(new String(buffer,0,length).getBytes("GBK"));
                    ds.write(buffer, 0, length);
                }
                ds.flush();
                fStream.close();
                ds.close();
            }

            if (con.getResponseCode() == 200) {
                Log.i(TAG, "uploadLogFileByHttp: File uploaded successfully! File path: " + filePath);
//                Intent intent = new Intent();
//                intent.setAction("com.skw.diagnose.UploadSuccess");
//                intent.setPackage(ParsePackageName.getPackageName(intent));
//                intent.putExtra("status", 0);
//                intent.putExtra("reason", "upload success");
//                GlobalContext.getContext().sendBroadcast(intent);
                // 上传成功，只删除分段保存的那些文件
                if (! filePath.contains(RAW_LOG_FILE)) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        Log.d(TAG, "uploadLogFileByHttp: wait to delete file: " + filePath);
                        file.delete();
                    }
                }
            } else {
                Log.e(TAG, "uploadLogFileByHttp: Get response code: " + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "uploadLogFileByHttp: Failed to upload log file, " + e.getMessage());
        }
    }

    private void upload(String uploadUrl, String fileType, String delaySeconds) {
        switch (fileType) {
            case SCREENSHOT_TYPE:
                if (! Device.isScreenOn()) {
                    Log.e(TAG, "The screen is not in use and there is no need to take a screenshot.");
                    break;
                }
                ScreenShot2.getInstance().takeAndUpload(uploadUrl);
                break;
            case VIDEO_TYPE:
                handleVideoFile(uploadUrl, delaySeconds);
                break;
            case Config_File:
                //uploadFile("vendor/etc/skyconfig/config.properties", uploadUrl); // https 证书问题
                String configFilePath = GlobalContext.getContext().getFilesDir().getPath() + "refresh.txt";
                getRefreshData(configFilePath);
                uploadLogFile(uploadUrl, configFilePath, 1);
                break;
            case Log_File:
                handleLogFile(uploadUrl);
                break;
            case APP_ICON_TYPE:
                uploadIconFile(uploadUrl);
                break;
        }
    }

    private void handleVideoFile(String uploadUrl, String delaySeconds) {
        if (! Device.isScreenOn()) {
            Log.e(TAG, "The screen is not in use and there is no need to perform screen recording.");
            return;
        }
        Intent intent = new Intent(GlobalContext.getContext(), ScreenRecordActivity.class);
        intent.putExtra(ScreenRecordActivity.UPLOAD_URL, uploadUrl);
        intent.putExtra(ScreenRecordActivity.DELAY_SECONDS, Integer.parseInt(delaySeconds));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        GlobalContext.getContext().startActivity(intent);
        //同步
        synchronized (ScreenRecordService.SYNC_OBJ) {
            try {
                //超时退出，至少要等待：要录制的时间 + 用户同意录屏的时间 + 缓冲时间。
                ScreenRecordService.SYNC_OBJ.wait(
                        Integer.parseInt(delaySeconds) * 1000L
                                + ScreenRecordActivity.WAITING_USER_AGREE_TIME_MS
                                + 5000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Call to ScreenRecordService function failed. " + e.getMessage());
            }
        }
    }

    private boolean isFileNeedToBeUploaded(String fileName, String startTime, String endTime) {
        if (fileName == null || startTime == null || endTime == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(fileName);
        String fileTime = "0";

        if (matcher.find()) {
            fileTime = matcher.group();
        } else {
            return false;
        }

        return Long.parseLong(fileTime) > Long.parseLong(startTime)
                && Long.parseLong(fileTime) <= Long.parseLong(endTime);
    }

    private void handleLogFile(String uploadUrl) {
        if (uploadUrl == null) {
            Log.e(TAG, "Execution error: URL parameter error.");
            return;
        }
        File folder = new File(LOG_SOURCE_DIR_PATH);
        if (! folder.exists() || ! folder.isDirectory()) {
            Log.e(TAG, "Execution error: The folder for storing logs was not found.");
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            Log.e(TAG, "Execution error: This abstract pathname does not denote a directory.");
            return;
        }

        // 分割号和同事协商后定义为%%%，服务端下发数据内容<URL>https://xxx/xxx%%%开始时间戳%%%结束时间戳</URL>
        String[] keywords = uploadUrl.split("%%%");
        String filterUrl = "";
        String filterStartTime = "";
        String filterEndTime = "";
        if (keywords.length > 0) {
            filterUrl = keywords[0];
            if (keywords.length > 2) {
                filterStartTime = keywords[1];
                filterEndTime = keywords[2];
            }
            Log.d(TAG, "The filtering condition for uploading logs is: " + filterStartTime
                    + " ~ " + filterEndTime + " > filterUrl: " + filterUrl);
        }

        ArrayList<String> logFiles = new ArrayList<>();
        if (filterUrl.isEmpty()) {
            Log.e(TAG, "Execution error: URL recognition failed.");
            return;
        } else if (filterStartTime.isEmpty() || filterEndTime.isEmpty()) {
//            File file = new File(LOG_SOURCE_FILE_PATH);
//            uploadLogFile(filterUrl, file.getAbsolutePath(), 1);
//            return;
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.startsWith(RAW_LOG_FILE)) {
                        logFiles.add(file.getAbsolutePath());
                    }
                }
            }
        } else {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.contains("logcat_tr369_")
                            && fileName.contains(".txt")
                            && isFileNeedToBeUploaded(fileName, filterStartTime, filterEndTime)) {
                        logFiles.add(file.getAbsolutePath());
                    }
                }
            }
        }

        int fileCounts = logFiles.size();
        if (fileCounts > 0) {
            for (String logFile : logFiles) {
                Log.d(TAG, "About to upload file: " + logFile);
                uploadLogFile(filterUrl, logFile, fileCounts);
                fileCounts--;
            }
        } else {
            uploadLogFile(filterUrl, "", 0);
        }

    }

    public void uploadIconFile(String uploadUrl) {
        String packageName = uploadUrl.split("/")[uploadUrl.split("/").length - 1].replace("-", ".");
        PackageManager packageManager = GlobalContext.getContext().getPackageManager();
        List<PackageInfo> packlist = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo packageInfo : packlist) {
            if (packageInfo.packageName.equals(packageName)) {
                Drawable drawable = packageInfo.applicationInfo.loadIcon(packageManager);
                saveIcon(drawable, packageInfo.applicationInfo.name);
                uploadAppIcon(uploadUrl, GlobalContext.getContext().getFilesDir() + "/"
                        + packageInfo.applicationInfo.name + ".png", new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to upload icon file. Failure Message: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.d(TAG, "uploadIconFile onResponse: " + response.protocol()
                                + ", code: " + response.code()
                                + ", message: " + response.message());
                    }
                });
            }
        }
    }

    public void saveIcon(Drawable icon, String appName) {
        String dataPath = GlobalContext.getContext().getFilesDir() + "/";
        File file = new File(dataPath + appName + ".png");
        if (! file.exists()) {
            try {
                file.createNewFile();
                Bitmap bitmap = drawableToBitmap(icon);
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                //上传app图标
            } catch (IOException e) {
                Log.e(TAG, "Call to saveIcon function failed. " + e.getMessage());
            }
        }
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        // canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void uploadAppIcon(String uploadUrl, String filePath, Callback callback) {
        if (uploadUrl.startsWith("https")) {
            HttpsUtils.uploadFile(uploadUrl, filePath, true, callback);
        } else if (uploadUrl.startsWith("http")) {
            HttpUtils.uploadFile(uploadUrl, filePath, true, callback);
        }
    }

    private void factoryReset() {
        Log.d(TAG, "FactoryReset called.");
        Intent resetIntent = new Intent("android.intent.action.MASTER_CLEAR"/*Intent.ACTION_FACTORY_RESET*/);
        resetIntent.setPackage("android");
        resetIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        resetIntent.putExtra("resason"/*Intent.EXTRA_REASON*/, "RemoteFactoryReset");
        GlobalContext.getContext().sendBroadcast(resetIntent);
    }

    /**
     * ping -c %d -s %d -t %d %s
     * 示例：ping -c 5 -s 1024 -t 64 www.baidu.com
     */
    private void ping() {
        String addr = DbManager.getDBParam("Device.IP.Diagnostics.IPPing.Host");
        String dscp = DbManager.getDBParam("Device.IP.Diagnostics.IPPing.DSCP");
        String len = DbManager.getDBParam("Device.IP.Diagnostics.IPPing.DataBlockSize");
        String pingCount = DbManager.getDBParam("Device.IP.Diagnostics.IPPing.NumberOfRepetitions");
        StringBuilder cmd = new StringBuilder("/system/bin/");
        cmd.append("ping -c ").append(pingCount)
                .append(" -s ").append(len)
                .append(" -t ").append(64)
                .append(" ").append(addr);

        Log.d(TAG, "tr369_ping cmd: " + cmd);

        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(cmd.toString(), false);
        Log.d(TAG, "tr369_ping getString: " + commandResult.toString());

        String ret = (commandResult.result == 0 ? commandResult.successMsg : commandResult.errorMsg);
        DbManager.setDBParam("skyworth.param.tr369_ping", ret);
    }

    private void screenCap() {
        ScreenShot screenShot = new ScreenShot(GlobalContext.getContext());
        Bitmap bitmap = screenShot.takeScreenshot();
    }

    static class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            Log.d(TAG, "HttpLogInfo: " + message);
        }
    }

    public void getRefreshData(String path) {
        String AUTHORITY = "com.skw.data.center";
        Uri sUri = Uri.parse("content://" + AUTHORITY + "/all");
        new Thread() {
            @Override
            public void run() {
                super.run();
                Cursor cursor = GlobalContext.getContext().getContentResolver().query(sUri, null, null,
                        null, null, null);
                File file = new File(path);
                try {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            try {
                                String line1 = cursor.getString(0) + "=" + cursor.getString(1) + "\n";
                                String line2 = cursor.getString(2) + "=" + cursor.getInt(3) + "\n";
                                fileOutputStream.write(line1.getBytes(StandardCharsets.UTF_8));
                                fileOutputStream.write(line2.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException e) {
                                Log.e(TAG, "getRefreshData Stream function call failed. " + e.getMessage());
                            }
                        }
                        fileOutputStream.close();
                        cursor.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "getRefreshData File function call failed. " + e.getMessage());
                }
            }
        }.start();
    }
}
