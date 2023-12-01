package com.sdt.diagnose.Device.X_Skyworth.App;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.SuspendDialogInfo;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.sdt.accessor.R;
import com.sdt.annotations.Tr369Get;
import com.sdt.annotations.Tr369Set;
import com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.AppPermissionControl;
import com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.model.AppPermissionGroup;
import com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.model.Permission;
import com.sdt.diagnose.common.ApplicationUtils;
import com.sdt.diagnose.common.AppsManager;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.IProtocolArray;
import com.sdt.diagnose.common.ProtocolPathUtils;
import com.sdt.diagnose.common.bean.AppInfo;
import com.sdt.diagnose.common.log.LogUtils;
import com.sdt.diagnose.database.DbManager;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class for protocol:
 * Device.X_Skyworth.App.{i}.Type               - 预制(System)\安装(ThirdParty)
 * Device.X_Skyworth.App.{i}.isUpdatedSystem    - 1(系统apk升级)\0（其他apk）
 * Device.X_Skyworth.App.{i}.BlockStatus        - disable \enable
 * Device.X_Skyworth.App.{i}.BlockEnable        - can disable\enable
 * Device.X_Skyworth.App.{i}.PackageName        - 包名
 * Device.X_Skyworth.App.{i}.Size               - 占用空间大小. apk + cache + data
 * Device.X_Skyworth.App.{i}.Version            - 版本
 * Device.X_Skyworth.App.{i}.Name               - 应用名称
 * Device.X_Skyworth.App.{i}.Running            - 是否在运行
 * Device.X_Skyworth.App.{i}.LastUpdatedTime    - 升级时间
 */
public class AppX implements IProtocolArray<AppInfo> {
    private static final String TAG = "AppX";
    private final static String REFIX = "Device.X_Skyworth.App.";
    private static final ArrayMap<String, AppPermissionGroup> mPermissionNameToGroup = new ArrayMap<>();
    private static ArrayList<AppPermissionGroup> mGroups = new ArrayList<>();
    public long firstTime = 0;
    public long totalFirstTime = 0;
    public UsageStatsManager usm =
            (UsageStatsManager) GlobalContext.getContext().getSystemService(Context.USAGE_STATS_SERVICE);
    public List<UsageStats> list;
    public List<UsageStats> totalList;
    final static Map<String, ArrayList<AppPermissionGroup>> appPermissionGroup = new HashMap<>();
    private static AppsManager mAppsManager = null;

    public static void updateAppList() {
        if (mAppsManager != null) {
            if (!mAppsManager.isEmpty()) {
                DbManager.delMultiObject("Device.X_Skyworth.App");
                mAppsManager.clear();
            }
            mAppsManager = null;
        }
        mAppsManager = new AppsManager(GlobalContext.getContext());
        int size = mAppsManager.getList().size();
        LogUtils.d(TAG, "Get the number of App List: " + size);
        if (size > 0) {
            DbManager.addMultiObject("Device.X_Skyworth.App", size);
            for (int i = 0; i < size; i++) {
                AppInfo appInfo = mAppsManager.getList().get(i);
                addPermission(appInfo, String.valueOf(i + 1), false);
            }
        }
    }

    @Tr369Get("Device.X_Skyworth.App.")
    public String SK_TR369_GetAppInfo(String path) {
        return handleAppPath(path);
    }

    private String handleAppPath(String path) {
        return ProtocolPathUtils.getInfoFromArray(REFIX, path, this);
    }

    @Override
    public List<AppInfo> getArray() {
        if (mAppsManager == null) {
            mAppsManager = new AppsManager(GlobalContext.getContext());
        }
        return mAppsManager.getList();
    }

