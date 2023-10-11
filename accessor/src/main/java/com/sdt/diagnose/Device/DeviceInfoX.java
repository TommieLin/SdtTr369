package com.sdt.diagnose.Device;

import com.sdt.annotations.Tr369Get;
import com.sdt.diagnose.common.DeviceInfoUtil;

public class DeviceInfoX {

    @Tr369Get("Device.DeviceInfo.SerialNumber")
    public String SK_TR369_GetSerialNumber() {
        return DeviceInfoUtil.getSerialNumber(); // about - Status - Serial Number
    }

}
