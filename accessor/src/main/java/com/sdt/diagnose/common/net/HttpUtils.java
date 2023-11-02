package com.sdt.diagnose.common.net;

import android.util.Log;

import com.sdt.diagnose.Device.X_Skyworth.LogManager;
import com.sdt.diagnose.common.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpUtils {
    public static final String TAG = "HttpUtils";

    public static void uploadFile(String url, String fileFullPath, boolean isSyncRequest, Callback callback) {
        try {
            File file = new File(fileFullPath);

            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    try {
                        String text = URLDecoder.decode(message, "utf-8");
                        Log.d(TAG, "HttpLoggingInterceptor text: " + text);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "HttpLoggingInterceptor error: " + e.getMessage());
                    }
                }
            });
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(logInterceptor).build();

            RequestBody requestBody = RequestBody.create(FileUtils.fileToByte(file));

            Request request = new Request.Builder()
                    .addHeader("Accept", "*/*")
                    .url(url)
                    .post(requestBody)
                    .build();

            if (isSyncRequest) {
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    if (callback != null) callback.onResponse(call, response);
                } catch (IOException e) {
                    if (callback != null) callback.onFailure(call, e);
                }
            } else {
                client.newCall(request).enqueue(callback);
            }
        } catch (Exception ex) {
            // Handle the error
            ex.printStackTrace();
        }
    }

    public static void uploadLog(String url, String content, String transactionId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .header("transactionId", transactionId)
                .url(url)
                .post(RequestBody.create(mediaType, content))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "uploadLog onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() != 200) {
                    LogManager.getInstance().stopLog();
                    Log.e(TAG, "The response code is not 200, so stop uploading logs");
                }
                Log.d(TAG, "uploadLog onResponse: " + response);
            }
        });
    }
}
