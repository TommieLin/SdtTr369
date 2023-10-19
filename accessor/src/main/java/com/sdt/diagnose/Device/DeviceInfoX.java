package com.sdt.diagnose.Device;

import android.util.Log;

import com.sdt.annotations.Tr369Get;
import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.common.DeviceInfoUtil;

public class DeviceInfoX {
    private static final String TAG = "DeviceInfoX";
    @Tr369Get("Device.DeviceInfo.SerialNumber")
    public String SK_TR369_GetSerialNumber() {
        return DeviceInfoUtil.getSerialNumber(); // about - Status - Serial Number
    }

    @Tr369Get("Device.X_Skyworth.OperatorName")
    public String SK_TR369_GetOperatorName() {
        Log.e(TAG, " ####### Outis ### SK_TR369_GetOperatorName start");
        return "Platform";
    }

    @Tr369Set("Device.X_Skyworth.Background.Enable")
    public boolean SK_TR369_SetBackgroundEnable(String path, String value) {
        Log.e(TAG, " ####### Outis ### SK_TR369_SetBackgroundEnable start, value: " + value);
        return true;
    }

}
