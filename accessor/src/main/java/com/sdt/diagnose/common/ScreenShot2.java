package com.sdt.diagnose.common;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.sdt.diagnose.command.Event;
import com.sdt.diagnose.common.log.LogUtils;
import com.sdt.diagnose.common.net.HttpUtils;
import com.sdt.diagnose.common.net.HttpsUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScreenShot2 {
    private static final String TAG = "ScreenShot2";
    private static ScreenShot2 mScreenShot2;
    private static String screenshotFilePath;
    private static String uploadUrl;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static final String fileParentDirPath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).toString() :
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

    public static ScreenShot2 getInstance() {
        synchronized (ScreenShot2.class) {
            if (mScreenShot2 == null) {
                mScreenShot2 = new ScreenShot2();
            }
        }
        return mScreenShot2;
    }

    private ScreenShot2() {
    }

    public void takeAndUpload(String uploadUrl) {
        ScreenShot2.uploadUrl = uploadUrl;
        if (takeScreenshot()) {
            uploadScreenshot();
        }
    }

    private boolean takeScreenshot() {
        File fileDir = new File(fileParentDirPath);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            String message = "Unable to create folder.";
            LogUtils.e(TAG, "takeScreenshot: " + message);
            Event.setUploadResponseDBParams("Error", message);
            return false;
        }
        screenshotFilePath = String.format("%s/%s.png", fileParentDirPath, simpleDateFormat.format(new Date()));
        NetWorkSpeedUtils.runShellCommand("/system/bin/screencap -p " + screenshotFilePath);
        return true;
    }

    private void uploadScreenshot() {
        if (uploadUrl.startsWith("https")) {
            HttpsUtils.uploadFile(uploadUrl, screenshotFilePath, true, new UploadFileCallback());
        } else if (uploadUrl.startsWith("http")) {
            HttpUtils.uploadFile(uploadUrl, screenshotFilePath, true, new UploadFileCallback());
        }
    }

    static class UploadFileCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            File file = new File(screenshotFilePath);
            file.delete();
            String message = "Failed to upload screenshot, " + e.getMessage();
            LogUtils.e(TAG, "UploadFileCallback: " + message);
            Event.setUploadResponseDBParams("Error", message);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response)
                throws IOException {
            if (response.code() == 200) {
                File file = new File(screenshotFilePath);
                file.delete();
                String message = "Successfully uploaded screenshot.";
                LogUtils.e(TAG, "UploadFileCallback: " + message
                        + " Protocol: " + response.protocol()
                        + ", Code: " + response.code());
                Event.setUploadResponseDBParams("Complete", message);
            } else {
                String message = "Failed to upload screenshot. " + response.protocol()
                        + " " + response.code() + " " + response.message();
                LogUtils.e(TAG, "UploadFileCallback: " + message);
                Event.setUploadResponseDBParams("Error", message);
            }
        }
    }
}