    public static void addPermission(AppInfo t, @NotNull String paramsArr, boolean isAdded) {
        mPermissionNameToGroup.clear();
        mGroups = new ArrayList<>();
        PackageInfo packageInfo = t.getPackageInfo();
        try {
            packageInfo = GlobalContext.getContext().createPackageContextAsUser(
                            packageInfo.packageName, 0,
                            UserHandle.getUserHandleForUid(packageInfo.applicationInfo.uid))
                    .getPackageManager().getPackageInfo(packageInfo.packageName,
                            PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "Failed to add permission information. Error: " + e.getMessage());
        }

        if (packageInfo.requestedPermissions != null) {
            for (String requestedPerm : packageInfo.requestedPermissions) {
                if (mPermissionNameToGroup.get(requestedPerm) == null) {
                    AppPermissionGroup group = AppPermissionGroup.create(
                            GlobalContext.getContext(),
                            packageInfo,
                            requestedPerm,
                            false);
                    if (group == null) {
                        continue;
                    }
                    mGroups.add(group);
                    appPermissionGroup.put(paramsArr, mGroups);
                    addAllPermissions(group);
                    AppPermissionGroup backgroundGroup = group.getBackgroundPermissions();
                    if (backgroundGroup != null) {
                        addAllPermissions(backgroundGroup);
                    }
                }
            }
        }
        String path = "Device.X_Skyworth.App." + paramsArr + ".Permissions";
        LogUtils.d(TAG, "addPermission path: " + path + ", mGroups.size(): " + mGroups.size());
        if (mGroups.size() > 0 && !isAdded) {
            DbManager.addMultiObject(path, mGroups.size());
        }
    }

    private static void addAllPermissions(AppPermissionGroup group) {
        ArrayList<Permission> perms = group.getPermissions();
        int numPerms = perms.size();
        for (int permNum = 0; permNum < numPerms; permNum++) {
            mPermissionNameToGroup.put(perms.get(permNum).getName(), group);
        }
    }

    @Override
    public String getValue(AppInfo t, @NotNull String[] paramsArr) {
        if (paramsArr.length < 2) {
            return null;
        }
        String secondParam = paramsArr[1];
        String thirdParam = paramsArr.length >= 3 ? paramsArr[2] : "";
        String forthParam = paramsArr.length >= 4 ? paramsArr[3] : "";
        if (TextUtils.isEmpty(secondParam)) {
            //Todo report error.
            return null;
        }
        switch (secondParam) {
            case "Type":
                return t.isSystem() ? "System" : "ThirdParty";
            case "isUpdatedSystem":
                LogUtils.d(TAG, "isUpdatedSystem: " + t.isUpdatedSystem());
                return t.isUpdatedSystem() ? "1" : "0";
            case "PackageName":
                return t.getPackageName();
            case "Name":
                return t.getName();
            case "Version":
                return t.getVersion();
            case "Running":
                return t.isRunning() ? "1" : "0";
            case "BlockStatus":
                return t.isEnable() ? "1" : "0";
            case "BlockEnable":
                return t.isCanShowEnable() ? "1" : "0";
            case "Size":
                return String.valueOf(t.getTotalSize());
            case "LastUpdatedTime":
                return t.getLastUpdateTime();
            case "StopEnable":
                return t.isCanStop() ? "1" : "0";
            case "MemoryUsage":
                return t.isRunning() ? getMemoryUsed(t.getPackageName()).trim() : "0";
            case "StorageUsage":
                return String.valueOf(t.getStorageUsed());
            case "Uptime":
                return getUsageStats(t.getPackageName(), GlobalContext.getContext()) != null
                        ? String.valueOf(
                        getUsageStats(
                                t.getPackageName(),
                                GlobalContext.getContext()).getTotalTimeInForeground() / 1000) : "0";
            case "TotalUptime":
                return getTotalUsageStats(t.getPackageName()) != null
                        ? String.valueOf(getTotalUsageStats(
                        t.getPackageName()).getTotalTimeInForeground() / 1000) : "0";
            case "RunningTimes":
                return getUsageStats(t.getPackageName(), GlobalContext.getContext()) != null
                        ? String.valueOf(getUsageStats(
                        t.getPackageName(), GlobalContext.getContext()).mLaunchCount) : "0";
            case "LastStartTime":
                return getUsageStats(t.getPackageName(), GlobalContext.getContext()) != null
                        ? String.valueOf(
                        getUsageStats(
                                t.getPackageName(), GlobalContext.getContext()).getLastTimeStamp()) : "0";
            case "CpuUsage":
                return getCpuUsed(t.getPackageName()).trim();
            case "ClearData":
                return "0";
            case "PermissionsNumberOfEntries":
                return String.valueOf(mGroups.size());
            case "Permissions":
                if (TextUtils.isEmpty(thirdParam)) {
                    //Todo report error.
                    return null;
                }
                try {
                    switch (forthParam) {
                        case "Name":
                            return Objects.requireNonNull(appPermissionGroup.get(paramsArr[0]))
                                    .get(Integer.parseInt(thirdParam) - 1)
                                    .getName();
                        case "Label":
                            return Objects.requireNonNull(appPermissionGroup.get(paramsArr[0]))
                                    .get(Integer.parseInt(thirdParam) - 1)
                                    .getLabel().toString();
                        case "Granted":
                            return Objects.requireNonNull(appPermissionGroup.get(paramsArr[0]))
                                    .get(Integer.parseInt(thirdParam) - 1)
                                    .areRuntimePermissionsGranted() + "";
                        case "CanModify":
                            return (!Objects.requireNonNull(appPermissionGroup.get(paramsArr[0]))
                                    .get(Integer.parseInt(thirdParam) - 1)
                                    .isSystemFixed()
                                    && !Objects.requireNonNull(
                                            appPermissionGroup.get(paramsArr[0]))
                                    .get(Integer.parseInt(thirdParam) - 1)
                                    .isPolicyFixed()) + "";
                        default:
                            break;
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, "Failed to obtain permission information. Error: " + e.getMessage());
                    return null;
                }
        }
        return null;
    }

