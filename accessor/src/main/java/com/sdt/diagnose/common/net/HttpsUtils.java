package com.sdt.diagnose.common.net;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sdt.diagnose.Device.X_Skyworth.LogManager;
import com.sdt.diagnose.common.DeviceInfoUtils;
import com.sdt.diagnose.common.FileUtils;
import com.sdt.diagnose.common.bean.LogResponseBean;
import com.sdt.diagnose.common.bean.NotificationBean;
import com.sdt.diagnose.common.bean.StandbyBean;
import com.sdt.diagnose.common.log.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpsUtils {
    private static final String TAG = "HttpsUtils";
    public static OnUploadCallback mOnUploadCallback;
    static CreateSSL mCreateSSL = new CreateSSL();

    public static void uploadFile(String url, String fileFullPath, boolean isSyncRequest, Callback callback) {
        File file = new File(fileFullPath);
        RequestBody requestBody = RequestBody.create(FileUtils.fileToByte(file));

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .post(requestBody)
                .build();

        execute(call(request), isSyncRequest, callback);
    }

    public static void uploadSpeedData(String url, String content, String dataType, String transactionId, String isEnd) {
        CreateSSL createSSL = new CreateSSL();
        OkHttpClient okHttpClient = createSSL.getCheckedOkHttpClient();
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("dataType", dataType)    //取值有 upload download failure
                .addHeader("transactionId", transactionId)
                .addHeader("isEnd", isEnd)      //取值有 true false 用于指示测速是否结束
                .post(RequestBody.create(mediaType, content))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "uploadSpeedData onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                LogUtils.d(TAG, "uploadSpeedData protocol: " + response.protocol()
                        + ", code: " + response.code()
                        + ", message: " + response.message());
            }
        });
    }

    public static void uploadLog(String url, String content, String transactionId) {
        CreateSSL createSSL = new CreateSSL();
        OkHttpClient okHttpClient = createSSL.getCheckedOkHttpClient();
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .header("transactionId", transactionId)
                .url(url)
                .post(RequestBody.create(mediaType, content))
                .build();
        mOnUploadCallback.deleteLog(content.length());

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "uploadLog onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) return;
                Gson gson = new Gson();
                LogResponseBean logResponseBean = gson.fromJson(response.body().string(),
                        LogResponseBean.class);
                if (logResponseBean.getCode() != 0) {
                    LogUtils.e(TAG, "uploadLog response code: " + logResponseBean.getCode() + ", stop upload log");
                    LogManager.getInstance().stopLog();
                }
                LogUtils.d(TAG, "uploadLog protocol: " + response.protocol()
                        + ", code: " + response.code()
                        + ", message: " + response.message());
            }
        });
    }

    public static void requestAppUpdateStatus(String url, HashMap<String, String> param, Callback callback) {
        CreateSSL createSSL = new CreateSSL();
        OkHttpClient okHttpClient = createSSL.getCheckedOkHttpClient();
        String wholeUrl = buildUrl(url, param);
        Request request = new Request.Builder()
                .url(wholeUrl)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void uploadScreenshotAllowStatus(String url, int status, String transactionId) {
        HashMap<String, String> param = new HashMap<>();
        param.put("deviceId", DeviceInfoUtils.getSerialNumber());
        param.put("confirmCode", String.valueOf(status));
        param.put("transactionId", transactionId);
        String wholeUrl = buildUrl(url, param);
        LogUtils.d(TAG, "uploadScreenshotAllowStatus wholeUrl: " + wholeUrl);
        requestAndCallback(wholeUrl);
    }

    public static void uploadAllowStatus(String url, int status, String confirmMessage, String transactionId) {
        HashMap<String, String> param = new HashMap<>();
        param.put("deviceId", DeviceInfoUtils.getSerialNumber());
        param.put("confirmCode", String.valueOf(status));
        param.put("confirmMessage", confirmMessage);
        param.put("transactionId", transactionId);
        String wholeUrl = buildUrl(url, param);
        LogUtils.d(TAG, "uploadAllowStatus wholeUrl: " + wholeUrl);
        requestAndCallback(wholeUrl);
    }

    public static void uploadStandbyStatus(int status) {
        String url = StandbyBean.getInstance().getUpdateUrl();
        if (TextUtils.isEmpty(url)) {
            LogUtils.e(TAG, "uploadStandbyStatus: The upload URL is empty!");
            return;
        }
        HashMap<String, String> param = new HashMap<>();
        param.put("deviceId", DeviceInfoUtils.getSerialNumber());
        param.put("confirmCode", String.valueOf(status));
        String wholeUrl = buildUrl(url, param);
        LogUtils.d(TAG, "uploadStandbyStatus wholeUrl: " + wholeUrl);
        requestAndCallback(wholeUrl);
    }

    public static void uploadNotificationStatus(boolean isCompleted) {
        String url = NotificationBean.getInstance().getUrl();
        if (TextUtils.isEmpty(url)) {
            LogUtils.e(TAG, "uploadNotificationStatus: The upload URL is empty!");
            return;
        }
        HashMap<String, String> param = new HashMap<>();
        param.put("status", String.valueOf(isCompleted));
        String wholeUrl = buildUrl(url, param);
        LogUtils.d(TAG, "uploadNotificationStatus wholeUrl: " + wholeUrl);
        requestAndCallback(wholeUrl);
    }

    private static void requestAndCallback(String wholeUrl) {
        CreateSSL createSSL = new CreateSSL();
        OkHttpClient okHttpClient = createSSL.getCheckedOkHttpClient();
        Request request = new Request.Builder()
                .url(wholeUrl)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "requestAndCallback onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                LogUtils.d(TAG, "requestAndCallback onResponse: " + response.protocol()
                        + ", code: " + response.code()
                        + ", message: " + response.message());
            }
        });
    }

    public static void noticeResponse(String url, HashMap<String, String> param) {
        CreateSSL createSSL = new CreateSSL();
        OkHttpClient okHttpClient = createSSL.getCheckedOkHttpClient();
        String wholeUrl = buildUrl(url, param);
        LogUtils.d(TAG, "noticeResponse url: " + url + ", wholeUrl: " + wholeUrl);
        Request request = new Request.Builder()
                .url(wholeUrl)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "noticeResponse onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                LogUtils.d(TAG, "noticeResponse onResponse: " + response.protocol()
                        + ", code: " + response.code()
                        + ", message: " + response.message());
            }
        });
    }

    /**
     * 构建https请求
     */
    private static Call call(Request httpRequest) {
        return mCreateSSL.getCheckedOkHttpClient().newCall(httpRequest);
    }

    /**
     * 执行 OkHttps call
     */
    private static void execute(Call call, boolean isSyncRequest, Callback callback) {
        if (isSyncRequest) {
            Response response = null;
            try {
                if (call != null) {
                    response = call.execute();
                    if (callback != null) callback.onResponse(call, response);
                }
            } catch (IOException e) {
                if (callback != null) callback.onFailure(call, e);
            } finally {
                if (response != null) response.close();
            }
        } else {
            if (call != null) call.enqueue(callback);
        }
    }

    private static String buildUrl(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        // 去掉baseUrl后面的?符号
        if (url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        String param = buildUrlParams(params);
        if (!TextUtils.isEmpty(param)) {
            url = url + param;
        }
        return url;
    }

    private static String buildUrlParams(Map<String, String> params) {
        // TODO: 2019/8/6 参数由外部保证 query不能为null
        int count = 0;
        StringBuilder result = new StringBuilder();
        if (null == params || params.isEmpty()) {
            return null;
        }

        final String gap = "&";
        for (String key : params.keySet()) {
            String value = null;

            if (key != null) {
                value = params.get(key);
                if (!TextUtils.isEmpty(value)) {
                    if (count == 0) {
                        result = new StringBuilder("?" + key + "=" + value);
                    } else {
                        result.append(gap).append(key).append("=").append(value);
                    }
                    count++;
                }
            }
        }

        return result.toString();
    }

    public interface OnUploadCallback {
        void deleteLog(int length);
    }

    public static void setOnUploadCallback(OnUploadCallback onUploadCallback) {
        mOnUploadCallback = onUploadCallback;
    }
}
