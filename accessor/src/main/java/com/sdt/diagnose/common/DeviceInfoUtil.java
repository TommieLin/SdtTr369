package com.sdt.diagnose.common;

import android.os.Build;

public class DeviceInfoUtil {

    public static String getSerialNumber() {
        return Build.getSerial();
    }

}