    @Tr369Set("Device.X_Skyworth.App.")
    public boolean SK_TR369_HandleAppInfoSetCmd(String path, String value) {
        LogUtils.d(TAG, "Set the path for app information: " + path);
        AppInfo appInfo = getAppByPath(path);
        if (appInfo == null) return false;

        String pkg = appInfo.getPackageName();
        if (TextUtils.isEmpty(pkg))
            return false;

        if (path.endsWith(".BlockStatus")) {
            if (TextUtils.equals(value, "0")) {
                return ApplicationUtils.ableApplication(pkg, false);
            } else if (TextUtils.equals(value, "1")) {
                return ApplicationUtils.ableApplication(pkg, true);
            }
        } else if (path.endsWith(".Running")) {
            if (DbManager.getDBParam("Device.X_Skyworth.Lock.Enable").equals("1")) {
                return false;
            }
            if (TextUtils.equals(value, "0")) {
                if (pkg.contains("tr069") || pkg.contains("diagnose") || pkg.contains("tr369"))
                    return true;
                return ApplicationUtils.forceStopApp(pkg);
            } else if (TextUtils.equals(value, "1")) {
                return ApplicationUtils.openApp(pkg);
            }
        } else if (path.endsWith(".ClearData")) {
            if (TextUtils.equals(value, "1")) {
                if (pkg.contains("tr069") || pkg.contains("tr369") || pkg.contains("diagnose")) {
                    return true;
                }
                return ApplicationUtils.clearData(pkg);
            }
        } else if (path.contains("Permissions")) {
            if (path.endsWith("Granted")) {
                try {
                    String[] paths = path.split("\\.");
                    boolean success =
                            AppPermissionControl.changeRuntimePermissions(
                                    GlobalContext.getContext(), pkg, Objects.requireNonNull(
                                            appPermissionGroup.get(path.split("\\.")[3])).get(
                                            Integer.parseInt(paths[5]) - 1).getName(),
                                    Boolean.parseBoolean(value));
                    addPermission(appInfo, paths[3], true);
                    return success;
                } catch (Exception e) {
                    LogUtils.e(TAG, "Failed to set permission information. Error: " + e.getMessage());
                }
            }
        }
        return false;
    }

    private ArrayList<String> getBlockListPkgNames() {
        String listFromDBParam = DbManager.getDBParam("Device.X_Skyworth.BlockListPkgNames");
        return (ArrayList<String>) ApplicationUtils.parseStringList(listFromDBParam);
    }

    private void setBlockListPkgNames(ArrayList<String> list) {
        String listToDBParam = list.toString();
        DbManager.setDBParam("Device.X_Skyworth.BlockListPkgNames", listToDBParam);
    }

    @Tr369Get("Device.X_Skyworth.AppBatchBlock")
    public String SK_TR369_GetAppBatchBlock(String path) {
        return new Gson().toJson(getBlockListPkgNames());
    }

