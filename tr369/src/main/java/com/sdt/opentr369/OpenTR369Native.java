package com.sdt.opentr369;

import android.util.Log;

import com.sdt.diagnose.Tr369PathInvoke;

public class OpenTR369Native {
    static {
        System.loadLibrary("sk_tr369_jni");
    }

    private final static String TAG = "OpenTR369Native";

    public static String OpenTR369CallbackGet(int what, String name, String val) {
        String ret = Tr369PathInvoke.getInstance().getAttribute(what, name);
        String str = "<-- what: " + what;
        if (name != null) {
            str = str + ", name: " + name;
        }
        if (val != null) {
            str = str + ", value: " + val;
        }
        str = str + ", result: " + ret;
        Log.d(TAG, "J OpenTR369CallbackGet " + str);

        return ret;
    }

    public static int OpenTR369CallbackSet(int what, String name, String val, String str3) {
        String str = "--> what: " + what;
        if (name != null) {
            str = str + ", name: " + name;
        }
        if (val != null) {
            str = str + ", value: " + val;
        }
        if (str3 != null) {
            str = str + ", param: " + str3;
        }
        Log.d(TAG, "J OpenTR369CallbackSet " + str);

//        if ("skyworth.tr369.event".equals(name)) {
//            if ("tr069_ping".equals(val) || "tr069_traceroute".equals(val)) {
//                val = val + "###" + str3;
//            }
//        }
        return Tr369PathInvoke.getInstance().setAttribute(what, name, val) ? 0 : -1;
    }

    public static String OpenTR369CallbackGetAttr(String path, String method) {
        String reply = Tr369PathInvoke.getInstance().getAttribute(0, path);
        String str = "-->: reply: " + reply;
        if (path != null) {
            str = str + ", path: " + path;
        }
        if (method != null) {
            str = str + ", method: " + method;
            Log.d(TAG, "J OpenTR369CallbackGetAttr" + str);
        }
        return reply;
    }

    public static int OpenTR369CallbackSetAttr(String path, String method, String value) {
        String str = "--> ";
        if (path != null) {
            str = str + "path: " + path;
        }
        if (method != null) {
            str = str + ", method: " + method;
        }
        if (value != null) {
            str = str + ", value: " + value;
        }
        Log.d(TAG, "J OpenTR369CallbackSetAttr " + str);
        return Tr369PathInvoke.getInstance().setAttribute(0, path, value) ? 0 : -1;
    }

    public static native String stringFromJNI();
    public static native int OpenTR369Init(String path);
    public static native int SetInitFilePath(String db_path, String default_path);
    public static native String GetDBFilePath();
    public static native String GetDefaultFilePath();

}
