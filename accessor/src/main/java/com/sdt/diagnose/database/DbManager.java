package com.sdt.diagnose.database;

import com.sdt.opentr369.OpenTR369Native;

public class DbManager {

    public static String getDBParam(String path) {
        return OpenTR369Native.GetDBParam(path);
    }

    public static int setDBParam(String path, String value) {
        return OpenTR369Native.SetDBParam(path, value);
    }

    public static int showData(String cmd) {
        return OpenTR369Native.ShowData(cmd);
    }
}