    @Tr369Get("Device.X_Skyworth.AppBatchUnBlock")
    public String SK_TR369_GetAppBatchUnBlock(String path) {
        ArrayList<String> blockListPkgNames = getBlockListPkgNames();
        ArrayList<String> whiteListPkgNames = new ArrayList<>();

        final PackageManager pm = GlobalContext.getContext().getPackageManager();
        List<PackageInfo> packlist = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        for (int i = 0; i < packlist.size(); i++) {
            PackageInfo pkgInfo = packlist.get(i);
            // 过滤掉无Activity的应用
            if (pkgInfo.activities == null || pkgInfo.activities.length < 1)
                continue;
            final String pkgName = pkgInfo.packageName;
            // 过滤掉不可以被open的应用
            if (!ApplicationUtils.canOpen(pm, pkgName))
                continue;
            // 过滤掉已被挂起的应用
            if (blockListPkgNames.contains(pkgName))
                continue;
            whiteListPkgNames.add(pkgName);
        }
        LogUtils.d(TAG, "whiteListPkgNames: " + whiteListPkgNames);
        return new Gson().toJson(whiteListPkgNames);
    }

    @Tr369Set("Device.X_Skyworth.AppBatchBlock")
    public boolean SK_TR369_SetAppBatchBlock(String path, String value) {
        ArrayList<String> blockListPkgNames = getBlockListPkgNames();

        List<String> packageNames = ApplicationUtils.parseStringList(value);
        LogUtils.d(TAG, "Wait to suspend application: " + packageNames);

        PackageManager packageManager = GlobalContext.getContext().getPackageManager();
        SuspendDialogInfo suspendDialogInfo = new SuspendDialogInfo.Builder()
                .setTitle(R.string.app_suspend_dialog_title)
                .setMessage(R.string.app_suspend_dialog_message)
                .build();

        String[] failedPackages = packageManager.setPackagesSuspended(
                packageNames.toArray(new String[0]), true, null, null, suspendDialogInfo);

        if (failedPackages.length != 0) {
            // 处理未成功挂起的应用程序，未成功挂起的应用将被移出黑名单全局变量
            LogUtils.e(TAG, "Failed to suspend App: " + Arrays.toString(failedPackages));
            for (String packageName : failedPackages) {
                packageNames.remove(packageName);
            }
        }

        for (String packageName : packageNames) {
            if (!blockListPkgNames.contains(packageName)) {
                // 此处判断新挂起的应用是否已经在黑名单全局变量中，如果不在则新加到全局变量中
                blockListPkgNames.add(packageName);
            }
        }

        setBlockListPkgNames(blockListPkgNames);
        return true;
    }

    @Tr369Set("Device.X_Skyworth.AppBatchUnBlock")
    public boolean SK_TR369_SetAppBatchUnBlock(String path, String value) {
        ArrayList<String> blockListPkgNames = getBlockListPkgNames();

        List<String> packageNames = ApplicationUtils.parseStringList(value);
        LogUtils.d(TAG, "Wait to cancel pending application: " + packageNames);

        PackageManager packageManager = GlobalContext.getContext().getPackageManager();

        String[] failedPackages = packageManager.setPackagesSuspended(
                packageNames.toArray(new String[0]), false, null, null, (SuspendDialogInfo) null);

        for (String packageName : packageNames) {
            if (blockListPkgNames.contains(packageName)) {
                if (failedPackages.length != 0) {
                    // 如果某个应用解除挂起失败，包名仍然留在黑名单全局变量中
                    if (Arrays.asList(failedPackages).contains(packageName)) {
                        LogUtils.e(TAG, "Unsuspended application failed: " + packageName);
                        continue;
                    }
                }
                // 成功解除挂起的应用将被移出黑名单
                blockListPkgNames.remove(packageName);
            }
        }

        setBlockListPkgNames(blockListPkgNames);
        return true;
    }

    private void clearAppBlackList() {
        int numBlacklist = SystemProperties.getInt("persist.sys.tr069.blacklist.number", 0);
        for (int i = 1; i <= numBlacklist; ++i) {
            SystemProperties.set("persist.sys.tr069.blacklist.part" + i, "");
        }
        SystemProperties.set("persist.sys.tr069.blacklist.number", "0");
    }

    private void clearAppWhiteList() {
        int numWhitelist = SystemProperties.getInt("persist.sys.tr069.whitelist.number", 0);
        for (int i = 1; i <= numWhitelist; ++i) {
            SystemProperties.set("persist.sys.tr069.whitelist.part" + i, "");
        }
        SystemProperties.set("persist.sys.tr069.whitelist.number", "0");
    }

