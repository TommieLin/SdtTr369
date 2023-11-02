package com.sdt.diagnose.Device;

import android.util.Log;

import com.sdt.annotations.Tr369Get;
import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.common.DeviceInfoUtil;

public class DeviceInfoX {

    private static final String TAG = "DeviceInfoX";

    @Tr369Get("Device.DeviceInfo.Manufacturer")
    public String SK_TR369_GetManufacturer() {
        return DeviceInfoUtil.getManufacturer();
    }

    @Tr369Get("Device.DeviceInfo.SerialNumber")
    public String SK_TR369_GetSerialNumber() {
        return DeviceInfoUtil.getSerialNumber(); // about - Status - Serial Number
    }


}
