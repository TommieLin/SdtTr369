package com.sdt.diagnose.Device;

import static android.content.Context.NETWORK_STATS_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.sdt.accessor.R;
import com.sdt.annotations.Tr369Get;
import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.Device.X_Skyworth.LockUnlockActivity;
import com.sdt.diagnose.Device.X_Skyworth.Log.bean.LogCmd;
import com.sdt.diagnose.Device.X_Skyworth.Log.bean.LogRepository;
import com.sdt.diagnose.Device.X_Skyworth.LogManager;
import com.sdt.diagnose.Device.X_Skyworth.SkyworthXManager;
import com.sdt.diagnose.Device.X_Skyworth.SystemDataStat;
import com.sdt.diagnose.DiagnoseServiceManager;
import com.sdt.diagnose.common.ApplicationUtil;
import com.sdt.diagnose.common.DeviceInfoUtil;
import com.sdt.diagnose.common.DreamBackend;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.NetWorkSpeedUtils;
import com.sdt.diagnose.common.NetworkUtils;
import com.sdt.diagnose.common.SPUtils;
import com.sdt.diagnose.common.TimeFormatUtil;
import com.sdt.diagnose.common.XshellClient;
import com.sdt.diagnose.common.bean.NotificationBean;
import com.sdt.diagnose.common.bean.SpeedTestBean;
import com.sdt.diagnose.common.bean.StandbyBean;
import com.sdt.diagnose.database.DbManager;
import com.skyworth.scrrtcsrv.Device;
import com.skyworth.scrrtcsrv.RemoteControlAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SkyworthX {
    private static final String TAG = "SkyworthX";

    @Tr369Get("Device.X_Skyworth.DeviceName")
    public String SK_TR369_GetDeviceName() {
        return DeviceInfoUtil.getDeviceName(GlobalContext.getContext()); // about - Device Name
    }

    @Tr369Get("Device.X_Skyworth.OperatorName")
    public String SK_TR369_GetOperatorName() {
        return DeviceInfoUtil.getOperatorName();
    }

    @Tr369Get("Device.X_Skyworth.BluetoothMac")
    public String SK_TR369_GetBluetoothMac() {
        return DeviceInfoUtil.getBluetoothMac(GlobalContext.getContext()); // about - Status - Bluetooth mac
    }

    @Tr369Get("Device.X_Skyworth.AndroidVersion")
    public String SK_TR369_GetAndroidVersion() {
        return DeviceInfoUtil.getDeviceFirmwareVersion(); // about - Version
    }

    @Tr369Get("Device.X_Skyworth.AndroidTVOS")
    public String SK_TR369_GetAndroidTVOS() {
        return DeviceInfoUtil.getBuildInfo(); // about - build
    }

    @Tr369Get("Device.X_Skyworth.PatchLevel")
    public String SK_TR369_GetPatchLevel() {
        return DeviceInfoUtil.getAndroidSecurityPatchLevel(); // about - Android security patch level
    }

    @Tr369Get("Device.X_Skyworth.ScreenSaver")
    public String SK_TR369_GetScreenSaver() {
        return DeviceInfoUtil.getScreenSaver(GlobalContext.getContext()); // about - screensaver
    }

    @Tr369Get("Device.X_Skyworth.InternalDataStorageFree")
    public String SK_TR369_GetStorageFree() {
        return SkyworthXManager.getInstance().getInternalDataStorageFree();
    }

    @Tr369Get("Device.X_Skyworth.InternalDataStorageTotal")
    public String SK_TR369_GetStorageTotal() {
        return SkyworthXManager.getInstance().getInternalDataStorageTotal();
    }

    @Tr369Get("Device.X_Skyworth.InternalDataStorageUtilisation")
    public String SK_TR369_GetStorageUtil() {
        return SkyworthXManager.getInstance().getInternalDataStorageUtilisation();
    }

    @Tr369Get("Device.X_Skyworth.MemoryUtilisation")
    public String SK_TR369_GetMemoryUtil() {
        return SkyworthXManager.getInstance().getMemoryUtilisation();
    }

    @Tr369Get("Device.X_Skyworth.ActiveNetworkConnection")
    public String SK_TR369_GetActiveNetworkConnection() {
        return NetworkUtils.getActiveNetworkType(GlobalContext.getContext());
    }

    @Tr369Get("Device.X_Skyworth.CpuTemperature")
    public String SK_TR369_GetCPUTemperature() {
        return SkyworthXManager.getInstance().getCpuTemperature();
    }

    @Tr369Get("Device.X_Skyworth.DownlinkRate")
    public String SK_TR369_GetDownlinkRate() {
        return String.valueOf(NetWorkSpeedUtils.calcDownSpeed());
    }

    @Tr369Get("Device.X_Skyworth.UplinkRate")
    public String SK_TR369_GetUplinkRate() {
        return String.valueOf(NetWorkSpeedUtils.calcUpSpeed());
    }

    @Tr369Get("Device.X_Skyworth.TotalRAMUsageCapacity")
    public String SK_TR369_GetTotalRAMUsageCapacity() {
        ActivityManager manager =
                (ActivityManager) GlobalContext.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        int percent = (int) ((info.totalMem - info.availMem) / (float) info.totalMem * 100);
        return String.valueOf(percent);
    }

    @Tr369Get("Device.X_Skyworth.Mute")
    public String SK_TR369_GetMuteStatus(String path) {
        AudioManager audioManager =
                (AudioManager) GlobalContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        boolean result = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        return String.valueOf(result ? 1 : 0);
    }

    @Tr369Set("Device.X_Skyworth.Mute")
    public boolean SK_TR369_SetMuteStatus(String path, String value) {
        try {
            if (Boolean.parseBoolean(value) || Integer.parseInt(value) == 1) {
                return SkyworthXManager.getInstance().mute();
            } else if ((! Boolean.parseBoolean(value)) || Integer.parseInt(value) == 0) {
                return SkyworthXManager.getInstance().unmute();
            }
        } catch (Exception e) {
            Log.e(TAG, "setMuteStatus: parse value error, " + e.getMessage());
        }
        return false;
    }

    @Tr369Set("Device.X_Skyworth.Volume")
    public boolean SK_TR369_SetVolume(String path, String volume) {
        Log.d(TAG, "setVolume volume: " + volume);
        int index = - 1;
        index = Integer.parseInt(volume);
        Log.d(TAG, "setVolume value: " + index);
        AudioManager audioManager =
                (AudioManager) GlobalContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        int flags = AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, flags);
        return true;
    }

    @Tr369Get("Device.X_Skyworth.Volume")
    public String SK_TR369_GetVolume(String path) {
        AudioManager audioManager =
                (AudioManager) GlobalContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return String.valueOf(vol);
    }

    @Tr369Get("Device.X_Skyworth.Lock.Enable")
    public String SK_TR369_GetLockEnable(String path) {
        return DbManager.getDBParam("Device.X_Skyworth.Lock.Enable");
    }

    @Tr369Set("Device.X_Skyworth.Lock.Enable")
    public boolean SK_TR369_SetLockEnable(String path, String value) {
        //value: 0->unlock, 1->lock
        DbManager.setDBParam("Device.X_Skyworth.Lock.Enable", value);
        SystemProperties.set("persist.sys.tr069.lock", value);
        boolean isEnable = "1".equals(value);
        String backgroundImageUrl = DbManager.getDBParam("Device.X_Skyworth.Lock.Background");
        return setLock(isEnable, backgroundImageUrl);
    }

    private boolean setLock(boolean toLock, String imageUrl) {
//        checkFloatPermission(GlobalContext.getContext());
        Intent intent = new Intent(GlobalContext.getContext(), LockUnlockActivity.class);
        if (toLock) {
            intent.putExtra("lockTip", "stb is locked");
            intent.putExtra("imageUrl", imageUrl);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            GlobalContext.getContext().startActivity(intent);
        } else {
            if (LockUnlockActivity.instance != null) {
                LockUnlockActivity.instance.finish();
            }
            Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
            mHomeIntent.addCategory(Intent.CATEGORY_HOME);
            mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            GlobalContext.getContext().startActivity(mHomeIntent);
        }
        return true;
    }

    public boolean checkFloatPermission(Context context) {
        AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOpsMgr == null)
            return false;
        int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                .getPackageName());
        return Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
    }

    @Tr369Get("Device.X_Skyworth.WiFiChannelBand")
    public String SK_TR369_GetWiFiChannelBand() {
        WifiManager mWifiManager = (WifiManager) GlobalContext.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = mWifiManager.getConnectionInfo(); // 已连接wifi信息
        if (info != null) {
            return String.valueOf(info.getFrequency()); // 单位: MHz
        }
        return null;
    }

    @Tr369Get("Device.X_Skyworth.WiFiSecureConnection")
    public String SK_TR369_GetWiFiSecureConnection() {
        return SkyworthXManager.getInstance().getWiFiSecureConnection();
    }

    @Tr369Get("Device.X_Skyworth.WifiPHYType")
    public String SK_TR369_GetWifiPHYType() {
        return SkyworthXManager.getInstance().getWifiPHYType();
    }

    @Tr369Get("Device.X_Skyworth.WiFiMIMOMode")
    public String SK_TR369_GetWiFiMIMOMode() {
        return SkyworthXManager.getInstance().getWiFiMIMOMode();
    }

    @Tr369Set("Device.X_Skyworth.UninstallApp")
    public boolean SK_TR369_UninstallApp(String path, String value) {
        return ApplicationUtil.uninstall(value);
    }

    @Tr369Set("Device.X_Skyworth.UninstallApps")
    public boolean SK_TR369_UninstallApps(String path, String value) {
        return ApplicationUtil.uninstallApps(value);
    }

    private static int count = 0;

    @Tr369Set("Device.X_Skyworth.LogStream.")
    public boolean SK_TR369_SetLogStreamParams(String path, String value) {
        String[] split = path.split("\\.");
        if (split.length > 3) {
            switch (split[3]) {
                case "Enable":
                    LogManager.getInstance().setEnable(Integer.parseInt(value));
                    if (value.equals("1")) count++;
                    break;
                case "UploadUrl":
                    LogManager.getInstance().setUrl(value);
                    count++;
                    break;
                case "Level":
                    LogManager.getInstance().setLogLevel(value);
                    count++;
                    break;
                case "Keyword":
                    LogManager.getInstance().setKeywords(value);
                    count++;
                    break;
                case "Period":
                    LogManager.getInstance().setInterval(Integer.parseInt(value));
                    count++;
                    break;
                case "TransactionId":
                    LogManager.getInstance().setTransactionId(value);
                    count += 2;
                    Log.e(TAG, "TransactionId count: " + count);
                    break;
            }
        }
        if (count >= 6) {
            count = 0;
            LogManager.getInstance().start();
        }
        if (LogManager.mLogThread != null
                && LogManager.mLogThread.isAlive()
                && LogManager.getInstance().isEnable() == 0) {
            LogManager.getInstance().stopLog();
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.LogStream.")
    public String SK_TR369_GetLogStreamParams(String path) {
        String[] split = path.split("\\.");
        if (split.length > 3) {
            switch (split[3]) {
                case "Enable":
                    return (LogManager.getInstance().isEnable() == 1) ? "1" : "0";
                case "UploadUrl":
                    return LogManager.getInstance().getUrl();
                case "Level":
                    return LogManager.getInstance().getLogLevel();
                case "Keyword":
                    return LogManager.getInstance().getKeywords();
                case "Period":
                    return String.valueOf(LogManager.getInstance().getInterval());
                case "TransactionId":
                    return LogManager.getInstance().getTransactionId();
            }
        }
        return "";
    }

    public static String REMOTE_CONTROL_UPLOAD_URL_SP_KEY = "Device.X_Skyworth.RemoteControl.UploadUrl";
    public static String REMOTE_CONTROL_FRAME_RATE_SP_KEY = "Device.X_Skyworth.RemoteControl.FrameRate";
    public static String REMOTE_CONTROL_ICE_SERVICES_SP_KEY = "Device.X_Skyworth.RemoteControl.IceServers";
    public static String REMOTE_CONTROL_DPI_WIDTH_SP_KEY = "Device.X_Skyworth.RemoteControl.DPI.Width";
    public static String REMOTE_CONTROL_DPI_HEIGHT_SP_KEY = "Device.X_Skyworth.RemoteControl.DPI.Height";

    public boolean setRemoteControlEnable(String path, String val) {
        if (Boolean.getBoolean(val) || Integer.parseInt(val) == 1) {
            if (checkRemoteControlParam()) {
                HashMap<String, String> params = new HashMap<>();
                params.put(GlobalContext.getContext().getString(R.string.socketio_url),
                        SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_UPLOAD_URL_SP_KEY));
                params.put(GlobalContext.getContext().getString(R.string.video_fps),
                        SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_FRAME_RATE_SP_KEY));
                params.put(GlobalContext.getContext().getString(R.string.ice_servers),
                        SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_ICE_SERVICES_SP_KEY));
                params.put(GlobalContext.getContext().getString(R.string.video_size),
                        String.format("%sx%s",
                                SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_DPI_WIDTH_SP_KEY),
                                SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_DPI_HEIGHT_SP_KEY)));
                params.put(GlobalContext.getContext().getString(R.string.dev_name),
                        DeviceInfoUtil.getSerialNumber());
                RemoteControlAPI.start(GlobalContext.getContext(), params);
                SPUtils.getInstance(GlobalContext.getContext()).put(path, "1");
                return true;
            }
        } else if (! Boolean.getBoolean(val) || Integer.parseInt(val) == 0) {
            RemoteControlAPI.stop(GlobalContext.getContext());
        }
        SPUtils.getInstance(GlobalContext.getContext()).put(path, "0");
        return true;
    }

    private boolean checkRemoteControlParam() {
        if (TextUtils.isEmpty(SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_UPLOAD_URL_SP_KEY))
                || TextUtils.isEmpty(SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_ICE_SERVICES_SP_KEY))) {
            return false;
        }
        if (TextUtils.isEmpty(SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_FRAME_RATE_SP_KEY))) {
            SPUtils.getInstance(GlobalContext.getContext()).put(REMOTE_CONTROL_FRAME_RATE_SP_KEY, "30");
        }
        if (TextUtils.isEmpty(SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_DPI_WIDTH_SP_KEY))
                || TextUtils.isEmpty(SPUtils.getInstance(GlobalContext.getContext()).getString(REMOTE_CONTROL_DPI_HEIGHT_SP_KEY))) {
            SPUtils.getInstance(GlobalContext.getContext()).put(REMOTE_CONTROL_DPI_WIDTH_SP_KEY, "1280");
            SPUtils.getInstance(GlobalContext.getContext()).put(REMOTE_CONTROL_DPI_HEIGHT_SP_KEY, "720");
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.RemoteControl.")
    public String SK_TR369_GetRemoteControlInfo(String path) {
        return SPUtils.getInstance(GlobalContext.getContext()).getString(path);
    }

    @Tr369Set("Device.X_Skyworth.RemoteControl.")
    public boolean SK_TR369_SetRemoteControlInfo(String path, String val) {
        if (path.endsWith(".Enable")) {
            return setRemoteControlEnable(path, val);
        } else {
            SPUtils.getInstance(GlobalContext.getContext()).put(path, val);
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.System.Setting.Use24HourFormat")
    public String SK_TR369_GetTimeFormat(String path) {
        return TimeFormatUtil.is24Hour(GlobalContext.getContext()) ? "1" : "0";
    }

    @Tr369Set("Device.X_Skyworth.System.Setting.Use24HourFormat")
    public boolean SK_TR369_SetTimeFormat(String path, String val) {
        TimeFormatUtil.set24HourAndTimeUpdated(GlobalContext.getContext(), "1".equals(val));
        return true;
    }

    @Tr369Set("Device.X_Skyworth.SpeedTest.")
    public boolean SK_TR369_SetSpeedTestParams(String path, String value) {
        if (path.contains("Enable")) {
            SpeedTestBean.getInstance().setEnable(value);
        } else if (path.contains("Url")) {
            SpeedTestBean.getInstance().setUrl(value);
        } else if (path.contains("TransactionId")) {
            SpeedTestBean.getInstance().setTransactionId(value);
            if (SpeedTestBean.getInstance().getEnable().equals("1")
                    && ! TextUtils.isEmpty(SpeedTestBean.getInstance().getUrl())
                    && ! TextUtils.isEmpty(SpeedTestBean.getInstance().getTransactionId())) {
                Log.d(TAG, "Wait to call bindSpeedTestService function");
                DiagnoseServiceManager.getInstance().bindSpeedTestService();
            }
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.SpeedTest.")
    public String SK_TR369_GetSpeedTestParams(String path) {
        if (path.contains("Enable")) {
            return SpeedTestBean.getInstance().getEnable();
        } else if (path.contains("Url")) {
            return SpeedTestBean.getInstance().getUrl();
        } else if (path.contains("TransactionId")) {
            return SpeedTestBean.getInstance().getTransactionId();
        }
        return "";
    }

    private static String remoteSshUrl;

    @Tr369Set("Device.X_Skyworth.RemoteSSH.")
    public Boolean SK_TR369_SetRemoteSSHParams(String path, String value) {
        if (path.contains("Enable")) {
            if ("1".equals(value)) {
                String serviceUrl = String.format("%s?userId=%s", remoteSshUrl, DeviceInfoUtil.getSerialNumber());
                XshellClient.start(serviceUrl);
            } else if ("0".equals(value)) {
                XshellClient.stop();
            } else {
                return false;
            }
        } else if (path.contains("IpPort")) {
            remoteSshUrl = value;
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.TotalBytes.Today")
    public String SK_TR369_GetTodayTotalBytes(String path) {
        NetworkStatsManager networkStatsManager =
                (NetworkStatsManager) GlobalContext.getContext().getSystemService(NETWORK_STATS_SERVICE);
        try {
            NetworkStats.Bucket wifi = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_WIFI, "",
                    getTodayZeroDate().getTime(), System.currentTimeMillis());

            NetworkStats.Bucket ethernet = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_ETHERNET, "",
                    getTodayZeroDate().getTime(), System.currentTimeMillis());
            long wifiUsage = (wifi == null) ? 0 : wifi.getRxBytes() + wifi.getTxBytes();
            long ethernetUsage = (ethernet == null) ? 0 : ethernet.getRxBytes() + ethernet.getTxBytes();
            return String.valueOf((wifiUsage + ethernetUsage) / (1024 * 1024));
        } catch (Exception e) {
            Log.e(TAG, "GetTodayTotalBytes error, " + e.getMessage());
            return null;
        }
    }

    @Tr369Get("Device.X_Skyworth.TotalBytes.TxTotal")
    public String SK_TR369_GetTodayTxTraffic(String path) {
        return String.valueOf(SystemDataStat.getTxTotalTraffic());
    }

    @Tr369Get("Device.X_Skyworth.TotalBytes.RxTotal")
    public String SK_TR369_GetTodayRxTraffic(String path) {
        return String.valueOf(SystemDataStat.getRxTotalTraffic());
    }

    public Date getTodayZeroDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    @Tr369Get("Device.X_Skyworth.Reboot.Numbers")
    public String SK_TR369_GetRebootNumbers(String path) {
        return SystemProperties.get("persist.sys.tr369.reboot.numbers", "");
    }

    @Tr369Get("Device.X_Skyworth.Array")
    public String SK_TR369_GetSystemStatArrayParams(String path) {
        return SystemDataStat.getSystemDataStatInfo();
    }

    @Tr369Set("Device.X_Skyworth.ArrayRecordInterval")
    public Boolean SK_TR369_SetSystemStatArrayRecordInterval(String path, String value) {
        SystemDataStat.setPeriodicMillisTime(value);
        return (DbManager.setDBParam("Device.X_Skyworth.ArrayRecordInterval", value) == 0);
    }

    @Tr369Get("Device.X_Skyworth.ArrayRecordInterval")
    public String SK_TR369_GetSystemStatArrayRecordInterval(String path) {
        return String.valueOf(SystemDataStat.getPeriodicMillisTime());
    }

    @Tr369Get("Device.X_Skyworth.ScreenInUse")
    public String SK_TR369_GetScreenStatus(String path) {
        return String.valueOf(Boolean.compare(Device.isScreenOn(), false));
    }

    @Tr369Get("Device.X_Skyworth.FTI.ResidenceDuration")
    public String SK_TR369_GetFTIResidenceDuration(String path) {
        return SystemProperties.get("persist.sys.tr369.FTI.residence.duration", "");
    }

    @Tr369Get("Device.X_Skyworth.MaxScreenRecordFileSize")
    public String SK_TR369_GetMaxUploadFileSize(String path) {
        return DbManager.getDBParam("Device.X_Skyworth.MaxScreenRecordFileSize");
    }

    @Tr369Set("Device.X_Skyworth.MaxScreenRecordFileSize")
    public Boolean SK_TR369_SetMaxUploadFileSize(String path, String value) {
        long maxFileSize = 10 * 1024 * 1024;
        if (value == null || value.length() == 0 || Long.parseLong(value) <= 0) {
            DbManager.setDBParam("Device.X_Skyworth.MaxScreenRecordFileSize", String.valueOf(maxFileSize));
        } else {
            DbManager.setDBParam("Device.X_Skyworth.MaxScreenRecordFileSize", value);
        }
        return true;
    }

    static final Map<String, DreamBackend.DreamInfo> mDreamInfos = new ArrayMap<>();
    public DreamBackend mBackend = new DreamBackend(GlobalContext.getContext());

    @Tr369Get("Device.X_Skyworth.DreamInfo")
    public String SK_TR369_GetDreamInfo(String path) {
        List<DreamBackend.DreamInfo> infos = mBackend.getDreamInfos();
        mDreamInfos.clear();
        for (final DreamBackend.DreamInfo info : infos) {
            final String caption = info.caption.toString();
            mDreamInfos.put(caption, info);
        }
        return Arrays.toString(mDreamInfos.keySet().toArray());
    }

    @Tr369Get("Device.X_Skyworth.CurrentDreamInfo")
    public String SK_TR369_GetCurrentDreamInfo(String path) {
        for (DreamBackend.DreamInfo info : mDreamInfos.values()) {
            if (info.isActive) {
                return info.caption.toString();
            }
        }
        return null;
    }

    @Tr369Set("Device.X_Skyworth.DreamInfo")
    public Boolean SK_TR369_SetActiveDream(String path, String caption) {
        DreamBackend mBackend = new DreamBackend(GlobalContext.getContext());
        final DreamBackend.DreamInfo dreamInfo = mDreamInfos.get(caption);
        if (dreamInfo != null) {
            if (dreamInfo.settingsComponentName != null) {
                Log.e(TAG, "settingsComponentName: " + dreamInfo.settingsComponentName);
                GlobalContext.getContext().startActivity(
                        new Intent().setComponent(dreamInfo.settingsComponentName));
            }
            if (! mBackend.isEnabled()) {
                mBackend.setEnabled(true);
            }
            if (! Objects.equals(mBackend.getActiveDream(), dreamInfo.componentName)) {
                mBackend.setActiveDream(dreamInfo.componentName);
            }
        } else {
            if (mBackend.isEnabled()) {
                mBackend.setActiveDream(null);
                mBackend.setEnabled(false);
            }
        }
        return true;
    }

    @Tr369Set("Device.X_Skyworth.DreamTime")
    public Boolean SK_TR369_SetDreamTime(String path, String ms) {
        return Settings.System.putInt(
                GlobalContext.getContext().getContentResolver(),
                SCREEN_OFF_TIMEOUT,
                Integer.parseInt(ms));
    }

    public static final int DEFAULT_DREAM_TIME_MS = (int) (30 * DateUtils.MINUTE_IN_MILLIS);

    @Tr369Get("Device.X_Skyworth.DreamTime")
    public String SK_TR369_GetDreamTime(String path) {
        return String.valueOf(Settings.System.getInt(
                GlobalContext.getContext().getContentResolver(),
                SCREEN_OFF_TIMEOUT,
                DEFAULT_DREAM_TIME_MS));
    }

    @Tr369Get("Device.X_Skyworth.StandbyMode")
    public String SK_TR369_GetStandbyMode(String path) {
        String value = SystemProperties.get("persist.sys.standby.mode", "");
        if (TextUtils.equals(value, "1")) {
            return "Shutdown";
        } else {
            return "Sleep";
        }
    }

    @Tr369Set("Device.X_Skyworth.StandbyMode")
    public boolean SK_TR369_SetStandbyMode(String path, String mode) {
        if (TextUtils.equals(mode, "Shutdown")) {
            SystemProperties.set("persist.sys.standby.mode", "1");
        } else if (TextUtils.equals(mode, "Sleep")) {
            SystemProperties.set("persist.sys.standby.mode", "0");
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.Standby.")
    public String SK_TR369_GetStandbyParams(String path) {
        if (path.contains("Url")) {
            return StandbyBean.getInstance().getUpdateUrl();
        } else if (path.contains("Enable")) {
            return StandbyBean.getInstance().isEnable() ? "1" : "0";
        }
        return null;
    }

    @Tr369Set("Device.X_Skyworth.Standby.")
    public boolean SK_TR369_SetStandbyParams(String path, String value) {
        if (path.contains("Url")) {
            StandbyBean.getInstance().setUpdateUrl(value);
            Context context = GlobalContext.getContext();
            DeviceInfoUtil.updateStandbyStatus(context);
        } else if (path.contains("Enable")) {
            StandbyBean.getInstance().setEnable(value);
        }
        return true;
    }

    private static final int DEFAULT_SLEEP_TIME_MS = (int) (20 * DateUtils.MINUTE_IN_MILLIS);

    @Tr369Get("Device.X_Skyworth.SleepTime")
    public String SK_TR369_GetAttentiveSleepTime() {
        int time = Settings.Secure.getInt(GlobalContext.getContext().getContentResolver(),
                Settings.Secure.SLEEP_TIMEOUT, DEFAULT_SLEEP_TIME_MS);
        Log.d(TAG, "Get sleep time: " + time);
        return String.valueOf(time);
    }

    @Tr369Set("Device.X_Skyworth.SleepTime")
    public void SK_TR369_SetAttentiveSleepTime(String ms) {
        Settings.Secure.putInt(GlobalContext.getContext().getContentResolver(),
                Settings.Secure.SLEEP_TIMEOUT, Integer.parseInt(ms));
    }

    @Tr369Get("Device.X_Skyworth.IsSleep")
    public String SK_TR369_GetIsSleep(String path) {
        PowerManager powerManager =
                (PowerManager) GlobalContext.getContext().getSystemService(Context.POWER_SERVICE);
        return String.valueOf(powerManager.isInteractive());
    }

    @Tr369Set("Device.X_Skyworth.IsSleep")
    public boolean SK_TR369_SetSleep(String path, String enabled) {
        PowerManager powerManager =
                (PowerManager) GlobalContext.getContext().getSystemService(Context.POWER_SERVICE);
        if (Boolean.parseBoolean(enabled)) {
            powerManager.wakeUp(SystemClock.uptimeMillis());
        } else {
            powerManager.goToSleep(SystemClock.uptimeMillis());
        }
        return true;
    }

    private static final List<String> sound = new ArrayList<String>() {{
        add("auto");
        add("never");
        add("always");
        add("manual");
    }};

    @Tr369Set("Device.X_Skyworth.Sound")
    public Boolean SK_TR369_SetSound(String path, String mode) {
        return Settings.Global.putInt(GlobalContext.getContext().getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT, sound.indexOf(mode));
    }

    @Tr369Get("Device.X_Skyworth.Sound")
    public String SK_TR369_GetSound(String path) {
        return sound.get(Settings.Global.getInt(GlobalContext.getContext().getContentResolver(),
                Settings.Global.ENCODED_SURROUND_OUTPUT, Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO));
    }

    @Tr369Set("Device.X_Skyworth.SoundSwitch")
    public boolean SK_TR369_SetSoundEffectsEnabled(String path, String enabled) {
        AudioManager mAudioManager = GlobalContext.getContext().getSystemService(AudioManager.class);
        boolean enable = Boolean.parseBoolean(enabled);
        if (enable) {
            mAudioManager.loadSoundEffects();
        } else {
            mAudioManager.unloadSoundEffects();
        }
        return Settings.System.putInt(GlobalContext.getContext().getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, enable ? 1 : 0);
    }

    @Tr369Get("Device.X_Skyworth.SoundSwitch")
    public String SK_TR369_GetSoundEffectsEnabled(String path) {
        return String.valueOf(Settings.System.getInt(GlobalContext.getContext().getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0);
    }

    @Tr369Get("Device.X_Skyworth.Logcat.FileList")
    public String SK_TR369_GetUploadLogList(String path) {
        return LogRepository.getLogRepository().getLogList();
    }

    @Tr369Set("Device.X_Skyworth.Logcat.DeleteFiles")
    public boolean SK_TR369_HandleDeleteLogFiles(String path, String value) {
        return LogRepository.getLogRepository().deleteLogFiles(value);
    }

    @Tr369Get("Device.X_Skyworth.Logcat.AutoUpload.Enable")
    public String SK_TR369_GetAutoUploadSwitchStatus(String path) {
        return LogRepository.getLogRepository().getAutoUploadStatus() ? "1" : "0";
    }

    @Tr369Set("Device.X_Skyworth.Logcat.AutoUpload.Enable")
    public boolean SK_TR369_SetAutoUploadSwitchStatus(String path, String value) {
        boolean isEnable = "1".equals(value);
        LogRepository.getLogRepository().setAutoUploadStatus(isEnable);
        return (DbManager.setDBParam("Device.X_Skyworth.Logcat.AutoUpload.Enable", value) == 0);
    }

    @Tr369Get("Device.X_Skyworth.Logcat.AutoUpload.Interval")
    public String SK_TR369_GetAutoUploadInterval(String path) {
        return String.valueOf(LogRepository.getLogRepository().getPeriodicMillisTime());
    }

    @Tr369Set("Device.X_Skyworth.Logcat.AutoUpload.Interval")
    public boolean SK_TR369_SetAutoUploadInterval(String path, String value) {
        LogRepository.getLogRepository().setPeriodicMillisTime(value);
        return (DbManager.setDBParam("Device.X_Skyworth.Logcat.AutoUpload.Interval", value) == 0);
    }

    @Tr369Get("Device.X_Skyworth.Logcat.AutoUpload.Url")
    public String SK_TR369_GetAutoUploadUrl(String path) {
        return LogRepository.getLogRepository().getAutoUploadUrl();
    }

    @Tr369Set("Device.X_Skyworth.Logcat.AutoUpload.Url")
    public boolean SK_TR369_SetAutoUploadUrl(String path, String value) {
        LogRepository.getLogRepository().setAutoUploadUrl(value);
        return (DbManager.setDBParam("Device.X_Skyworth.Logcat.AutoUpload.Url", value) == 0);
    }

    @Tr369Set("Device.X_Skyworth.Logcat.Background.Enable")
    public boolean SK_TR369_SetLogcatEnable(String path, String value) {
        DbManager.setDBParam("Device.X_Skyworth.Logcat.Background.Enable", value);
        boolean isEnable = "1".equals(value) || "true".equals(value);
        if (isEnable) {
            LogRepository.getLogRepository().startCommand(LogCmd.CatchLog, "sky_log_tr369_logcat.sh");
        } else {
            LogRepository.getLogRepository().stopCommand();
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.Logcat.Background.Enable")
    public String SK_TR369_GetLogcatEnable(String path) {
        return DbManager.getDBParam("Device.X_Skyworth.Logcat.Background.Enable");
    }

    @Tr369Set("Device.X_Skyworth.Notification.")
    public boolean SK_TR369_SetNotificationParams(String path, String value) {
        if (path.contains("Url")) {
            NotificationBean.getInstance().setUrl(value);
        }
        return true;
    }

    @Tr369Get("Device.X_Skyworth.Notification.")
    public String SK_TR369_GetNotificationParams(String path) {
        if (path.contains("Url")) {
            return NotificationBean.getInstance().getUrl();
        }
        return "";
    }

}
