package com.sdt.android.tr369;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sdt.android.tr369.Utils.FileUtil;
import com.sdt.diagnose.Device.DeviceInfo.ProcessInfoX;
import com.sdt.diagnose.Device.LanX;
import com.sdt.diagnose.Device.X_Skyworth.FTIMonitor;
import com.sdt.diagnose.Device.X_Skyworth.SystemDataStat;
import com.sdt.diagnose.Tr369PathInvoke;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.database.DbManager;
import com.sdt.opentr369.OpenTR369Native;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class SdtTr369Service extends Service {
    private static final String TAG = "SdtTr369Service";
    private static final String CHANNEL_ID = "SdtTr369ServiceChannelId";
    private static final String CHANNEL_NAME = "SdtTr369ServiceChannelName";
    private SdtTr369Receiver mSdtTr369Receiver = null;
    private PackageReceiver mPackageReceiver = null;
    private BluetoothMonitorReceiver mBluetoothMonitorReceiver = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;
    public static final int MSG_START_TR369 = 3300;
    public static final int MSG_STOP_TR369 = 3301;

    public void handleTr369Message(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_START_TR369:
                Log.e(TAG, " ####### Outis ### handleTr369Message MSG_START_TR369");
                startTR369();
                break;
            case MSG_STOP_TR369:
                Log.e(TAG, " ####### Outis ### handleTr369Message MSG_STOP_TR369");
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.e(TAG, " ####### Outis ### onBind start");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, " ####### Outis ### onCreate start");
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
        startForeground(1, notification);

        GlobalContext.setContext(getApplicationContext());
        Log.e(TAG, " ####### Outis ### onCreate return");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, " ####### Outis ### onStartCommand start");
        initTr369Service();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    private void initTr369Service() {
        Log.e(TAG, " ####### Outis ### initTr369Service start");
        mHandlerThread = new HandlerThread("tr369_worker");
        // 先启动，再初始化handler
        mHandlerThread.start();
        if (mHandler == null) {
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    handleTr369Message(msg);
                }
            };
        }
        registerSdtTr369Receiver();
        registerPackageReceiver();
        registerBluetoothMonitorReceiver();

        // 初始化FTI停留时间监控程序
        new FTIMonitor();
        // 初始化系统数据采集程序
        new SystemDataStat(this);
        // 初始化LanX用于检测静态IP是否能访问互联网
        new LanX();
    }

    private void registerSdtTr369Receiver() {
        Log.e(TAG, " ####### Outis ### registerSdtTr369Receiver start");
        mSdtTr369Receiver = new SdtTr369Receiver(mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mSdtTr369Receiver, intentFilter);
    }

    private void registerPackageReceiver() {
        mPackageReceiver = new PackageReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addDataScheme("package");
        registerReceiver(mPackageReceiver, intentFilter);
    }

    private void registerBluetoothMonitorReceiver() {
        if (mBluetoothMonitorReceiver == null) {
            mBluetoothMonitorReceiver = new BluetoothMonitorReceiver();
        }
        //注册监听
        IntentFilter stateChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter connectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter nameChangeFilter = new IntentFilter(BluetoothDevice.ACTION_ALIAS_CHANGED);

        registerReceiver(mBluetoothMonitorReceiver, stateChangeFilter);
        registerReceiver(mBluetoothMonitorReceiver, connectedFilter);
        registerReceiver(mBluetoothMonitorReceiver, disConnectedFilter);
        registerReceiver(mBluetoothMonitorReceiver, nameChangeFilter);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, " ####### Outis ### onDestroy start");
        if (mSdtTr369Receiver != null)  unregisterReceiver(mSdtTr369Receiver);
        if (mBluetoothMonitorReceiver != null) unregisterReceiver(mBluetoothMonitorReceiver);
        if (mPackageReceiver != null) unregisterReceiver(mPackageReceiver);

        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_STOP_TR369);
            mHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        super.onDestroy();
    }

    private void startTR369() {

        OpenTR369Native.SetListener(mListener);

        FileUtil.copyTr369AssetsToFile(getApplicationContext());
        String defaultFilePath = getApplicationContext().getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_DEFAULT;
        Log.e(TAG, " ############ Outis ### startTR369 defaultFilePath: " + defaultFilePath);

        int ret = OpenTR369Native.SetInitFilePath(defaultFilePath);
        Log.e(TAG, " ############ Outis ### startTR369 SetInitFilePath ret: " + ret);

        String modelFile = getApplicationContext().getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_XML;
        ret = OpenTR369Native.OpenTR369Init(modelFile);
        Log.e(TAG, " ############ Outis ### startTR369 ret: " + ret);

        String test_str = OpenTR369Native.stringFromJNI();
        Log.e(TAG, " ############ Outis ### startTR369 test_str: " + test_str);
    }

    private final OpenTR369Native.IOpenTr369Listener mListener = new OpenTR369Native.IOpenTr369Listener() {
        @Override
        public String openTR369GetAttr(int what, String path) {
            String ret = Tr369PathInvoke.getInstance().getAttribute(what, path);
            if (ret == null) {
                ret = OpenTR369Native.GetDBParam(path);
            }
            return ret;
        }

        @Override
        public boolean openTR369SetAttr(int what, String path, String value) {
            boolean ret = Tr369PathInvoke.getInstance().setAttribute(what, path, value);
            if (! ret) {
                ret = (OpenTR369Native.SetDBParam(path, value) != 0);
            }
            return ret;
        }
    };

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: Can't dump ActivityManager from from pid = "
                    + Binder.getCallingPid() + ", uid = " + Binder.getCallingUid()
                    + " without permission " + android.Manifest.permission.DUMP);
            return;
        }
        printTr369Message(fd, pw, args);
    }

    private void printTr369Message(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length > 0) {
            String cmd = args[0];
            Log.d(TAG, "mSkParamDB dumpsys args: " + Arrays.toString(args));
            if (("dbget").equalsIgnoreCase(cmd) && args.length > 1) {
                pw.println(formatString(args[1]));
            } else if (("dbset").equalsIgnoreCase(cmd) && args.length > 2) {
                int ret = DbManager.setDBParam(args[1], args[2]);
                if (ret == 0) {
                    pw.println(formatString(args[1]));
                } else {
                    pw.println("dbset failed! error code: " + ret);
                }
            } else if (("dbdel").equalsIgnoreCase(cmd) && args.length > 1) {

            } else if ("show".equals(cmd)) {
                // [show] [database|datamodel]
//                pw.println(DbManager.showData(args[1]));
                DbManager.showData(args[1]);
            }
        }
    }

    private String formatString(String paramKey) {
        return paramKey + " : [" + DbManager.getDBParam(paramKey) + "]";
    }

}