    private void setBlackListData(int num, String data) {
        SystemProperties.set("persist.sys.tr069.blacklist.number", String.valueOf(num));
        SystemProperties.set("persist.sys.tr069.blacklist.part" + num, data);
    }

    private void setWhiteListData(int num, String data) {
        SystemProperties.set("persist.sys.tr069.whitelist.number", String.valueOf(num));
        SystemProperties.set("persist.sys.tr069.whitelist.part" + num, data);
    }

    @Tr369Get("Device.X_Skyworth.AppBlackList")
    public String SK_TR369_GetAppBlackList(String path) {
        int numBlacklist = SystemProperties.getInt("persist.sys.tr069.blacklist.number", 0);
        ArrayList<String> blacklist = new ArrayList<>();
        for (int i = 1; i <= numBlacklist; ++i) {
            String array = SystemProperties.get("persist.sys.tr069.blacklist.part" + i, "");

            List<String> packageNames = ApplicationUtils.parseStringList(array);
            blacklist.addAll(packageNames);
        }
        return new Gson().toJson(blacklist);
    }

    @Tr369Set("Device.X_Skyworth.AppBlackList")
    public boolean SK_TR369_SetAppBlackList(String path, String value) {
        clearAppWhiteList();
        clearAppBlackList();

        List<String> packageNames = ApplicationUtils.parseStringList(value);
        LogUtils.i(TAG, "Waiting for blacklist to be set: " + packageNames);
        if (packageNames.isEmpty()) {
            return true;
        }

        // 由于单个系统属性Set长度不能超过91，所以需要将包名数组拆分，依次存入不同的系统属性
        int numBlacklist = 0;
        if (value.length() > 91) {
            ArrayList<String> blacklist = new ArrayList<>();
            for (String packageName : packageNames) {
                if (packageName.length() + blacklist.toString().length() > 85) {
                    numBlacklist++;
                    setBlackListData(numBlacklist, new Gson().toJson(blacklist));
                    blacklist.clear();
                }
                blacklist.add(packageName);
            }
            if (!blacklist.isEmpty()) {
                numBlacklist++;
                setBlackListData(numBlacklist, new Gson().toJson(blacklist));
            }
        } else {
            setBlackListData(1, value);
        }

        // Black功能需要额外检测包名是否已安装，已安装则需要进行卸载，只卸载非预置APPs
        final PackageManager pm = GlobalContext.getContext().getPackageManager();
        List<PackageInfo> packlist = pm.getInstalledPackages(0);
        for (int i = 0; i < packlist.size(); i++) {
            PackageInfo pkgInfo = packlist.get(i);
            final String pkgName = pkgInfo.packageName;
            if (packageNames.contains(pkgName)) {
                final ApplicationInfo info = pkgInfo.applicationInfo;
                if (!info.isSystemApp()) {
                    ApplicationUtils.uninstall(pkgName);
                    LogUtils.d(TAG, "Uninstallation process completed.");
                } else {
                    LogUtils.i(TAG, "This application is a system app and cannot be uninstalled.");
                }
            }
        }

        return true;
    }

    @Tr369Get("Device.X_Skyworth.AppWhiteList")
    public String SK_TR369_GetAppWhiteList(String path) {
        int numWhitelist = SystemProperties.getInt("persist.sys.tr069.whitelist.number", 0);
        ArrayList<String> whitelist = new ArrayList<>();
        for (int i = 1; i <= numWhitelist; ++i) {
            String array = SystemProperties.get("persist.sys.tr069.whitelist.part" + i, "");

            List<String> packageNames = ApplicationUtils.parseStringList(array);
            whitelist.addAll(packageNames);
        }
        return new Gson().toJson(whitelist);
    }

