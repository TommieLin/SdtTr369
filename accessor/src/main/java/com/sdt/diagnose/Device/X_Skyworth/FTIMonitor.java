package com.sdt.diagnose.Device.X_Skyworth;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.log.LogUtils;


/**
 * @Author Outis
 * @Date 2023/11/30 13:51
 * @Version 1.0
 */
public class FTIMonitor {
    private static final String TAG = "FTIMonitor";
    private final Handler mHandler;
    private final HandlerThread mThread;
    private static int mTimeSpent = 0;  // 之前已度过的时间（用于还在FTI阶段但重启后，需要接着上次的时间）
    private static int mCycles = 0; // 监控循环的次数
    private static final int MSG_START_MONITOR_FTI_DURATION = 3303;
    private static final int DEFAULT_PERIOD_MILLIS_TIME = 30000;    // 默认30s监控一次

    public FTIMonitor() {
        mThread = new HandlerThread("FTIMonitorThread", Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandler =
                new Handler(mThread.getLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == MSG_START_MONITOR_FTI_DURATION) {
                            // 开始监控用户在开机向导停留的时间
                            startMonitorFTIDuration();
                        }
                    }
                };
        if (!isUserSetupComplete()) {
            handleFTIDurationSpent();
            mHandler.sendEmptyMessage(MSG_START_MONITOR_FTI_DURATION);
        }
    }

    public boolean isUserSetupComplete() {
        return Settings.Secure.getIntForUser(GlobalContext.getContext().getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0, UserHandle.USER_CURRENT) != 0;
    }

    private void handleFTIDurationSpent() {
        String timeSpent = SystemProperties.get("persist.sys.tr369.FTI.residence.duration", "");
        if (timeSpent.length() != 0 && Integer.parseInt(timeSpent) > 0) {
            mTimeSpent = Integer.parseInt(timeSpent);
        } else {
            mTimeSpent = 0;
        }
    }

    private void startMonitorFTIDuration() {
        int duration = mTimeSpent + (mCycles * DEFAULT_PERIOD_MILLIS_TIME);
        SystemProperties.set("persist.sys.tr369.FTI.residence.duration", String.valueOf(duration));

        if (isUserSetupComplete()) {
            LogUtils.d(TAG, "The monitored FTI residence duration time is: " + duration + "ms");
            mHandler.removeMessages(MSG_START_MONITOR_FTI_DURATION);
        } else {
            mCycles++;
            mHandler.sendEmptyMessageDelayed(MSG_START_MONITOR_FTI_DURATION, DEFAULT_PERIOD_MILLIS_TIME);
        }
    }
}
