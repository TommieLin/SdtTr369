package com.sdt.diagnose.common;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.sdt.diagnose.common.net.HttpsUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";
    private static final String CONFIG_FILE_PATH_DEFAULT = "/vendor/etc/skyconfig/config.properties";
    private static final String CONFIG_DEVICE_OPERATOR = "tms_operator_name";

    public static String getDeviceName(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
    }

    public static String getOperatorName() {
        InputStream is;
        BufferedReader reader;
        String line = null;
        String operatorName = "platform";

        try {
            int pos = - 1;
            is = Files.newInputStream(Paths.get(CONFIG_FILE_PATH_DEFAULT));
            reader = new BufferedReader(new InputStreamReader(is));
            line = reader.readLine();
            while (line != null) {
                pos = line.indexOf(CONFIG_DEVICE_OPERATOR);
                if (pos >= 0) {
                    Log.d(TAG, "DeviceInfoUtil Read configuration file data: " + line);
                    break;
                }
                line = reader.readLine();
            }
            reader.close();
            is.close();

            if (pos >= 0) {
                operatorName = line.substring(pos + CONFIG_DEVICE_OPERATOR.length() + 1);
                Log.d(TAG, "DeviceInfoUtil operatorName: " + operatorName);
            }
        } catch (Exception e) {
            Log.e(TAG, "DeviceInfoUtil getOperatorName Exception error: " + e.getMessage());
        }

        return operatorName;
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getSerialNumber() {
        return Build.getSerial();
    }

    /**
     * Reads a line from the specified file.
     *
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename), 256)) {
            return reader.readLine();
        }
    }

    private static String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine("/sys/board_properties/soc/msv");
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException | NumberFormatException e) {
            // Fail quietly, as the file may not exist on some devices, or may be unreadable
        }
        return "";
    }

    public static String getDeviceModel() {
        return Build.MODEL + getMsvSuffix();
    }

    public static String getDeviceFirmwareVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getBuildInfo() {
        return Build.DISPLAY;
    }

    static String getSecurityPatch() {
        String patch = Build.VERSION.SECURITY_PATCH;
        if (! "".equals(patch)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(patch);
                if (patchDate == null) return "";
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                patch = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {
                // broken parse; fall through and use the raw string
            }
            return patch;
        } else {
            return "";
        }
    }

    public static String getAndroidSecurityPatchLevel() {
        return getSecurityPatch();
    }

    public static String getBluetoothMac(Context context) {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        String result = "unavailable";
        if (bluetooth != null) {
            String address = bluetooth.isEnabled() ? bluetooth.getAddress() : "";
            if (! TextUtils.isEmpty(address)) {
                // Convert the address to lowercase for consistency with the wifi MAC address.
                result = address.toLowerCase();
            }
        }
        return result;
    }

    public static void updateStandbyStatus(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager.isInteractive();
        if (isScreenOn) {
            // 设备处于唤醒状态
            HttpsUtils.uploadStandbyStatus(1);
        } else {
            // 设备处于待机状态
            HttpsUtils.uploadStandbyStatus(0);
        }
    }


}
