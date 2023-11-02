package com.sdt.diagnose.Device.DeviceInfo;

import android.text.TextUtils;
import android.util.Log;

import com.sdt.annotations.Tr369Get;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.IProtocolArray;
import com.sdt.diagnose.common.ProtocolPathUtl;
import com.sdt.diagnose.common.bean.ProcessInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ProcessInfoX implements IProtocolArray<ProcessInfo> {
    private static final String TAG = "ProcessInfoX";

    private final static String REFIX = "Device.DeviceInfo.ProcessStatus.Process.";

    @Tr369Get("Device.DeviceInfo.ProcessStatus.Process.")
    public String SK_TR369_GetProcessInfo(String path) {
        Log.d(TAG, "getProcessInfo: >>> path: " + path);
        return handleProcessInfoX(path);
    }

    private String handleProcessInfoX(String path) {
        return ProtocolPathUtl.getInfoFromArray(REFIX, path, this);
    }

    @Override
    public List<ProcessInfo> getArray() {
//        return CacheArrayManager.getInstance(GlobalContext.getContext()).getProcessInfo();
        return null;
    }

    @Override
    public String getValue(ProcessInfo t, @NotNull String[] paramsArr) {
        if (paramsArr.length < 2) {
            return null;
        }
        String secondParam = paramsArr[1];
        if (TextUtils.isEmpty(secondParam)) {
            //Todo report error.
            return null;
        }
        switch (secondParam) {
            case "PID":
                return String.valueOf(t.getPid());
            case "Command":
                return t.getCommand();
            case "Size":
                return String.valueOf(t.getSize());
            case "Priority":
                return String.valueOf(t.getPriority());
            case "CPUTime":
                return String.valueOf(t.getCpuTime());
            case "State":
                return t.getState();
            default:
                break;
        }
        return null;
    }
}
