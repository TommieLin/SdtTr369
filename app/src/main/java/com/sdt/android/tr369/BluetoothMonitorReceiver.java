package com.sdt.android.tr369;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sdt.diagnose.Device.X_Skyworth.Bluetooth.BluetoothDeviceX;

/**
 * @Description: java类作用描述
 * @CreateDate: 2021/11/8 10:31
 */
public class BluetoothMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothMonitorReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BluetoothMonitorReceiver onReceive action: " + action);
        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "Bluetooth is turning on");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "Bluetooth is turned on");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "Bluetooth is shutting down");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "Bluetooth is turned off");
                            break;
                    }
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "Bluetooth device connected");
                    BluetoothDeviceX.updateBluetoothList();
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "Bluetooth device disconnected");
                    BluetoothDeviceX.updateBluetoothList();
                    break;

                case BluetoothDevice.ACTION_ALIAS_CHANGED:
                    Log.d(TAG, "Bluetooth device name change");
                    BluetoothDeviceX.updateBluetoothList();
                    break;
            }
        }
    }
}
