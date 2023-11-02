package com.sdt.diagnose.Device.Platform;

import android.os.SystemProperties;
import android.util.Log;

public class ModelX {
    private static final String TAG = "ModelX";

    public static Type getPlatform() {
        String platform = SystemProperties.get("ro.soc.model", "").toLowerCase();
        Log.d(TAG, "platform is [" + platform + "]");
        if (platform.startsWith("aml")) {
            return Type.Amlogic;
        } else if (platform.startsWith("rtd")) {
            return Type.Realtek;
        } else {
            return Type.Default;
        }
    }

    public enum Type {
        Amlogic,
        Realtek,
        Default
    }

}
