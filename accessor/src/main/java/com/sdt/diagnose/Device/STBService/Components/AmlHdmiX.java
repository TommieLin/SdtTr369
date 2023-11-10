package com.sdt.diagnose.Device.STBService.Components;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import com.sdt.diagnose.common.GlobalContext;

import java.util.ArrayList;
import java.util.List;

public class AmlHdmiX {
    private static final String TAG = "AmlHdmiX";
    private static Context mContext;
    private static AmlHdmiX mAmlHdmiX;
    private final String[] HDMI_LIST = {
            "2160p60hz",
            "2160p50hz",
            "2160p30hz",
            "2160p25hz",
            "2160p24hz",
            "smpte24hz",
            "1080p60hz",
            "1080p50hz",
            "1080p24hz",
            "720p60hz",
            "720p50hz",
            "1080i60hz",
            "1080i50hz",
            "576p50hz",
            "480p60hz"
    };

    AmlHdmiX(Context context) {
        mContext = context;
    }

    public static AmlHdmiX getInstance(@NonNull Context context) {
        if (null == mAmlHdmiX) {
            mAmlHdmiX = new AmlHdmiX(context);
        }
        return mAmlHdmiX;
    }

    public static AmlHdmiX getInstance() {
        if (null == mAmlHdmiX) {
            mAmlHdmiX = new AmlHdmiX(GlobalContext.getContext());
        }
        return mAmlHdmiX;
    }

    public boolean isHDMIPluggedByAml() {
        boolean isPlugged = false;
        try {
            isPlugged = OutputModeManager.getInstance(mContext).isHDMIPlugged();
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "isHDMIPluggedByAml: OutputModeManager call failed, " + e.getMessage());
        }
        return isPlugged;
    }

    public String getHdmiEnableByAml() {
        String status = Boolean.toString(false);
        try {
            String ret = SystemControlManager.getInstance().readSysFs("/sys/class/amhdmitx/amhdmitx0/avmute");
            status = Boolean.toString("-1".equals(ret));
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiEnableByAml: SystemControlManager call failed, " + e.getMessage());
        }
        return status;
    }

    public void setHdmiEnableByAml(boolean isEnable) {
        try {
            SystemControlManager.getInstance().writeSysFs("/sys/class/amhdmitx/amhdmitx0/avmute",
                    isEnable ? "-1" : "1");
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "setHdmiEnableByAml: SystemControlManager call failed, " + e.getMessage());
        }
    }

    public boolean getHdmiResolutionModeByAml() {
        boolean isBest = false;
        try {
            isBest = OutputModeManager.getInstance(mContext).isBestOutputmode();
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiResolutionModeByAml: OutputModeManager call failed, " + e.getMessage());
        }
        return isBest;
    }

    public String getHdmiNameByAml() {
        String name = "";
        try {
            String readStr = SystemControlManager.getInstance().readSysFs("/sys/class/amhdmitx/amhdmitx0/edid");
            if (! readStr.isEmpty()) {
                name = readStr.split("Rx Product Name: ")[1].split("Manufacture Week")[0];
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiNameByAml: SystemControlManager call failed, " + e.getMessage());
        }
        return name;
    }

    public String getHdmiResolutionValueByAml() {
        String mode = "";
        try {
            mode = OutputModeManager.getInstance(mContext).getCurrentOutputMode();
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiResolutionValueByAml: OutputModeManager call failed, " + e.getMessage());
        }
        if (mode.equals("dummy_l")) {
            return "";
        }
        return mode;
    }

    public boolean setHdmiResolutionValueByAml(String value) {
        List<String> listHdmiMode = getHdmiSupportListByAml();
        if (! listHdmiMode.contains(value)) {
            Log.e(TAG, "This resolution is not supported!");
            return false;
        }

        try {
            if (Build.VERSION.SDK_INT <= 30) {
                OutputModeManager.getInstance(mContext).setBestMode(value);
                String currentOutputMode = OutputModeManager.getInstance(mContext).getCurrentOutputMode();
                return currentOutputMode.equals(value);
            } else {
                String nextOutputMode;
                if (value.length() != 0) {
                    nextOutputMode = value;
                } else {
                    nextOutputMode = OutputModeManager.getInstance(mContext).getHighestMatchResolution();
                }
                DisplayCapabilityManager.getInstance(mContext).setResolutionAndRefreshRateByMode(nextOutputMode);
                return true;
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "setHdmiResolutionValueByAml: OutputModeManager call failed, " + e.getMessage());
        }

        return false;
    }

    @NonNull
    public List<String> getHdmiSupportListByAml() {
        String list = "";
        try {
            list = OutputModeManager.getInstance(mContext).getHdmiSupportList();
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiSupportListByAml: OutputModeManager call failed, " + e.getMessage());
        }

        Log.d(TAG, "getHdmiSupportList: " + list);

        List<String> listHdmiMode;
        if (list != null && list.length() != 0 && ! list.contains("null")) {
            final List<String> edidKeyList = new ArrayList<>();
            for (String mode : HDMI_LIST) {
                if (list.contains(mode)) {
                    edidKeyList.add(mode);
                }
            }
            listHdmiMode = edidKeyList;
        } else {
            listHdmiMode = new ArrayList<>();
        }
        return listHdmiMode;
    }

    public String getHdmiEdidByAml() {
        String edid = "";
        try {
            String readStr = SystemControlManager.getInstance().readSysFs("/sys/class/amhdmitx/amhdmitx0/edid");
            if (! readStr.isEmpty()) {
                String version = readStr.split("EDID Version: ")[1].split("EDID block number")[0];
                Log.d(TAG, "getHdmiEdidByAml: EDID Version " + version);
                if (null != version) {
                    edid = "EDID Version:" + version/*.replace("\n", " ")*/;
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getHdmiEdidByAml: SystemControlManager call failed, " + e.getMessage());
        }
        return edid;
    }

    public boolean getCapHdmiCecSupportByAml() {
        String readStr = "";
        //该节点的读取会有selinux权限的问题，需要修改selinux的权限
        try {
            readStr = SystemControlManager.getInstance().readSysFs("/sys/class/cec/pin_status");
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getCapHdmiCecSupportByAml: SystemControlManager call failed, " + e.getMessage());
        }

        if (null != readStr) {
            return readStr.contains("ok");
        }
        return false;
    }

}
