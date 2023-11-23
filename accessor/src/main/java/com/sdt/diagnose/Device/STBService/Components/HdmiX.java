package com.sdt.diagnose.Device.STBService.Components;

import android.os.Build;
import android.util.Log;

import com.sdt.annotations.Tr369Get;
import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.Device.Platform.ModelX;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.extra.CmsExtraServiceManager;

import java.util.List;

/**
 * @Description: java类作用描述
 * @CreateDate: 2021/8/12 19:40
 */
public class HdmiX {
    private static final String TAG = "HdmiX";
    private static final int HDMI_NUMBER_ENTRIES = 1;
    CmsExtraServiceManager mCmsExtraServiceManager = CmsExtraServiceManager.getInstance(GlobalContext.getContext());
    private ModelX.Type mStbModelType = null;

    @Tr369Get("Device.Services.STBService.1.Components.HDMINumberOfEntries")
    public String SK_TR369_GetHdmiNumberEntries(String path) {
        Log.d(TAG, "GetHdmiNumberEntries path = " + path);
        return String.valueOf(HDMI_NUMBER_ENTRIES);
    }

    /**
     * /sys/class/amhdmitx/amhdmitx0/hpd_state
     *
     * @return
     */
    private boolean isHDMIPlugged() {
        // HDMI 插入状态
        boolean isPlugged = false;

        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            isPlugged = AmlHdmiX.getInstance().isHDMIPluggedByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            isPlugged = RtkHdmiX.getInstance().isHDMIPluggedByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    isPlugged = mCmsExtraServiceManager.isHdmiPlugged();
                } else {
                    Log.e(TAG, "isHDMIPlugged: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "isHDMIPlugged: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }

        return isPlugged;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.Enable")
    public String SK_TR369_GetHdmiEnable(String path) {
        String status = Boolean.toString(false);

        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            status = AmlHdmiX.getInstance().getHdmiEnableByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            status = RtkHdmiX.getInstance().getHdmiEnableByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    status = mCmsExtraServiceManager.getHdmiStatus();
                } else {
                    Log.e(TAG, "getHdmiEnable: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiEnable: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return status;
    }

    /**
     * @param path
     * @param val  -1:正常输出，1：关闭输出
     * @return
     */
    @Tr369Set("Device.Services.STBService.1.Components.HDMI.1.Enable")
    public boolean SK_TR369_SetHdmiEnable(String path, String val) {
        boolean isEnable = false;
        try {
            isEnable = "1".equals(val) || "true".equals(val);
        } catch (Exception e) {
            Log.e(TAG, "setHdmiEnable: parseBoolean failed, " + e.getMessage());
        }

        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            AmlHdmiX.getInstance().setHdmiEnableByAml(isEnable);
        } else if (mStbModelType == ModelX.Type.Realtek) {
            RtkHdmiX.getInstance().setHdmiEnableByRtk(isEnable);
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    mCmsExtraServiceManager.setHdmiStatus(isEnable);
                } else {
                    Log.e(TAG, "setHdmiEnable: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "setHdmiEnable: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }

        return true;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.Status")
    public String SK_TR369_GetHdmiStatus(String path) {
        return isHDMIPlugged() ? "Plugged" : "Unplugged";
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.ResolutionMode")
    public String SK_TR369_GetHdmiResolutionMode(String path) {
        if (!isHDMIPlugged()) return "";

        boolean isBest = false;
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            isBest = AmlHdmiX.getInstance().getHdmiResolutionModeByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            isBest = RtkHdmiX.getInstance().getHdmiResolutionModeByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    isBest = mCmsExtraServiceManager.isBestOutputMode();
                } else {
                    Log.e(TAG, "getHdmiResolutionMode: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiResolutionMode: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return isBest ? "Best" : "Manual";
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.Name")
    public String SK_TR369_GetHdmiName(String path) {
        if (!isHDMIPlugged()) return "";

        String name = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            name = AmlHdmiX.getInstance().getHdmiNameByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            name = RtkHdmiX.getInstance().getHdmiNameByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    name = mCmsExtraServiceManager.getHdmiProductName();
                } else {
                    Log.e(TAG, "getHdmiName: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiName: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return name;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.ResolutionValue")
    public String SK_TR369_GetHdmiResolutionValue(String path) {
        if (!isHDMIPlugged()) return "";

        String mode = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            mode = AmlHdmiX.getInstance().getHdmiResolutionValueByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            mode = RtkHdmiX.getInstance().getHdmiResolutionValueByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    mode = mCmsExtraServiceManager.getHdmiResolutionValue();
                } else {
                    Log.e(TAG, "getHdmiResolutionValue: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiResolutionValue: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return mode;
    }

    @Tr369Set("Device.Services.STBService.1.Components.HDMI.1.ResolutionValue")
    public Boolean SK_TR369_SetHdmiResolutionValue(String path, String value) {
        if (!isHDMIPlugged()) return false;

        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            return AmlHdmiX.getInstance().setHdmiResolutionValueByAml(value);
        } else if (mStbModelType == ModelX.Type.Realtek) {
            return RtkHdmiX.getInstance().setHdmiResolutionValueByRtk(value);
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    String supportList = mCmsExtraServiceManager.getHdmiSupportResolution();
                    if (!supportList.contains(value)) {
                        Log.e(TAG, "This resolution is not supported!");
                        return false;
                    }
                    mCmsExtraServiceManager.setHdmiResolutionValue(value);
                    return true;
                } else {
                    Log.e(TAG, "setHdmiResolutionValue: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "setHdmiResolutionValue: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }

        return false;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.SupportedResolutions")
    public String SK_TR369_GetHdmiDisplayDevSupportedResolutions(String path) {
        if (!isHDMIPlugged()) return "";

        String supportList = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            List<String> listHdmiMode = AmlHdmiX.getInstance().getHdmiSupportListByAml();
            if (Build.VERSION.SDK_INT > 30) {
                DisplayCapabilityManager.getInstance(GlobalContext.getContext()).filterNoSupportMode(listHdmiMode);
            }
            if (listHdmiMode.size() != 0)
                supportList = listHdmiMode.toString();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            List<String> listHdmiMode = RtkHdmiX.getInstance().getHdmiSupportListByRtk();
            if (listHdmiMode.size() != 0)
                supportList = listHdmiMode.toString();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    supportList = mCmsExtraServiceManager.getHdmiSupportResolution();
                } else {
                    Log.e(TAG, "getHdmiDisplayDevSupportedResolutions: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiDisplayDevSupportedResolutions: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return supportList;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.Status")
    public String SK_TR369_GetHdmiDisplayDevStatus(String path) {
        return isHDMIPlugged() ? "Present" : "None";
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.Name")
    public String SK_TR369_GetHdmiDisplayDevName(String path) {
        if (!isHDMIPlugged()) return "";

        String name = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            name = AmlHdmiX.getInstance().getHdmiNameByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            name = RtkHdmiX.getInstance().getHdmiNameByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    name = mCmsExtraServiceManager.getHdmiProductName();
                } else {
                    Log.e(TAG, "getHdmiDisplayDevName: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiDisplayDevName: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return name;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.EEDID")
    public String SK_TR369_GetHdmiDisplayDevEEDID(String path) {
        if (!isHDMIPlugged()) return "";

        String edid = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            edid = AmlHdmiX.getInstance().getHdmiEdidByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            edid = RtkHdmiX.getInstance().getHdmiEdidByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    edid = mCmsExtraServiceManager.getHdmiEdidVersion();
                } else {
                    Log.e(TAG, "getHdmiDisplayDevEEDID: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiDisplayDevEEDID: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return edid;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.PreferredResolution")
    public String SK_TR369_GetHdmiDisplayDevPreferredResolution(String path) {
        if (!isHDMIPlugged()) return "";

        String mode = "";
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            mode = AmlHdmiX.getInstance().getHdmiResolutionValueByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            mode = RtkHdmiX.getInstance().getHdmiResolutionValueByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    mode = mCmsExtraServiceManager.getHdmiResolutionValue();
                } else {
                    Log.e(TAG, "getHdmiDisplayDevPreferredResolution: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiDisplayDevPreferredResolution: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return mode;
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.CECSupport")
    public String SK_TR369_GetHdmiDisplayDevCECSupport(String path) {
        if (!isHDMIPlugged()) return "";

        boolean isSupport = false;
        if (mStbModelType == null) mStbModelType = ModelX.getPlatform();
        if (mStbModelType == ModelX.Type.Amlogic) {
            isSupport = AmlHdmiX.getInstance().getCapHdmiCecSupportByAml();
        } else if (mStbModelType == ModelX.Type.Realtek) {
            isSupport = RtkHdmiX.getInstance().getCapHdmiCecSupportByRtk();
        } else {
            try {
                if (null != mCmsExtraServiceManager) {
                    isSupport = mCmsExtraServiceManager.isHdmiCecSupport();
                } else {
                    Log.e(TAG, "getHdmiDisplayDevCECSupport: CmsExtraServiceManager is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getHdmiDisplayDevCECSupport: CmsExtraServiceManager call failed, " + e.getMessage());
            }
        }
        return Boolean.toString(isSupport);
    }

    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.HDMI3DPresent")
    public String SK_TR369_GetHdmiDisplayHDMI3DPresent(String path) {
        Log.d(TAG, "GetHdmiDisplayHDMI3DPresent path = " + path);
        //TODO amlogic 不支持
        return Boolean.FALSE.toString();
    }

    //    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.VideoLatency")
    public String SK_TR369_GetHdmiDisplayDevVideoLatency(String path) {
        Log.d(TAG, "GetHdmiDisplayDevVideoLatency path = " + path);
        //TODO amlogic 不支持
        return Boolean.FALSE.toString();
    }

    //    @Tr369Get("Device.Services.STBService.1.Components.HDMI.1.DisplayDevice.AutoLipSyncSupport")
    public String SK_TR369_GetHdmiDisplayDevAutoLipSyncSupport(String path) {
        Log.d(TAG, "GetHdmiDisplayDevAutoLipSyncSupport path = " + path);
        //TODO amlogic 不支持
        return Boolean.FALSE.toString();
    }
}