    @Tr369Set("Device.X_Skyworth.AppWhiteList")
    public boolean SK_TR369_SetAppWhiteList(String path, String value) {
        clearAppBlackList();
        clearAppWhiteList();

        List<String> packageNames = ApplicationUtils.parseStringList(value);
        LogUtils.i(TAG, "Waiting for whitelist to be set: " + packageNames);
        if (packageNames.isEmpty()) {
            return true;
        }

        int numWhitelist = 0;
        if (value.length() > 91) {
            ArrayList<String> whitelist = new ArrayList<>();
            for (String packageName : packageNames) {
                if (packageName.length() + whitelist.toString().length() > 85) {
                    numWhitelist++;
                    setWhiteListData(numWhitelist, new Gson().toJson(whitelist));
                    whitelist.clear();
                }
                whitelist.add(packageName);
            }
            if (!whitelist.isEmpty()) {
                numWhitelist++;
                setWhiteListData(numWhitelist, new Gson().toJson(whitelist));
            }
        } else {
            setWhiteListData(1, value);
        }
        return true;
    }

    /**
     * 从 path 中获取到 i ，然后获取到App list 中第 i 个 app
     *
     * @return 第 i 个APP的包名
     */
    private AppInfo getAppByPath(String path) {
        AppInfo appInfo = null;
        String[] params = ProtocolPathUtils.parse(REFIX, path);
        if (params == null || params.length < 1) {
            //Todo report error.
            return null;
        }
        int index = 0;
        try {
            index = Integer.parseInt(params[0]);
            if (index < 1) {
                //Todo report error.
                return null;
            }
        } catch (NumberFormatException e) {
            //Todo report error.
            return null;
        }

        if (mAppsManager == null) {
            mAppsManager = new AppsManager(GlobalContext.getContext());
        }
        List<AppInfo> apps = mAppsManager.getList();
        if (apps == null || apps.size() < 1 || index > apps.size()) {
            return null;
        }

        try {
            appInfo = apps.get(index - 1);
            LogUtils.d(TAG, "Appx: " + appInfo.toString() + ", path: " + path);
        } catch (Exception e) {
            return null;
        }
        return appInfo;
    }

    public UsageStats getUsageStats(String packageName, Context context) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        String dbParam = String.valueOf(1800);  // DbManager.getDBParam("Device.X_Skyworth.AppInfoPeriodicInformInterval");
        long startTime = TextUtils.isEmpty(dbParam) ? 0 : (endTime - Long.parseLong(dbParam) * 1000);
        if (endTime - firstTime > 10000) {
            list = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
        }
        firstTime = endTime;
        //需要注意的是5.1以上，如果不打开此设置，queryUsageStats获取到的是size为0的list；
        if (list.size() == 0) {
            return null;
        } else {
            for (UsageStats usageStats : list) {
                if (usageStats.getPackageName().equals(packageName)) {
                    return usageStats;
                }
            }
        }
        return null;
    }

    public UsageStats getTotalUsageStats(String packageName) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = 0;
        if (endTime - totalFirstTime > 10000) {
            totalList = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
            totalFirstTime = endTime;
        }
        //需要注意的是5.1以上，如果不打开此设置，queryUsageStats获取到的是size为0的list；
        if (totalList.size() == 0) {
            return null;
        } else {
            for (UsageStats usageStats : totalList) {
                if (usageStats.getPackageName().equals(packageName)) {
                    return usageStats;
                }
            }
        }
        return null;
    }

    public String getMemoryUsed(String packageName) {
        return getUsage("dumpsys meminfo -s " + packageName, packageName);
    }

    public String getCpuUsed(String packageName) {
        return getUsage("dumpsys cpuinfo", packageName);
    }

    public String getUsage(String command, String packageName) {
        Process process = null;
        BufferedReader bufferedReader = null;
        float cpuUsage = 0;

        try {
            process = Runtime.getRuntime().exec(command);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (command.contains("cpuinfo")) {
                    if (line.contains(packageName) && line.trim().length() > 1) {
                        LogUtils.d(TAG, "The data obtained from the '" + command +
                                "' command is: " + line);
                        float ret = Float.parseFloat(line.split("%", 2)[0]);
                        cpuUsage += ret;
                        LogUtils.d(TAG, "App: " + packageName + ", CPU usage: " + cpuUsage + "%");
                    }
                } else if (command.contains("meminfo")) {
                    if (line.contains("TOTAL RSS")) {
                        return line.split("TOTAL RSS:", 2)[1].split("T", 2)[0];
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Appx: getUsage call failed, " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "bufferedReader close call failed, " + e.getMessage());
                }
            }
        }
        return (command.contains("cpuinfo")) ? String.valueOf(cpuUsage) : "0";
    }

}
