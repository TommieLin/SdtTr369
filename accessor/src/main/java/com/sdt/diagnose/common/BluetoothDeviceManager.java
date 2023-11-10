package com.sdt.diagnose.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sdt.diagnose.common.bean.BluetoothDeviceInfo;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothDeviceManager extends AbstractCachedArray<BluetoothDeviceInfo> {
    private static final String TAG = "BluetoothDeviceManager";

    public BluetoothDeviceManager(Context context) {
        super(context);
    }

    @Override
    void buildList(Context context) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Log.e(TAG, "BluetoothAdapter is null.");
            return;
        }
        final Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();
        if (bondedDevices == null) {
            Log.e(TAG, "no bonded devices");
            return;
        }

        Log.d(TAG, "bonded devices size = " + bondedDevices.size());
        if (bondedDevices.size() == 0) {
            return;
        }
        for (final BluetoothDevice device : bondedDevices) {
            final String deviceAddress = device.getAddress();
            if (TextUtils.isEmpty(deviceAddress)) {
                Log.e(TAG, "bluetooth device address null");
                continue;
            }
            BluetoothDeviceInfo bluetoothDeviceInfo = new BluetoothDeviceInfo(context, device);
            Log.d(TAG, "BluetoothDeviceInfo: " + bluetoothDeviceInfo);
            add(bluetoothDeviceInfo);
        }
    }

}
