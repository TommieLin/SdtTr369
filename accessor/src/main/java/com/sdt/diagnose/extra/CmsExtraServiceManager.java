package com.sdt.diagnose.extra;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sdt.sdtcmsextra.ICmsExtraService;

public class CmsExtraServiceManager {
    private static final String TAG = "CmsExtraServiceManager";
    private static ICmsExtraService mCmsExtraService;
    private static Context mContext;
    private static CmsExtraServiceManager mCmsExtraServiceManager;
    private final static String CMS_EXTRA_SERVICE_ACTION = "com.sdt.sdtcmsextra.callback";
    private final static String CMS_EXTRA_SERVICE_PACKAGE_NAME = "com.sdt.sdtcmsextra";

    private CmsExtraServiceManager() {
        connectCmsExtraService();
    }

    private void connectCmsExtraService() {
        try {
            Intent intent = buildCmsExtraServiceIntent();
            Log.d(TAG, "Begin to bindService " + intent.getPackage() + " with action " + intent.getAction());
            boolean ret = mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
            if (ret) {
                Log.d(TAG, "BindService finished");
            }
        } catch (Exception e) {
            Log.e(TAG, "CmsExtraService build failed, " + e.getMessage());
        }
    }

    @NonNull
    private Intent buildCmsExtraServiceIntent() {
        Intent intent = new Intent();
        intent.setAction(CMS_EXTRA_SERVICE_ACTION);
        intent.setPackage(CMS_EXTRA_SERVICE_PACKAGE_NAME);
        return intent;
    }

    public static CmsExtraServiceManager getInstance(@NonNull Context context) {
        if (null == mCmsExtraServiceManager || null == mCmsExtraService) {
            synchronized (CmsExtraServiceManager.class) {
                Log.d(TAG, "Binding failed, we need to renew a service manager");
                mContext = context;
                mCmsExtraServiceManager = new CmsExtraServiceManager();
            }
        }
        return mCmsExtraServiceManager;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ICmsExtraService connected");
            mCmsExtraService = ICmsExtraService.Stub.asInterface(service);
            try {
                mCmsExtraService.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "mCmsExtraService linkToDeath failed, " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "ICmsExtraService Disconnected");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(TAG, "ICmsExtraService BindingDied");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(TAG, "ICmsExtraService NullBinding");
        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            synchronized (CmsExtraServiceManager.class) {
                Log.e(TAG, "CmsExtraService Died");
                mCmsExtraService.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
                mCmsExtraService = null;
                connectCmsExtraService();
            }
        }
    };

    public double getCpuUsage() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        double ret = 0;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getCpuUsage();
                } else {
                    Log.e(TAG, "getCpuUsageRate: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getCpuUsageRate execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public double getCpuTemp() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        double ret = 0;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getCpuTemp();
                } else {
                    Log.e(TAG, "getCpuTemperature: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getCpuTemperature execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public boolean isHdmiPlugged() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        boolean ret = false;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.isHdmiPlugged();
                } else {
                    Log.e(TAG, "isHDMIPlugged: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "isHDMIPlugged execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public String getHdmiStatus() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        String ret = "";
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.isHdmiEnabled() ? "Enabled" : "Disabled";
                } else {
                    Log.e(TAG, "getHDMIStatus: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getHDMIStatus execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public int setHdmiStatus(boolean isEnable) {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        int ret = 0;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.setHdmiStatus(isEnable);
                } else {
                    Log.e(TAG, "setHDMIStatus: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "setHDMIStatus execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public String getHdmiSupportResolution() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        String ret = "";
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getHdmiSupportResolution();
                } else {
                    Log.e(TAG, "getHdmiSupportResolution: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getHdmiSupportResolution execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public String getHdmiResolutionValue() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        String ret = "";
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getHdmiResolutionValue();
                } else {
                    Log.e(TAG, "getHdmiResolutionValue: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getHdmiResolutionValue execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public int setHdmiResolutionValue(String mode) {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        int ret = 0;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.setHdmiResolutionValue(mode);
                } else {
                    Log.e(TAG, "setHdmiResolutionValue: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "setHdmiResolutionValue execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public boolean isBestOutputMode() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        boolean ret = false;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.isBestOutputMode();
                } else {
                    Log.e(TAG, "isBestOutputmode: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "isBestOutputmode execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public boolean isHdmiCecSupport() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        boolean ret = false;
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.isHdmiCecSupport();
                } else {
                    Log.e(TAG, "isHdmiCecSupport: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "isHdmiCecSupport execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public String getHdmiProductName() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        String ret = "";
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getHdmiProductName();
                } else {
                    Log.e(TAG, "getHdmiProductName: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getHdmiProductName execution failed, " + e.getMessage());
            }
        }
        return ret;
    }

    public String getHdmiEdidVersion() {
        if (null == mCmsExtraService) {
            throw new NullPointerException("mCmsExtraService is null");
        }

        String ret = "";
        synchronized (CmsExtraServiceManager.class) {
            try {
                if (null != mCmsExtraService) {
                    ret = mCmsExtraService.getHdmiEdidVersion();
                } else {
                    Log.e(TAG, "getHdmiEdidVersion: mCmsExtraService is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "getHdmiEdidVersion execution failed, " + e.getMessage());
            }
        }
        return ret;
    }
}
