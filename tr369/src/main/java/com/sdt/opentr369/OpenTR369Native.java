package com.sdt.opentr369;

public class OpenTR369Native {
    static {
        System.loadLibrary("sk_tr369_jni");
    }

    public static native String stringFromJNI();
    public static native int OpenTR369Init(String path);
    public static native int SetDBFilePath(String path);
    public static native String GetDBFilePath();

}
