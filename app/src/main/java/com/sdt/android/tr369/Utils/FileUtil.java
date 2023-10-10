package com.sdt.android.tr369.Utils;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class FileUtil {

    private static final String TAG = "FileUtil";
    public static final String PLATFORM_TMS_TR369_MODEL = "sdt_tms_tr369_model.txt";
    public static final String SYS_PROP_TR369_MODE_ISUPDATED = "persist.sys.tr369.mode.isupdated";

    public static void copyTr369ModelToFile(Context context) {
        String modelFileName = PLATFORM_TMS_TR369_MODEL;
        File modelFile = getTr369ModelFile(context, modelFileName);
        if (modelFile != null && !modelFile.exists()) {
            copyAssetFile(context, modelFileName, modelFile);
        } else if (modelFile != null && modelFile.exists()) {
            /* 判断model是否已经更新过了 */
            boolean isUpdated = SystemProperties.getBoolean(SYS_PROP_TR369_MODE_ISUPDATED, false);
            if (!isUpdated || "eng".equalsIgnoreCase(Build.TYPE)) {
                SystemProperties.set(SYS_PROP_TR369_MODE_ISUPDATED, Boolean.TRUE.toString());
                modelFile.delete();
                copyAssetFile(context, modelFileName, modelFile);
            }
        }
    }

    public static File getTr369ModelFile(Context context, String filePath) {
        if (context == null) return null;
        return new File(context.getDataDir(), filePath);
    }

    private static void copyAssetFile(Context context, String inFileName, File outFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(inFileName);
            outputStream = Files.newOutputStream(outFile.toPath());
            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }
            outputStream.flush();
            chmod(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void chmod(File outFileName) throws IOException {
        Path path = Paths.get(outFileName.getPath());
        Set perms = Files.readAttributes(path, PosixFileAttributes.class).permissions();
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(path, perms);
    }

}
