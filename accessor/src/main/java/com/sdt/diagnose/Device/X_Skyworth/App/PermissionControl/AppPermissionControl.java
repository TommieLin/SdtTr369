package com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;

import com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.model.AppPermissionGroup;
import com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AppPermissionControl {
    public static boolean changeRuntimePermissions(Context context, String appPackageName, String permissionGroupName, boolean isGrant) {
        if (isGrant) {
            return grantRuntimePermissions(context, appPackageName, permissionGroupName);
        } else {
            return revokeRuntimePermissions(context, appPackageName, permissionGroupName);
        }
    }

    private static boolean grantRuntimePermissions(Context context, String appPackageName, String permissionGroupName) {
        AppPermissionGroup group = createAppPermissionGroup(context, appPackageName, permissionGroupName);
        return group != null && group.grantRuntimePermissions(false);
    }

    private static boolean revokeRuntimePermissions(Context context, String appPackageName, String permissionGroupName) {
        AppPermissionGroup group = createAppPermissionGroup(context, appPackageName, permissionGroupName);
        return group != null && group.revokeRuntimePermissions(false);
    }

    public static boolean areRuntimePermissionsGranted(Context context, String appPackageName, String permissionGroupName) {
        AppPermissionGroup group = createAppPermissionGroup(context, appPackageName, permissionGroupName);
        return group != null && group.areRuntimePermissionsGranted();
    }

    public static boolean isSupportOperatePermissions(Context context, String appPackageName, String permissionGroupName) {
        AppPermissionGroup group = createAppPermissionGroup(context, appPackageName, permissionGroupName);
        return group != null && ! group.isSystemFixed() && ! group.isPolicyFixed();
    }


    private static AppPermissionGroup createAppPermissionGroup(Context context, String appPackageName, String permissionGroupName) {
        PackageManager pm = context.getPackageManager();
        final PackageInfo packageInfo;
        AppPermissionGroup group = null;
        PackageItemInfo groupInfo = Utils.getGroupInfo(permissionGroupName, context);
        List<PermissionInfo> groupPermInfos = Utils.getGroupPermissionInfos(permissionGroupName, context);
        if (groupInfo == null || groupPermInfos == null) return null;
        PackageManager packageManager = context.getPackageManager();
        CharSequence groupLabel = groupInfo.loadLabel(packageManager);
        CharSequence fullGroupLabel = groupInfo.loadSafeLabel(packageManager, 0,
                TextUtils.SAFE_STRING_FLAG_TRIM | TextUtils.SAFE_STRING_FLAG_FIRST_LINE);

        List<PermissionInfo> targetPermInfos = new ArrayList<>(groupPermInfos.size());
        for (int i = 0; i < groupPermInfos.size(); i++) {
            PermissionInfo permInfo = groupPermInfos.get(i);
            if (permInfo.getProtection()
                    == PermissionInfo.PROTECTION_DANGEROUS
                    && (permInfo.flags & PermissionInfo.FLAG_INSTALLED) != 0
                    && (permInfo.flags & PermissionInfo.FLAG_REMOVED) == 0) {
                targetPermInfos.add(permInfo);
            }
        }

        try {
            packageInfo = pm.getPackageInfo(appPackageName,
                    PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        if (packageInfo.requestedPermissions == null) {
            return null;
        }

        for (int j = 0; j < packageInfo.requestedPermissions.length; j++) {
            String requestedPerm = packageInfo.requestedPermissions[j];
            PermissionInfo requestedPermissionInfo = null;

            for (PermissionInfo groupPermInfo : targetPermInfos) {
                if (requestedPerm.equals(groupPermInfo.name)) {
                    requestedPermissionInfo = groupPermInfo;
                    break;
                }
            }

            if (requestedPermissionInfo == null) {
                continue;
            }

            group = AppPermissionGroup.create(context,
                    packageInfo, groupInfo, groupPermInfos, groupLabel, fullGroupLabel, false);

            if (group != null) break;
        }
        return group;
    }
}
