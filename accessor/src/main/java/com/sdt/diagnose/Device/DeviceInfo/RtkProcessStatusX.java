package com.sdt.diagnose.Device.DeviceInfo;

import android.util.Log;

import com.realtek.hardware.RtkVoutUtilManager;

public class RtkProcessStatusX {
    private static final String TAG = "RtkProcessStatusX";
    private static RtkProcessStatusX mRtkProcessStatusX;

    RtkProcessStatusX() {
    }

    public static RtkProcessStatusX getInstance() {
        if (null == mRtkProcessStatusX) {
            mRtkProcessStatusX = new RtkProcessStatusX();
        }
        return mRtkProcessStatusX;
    }

    public double getCpuUsageByRtk() {
        double usage = 0;
        try {
            RtkVoutUtilManager manager = new RtkVoutUtilManager();
            usage = (double) manager.getProcStat();
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getCpuUsageByRtk - RtkVoutUtilManager call failed, " + e.getMessage());
        }
        Log.d(TAG, "getCpuUsageByRtk: " + usage);
        return usage;
    }
}
