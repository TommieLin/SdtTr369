package com.sdt.diagnose.Device;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.sdt.annotations.Tr369Get;
import com.sdt.diagnose.common.DeviceInfoUtils;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.NetworkUtils;
import com.sdt.diagnose.database.DbManager;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class DeviceInfoX {
    private static final String TAG = "DeviceInfoX";

    @Tr369Get("Device.DeviceInfo.Manufacturer")
    public String SK_TR369_GetManufacturer() {
        return DeviceInfoUtils.getManufacturer();
    }

    @Tr369Get("Device.DeviceInfo.ManufacturerOUI")
    public String SK_TR369_GetManufacturerOUI() {
        return DbManager.getDBParam("Device.DeviceInfo.ManufacturerOUI");
    }

    @Tr369Get("Device.DeviceInfo.SerialNumber")
    public String SK_TR369_GetSerialNumber() {
        return DeviceInfoUtils.getSerialNumber(); // about - Status - Serial Number
    }

    @Tr369Get("Device.DeviceInfo.ModelName,Device.DeviceInfo.ModelID,Device.DeviceInfo.ProductClass")
    public String SK_TR369_GetModelName() {
        return DeviceInfoUtils.getDeviceModel(); // about - Model
    }

    @Tr369Get("Device.DeviceInfo.TvName")
    public String SK_TR369_GetDeviceName() {
        return DeviceInfoUtils.getDeviceName(GlobalContext.getContext());
    }

    @Tr369Get("Device.DeviceInfo.ActiveFirmwareImage")
    public String SK_TR369_GetActiveFirmwareImage() {
        return SystemProperties.get("ro.boot.slot_suffix", "");
    }

    @Tr369Get("Device.DeviceInfo.BootFirmwareImage")
    public String SK_TR369_GetBootFirmwareImage(String path) {
        String active = SystemProperties.get("ro.boot.slot_suffix", "");
        return active.isEmpty() ? DbManager.getDBParam(path) : active;
    }

    @Tr369Get("Device.DeviceInfo.HardwareVersion")
    public String SK_TR369_GetHardwareVersion() {
        return Build.HARDWARE;
    }

    @Tr369Get("Device.DeviceInfo.SoftwareVersion")
    public String SK_TR369_GetSoftwareVersion() {
        long utc = SystemProperties.getLong("ro.build.date.utc", 1631947123L);
        return String.valueOf(utc * 1000L);
    }

    @Tr369Get("Device.DeviceInfo.DeviceStatus")
    public String SK_TR369_GetDeviceStatus() {
        return "Up";
    }

    @Tr369Get("Device.DeviceInfo.UpTime")
    public String SK_TR369_GetUpTime() {
        long time_s = SystemClock.elapsedRealtime() / 1000;
        return String.valueOf(time_s);
    }

    @Tr369Get("Device.DeviceInfo.FirstUseDate")
    public String SK_TR369_GetFirstUseDate(String path) {
        String firstUseDate = DbManager.getDBParam("Device.DeviceInfo.FirstUseDate");
        if (TextUtils.isEmpty(firstUseDate)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            firstUseDate = df.format(System.currentTimeMillis());
            DbManager.setDBParam("Device.DeviceInfo.FirstUseDate", firstUseDate);
            return firstUseDate;
        }
        return firstUseDate;
    }

    @Tr369Get("Device.DeviceInfo.mac")
    public String SK_TR369_GetDeviceMac() {
        String result = NetworkUtils.getEthernetMacAddress();
        if (TextUtils.isEmpty(result)) {
            result = NetworkUtils.getWifiMacAddress();
        }
        return result;
    }

    @Tr369Get("Device.DeviceInfo.IPAddress")
    public String SK_TR369_GetIpAddress(Context context) {
        String result = "";
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        Network net = cm.getActiveNetwork();
        if (net != null) {
            Log.d(TAG, "getActiveNetwork: " + net);
            LinkProperties prop = cm.getLinkProperties(net);
            if (prop == null) return result;
            Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
            // If there are no entries, return null
            if (!iter.hasNext()) return result;
            // Concatenate all available addresses, newline separated
            StringBuilder addresses = new StringBuilder();
            while (iter.hasNext()) {
                addresses.append(iter.next().getHostAddress());
                if (iter.hasNext()) addresses.append("\n");
            }
            result = addresses.toString();
        }
        return result;
    }

    @Tr369Get("Device.DeviceInfo.MACAddress")
    public String SK_TR369_GetMacAddress(Context context) {
        WifiManager wm = context.getSystemService(WifiManager.class);
        final String[] macAddresses = wm.getFactoryMacAddresses();
        String macAddress = null;
        if (macAddresses != null && macAddresses.length > 0) {
            macAddress = macAddresses[0];
        }
        return (macAddress != null) ? macAddress.toLowerCase() : "";
    }

}
