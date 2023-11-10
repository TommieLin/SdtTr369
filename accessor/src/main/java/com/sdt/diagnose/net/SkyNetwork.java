package com.sdt.diagnose.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sdt.diagnose.common.NetworkUtils;
import com.sdt.diagnose.database.DbManager;


public class SkyNetwork {
    private static final String TAG = "SkyNetwork";
    private static ConnectivityManager connectActivityManager = null;
    private static Context mContext = null;

    /**
     * WiFi mac 地址
     */
    public static final String SKYWORTH_PARAMS_SYS_WIFIMAC =
            "skyworth.params.sys.wifimac";

    /**
     * 是否启用以太网有线网络
     */
    public static final String SKYWORTH_PARAMS_NET_ETH_ENABLE =
            "skyworth.params.net.eth_enable";

    /**
     * 是否启用WIFI无线网络
     */
    public static final String SKYWORTH_PARAMS_NET_WIFI_ENABLE =
            "skyworth.params.net.wifi_enable";


    /**
     * 无线网络IP
     */
    public static final String SKYWORTH_PARAMS_NET_WIFI_IP =
            "skyworth.params.net.wifi_ip";

    /**
     * 无线网络Mask
     */
    public static final String SKYWORTH_PARAMS_NET_WIFI_MASK =
            "skyworth.params.net.wifi_mask";

    /**
     * 无线网络网关
     */
    public static final String SKYWORTH_PARAMS_NET_WIFI_GATEWAY =
            "skyworth.params.net.wifi_gateway";

    /**
     * 本机Mac地址
     */
    public static final String DEVICE_LAN_MACADDRESS =
            "Device.LAN.MACAddress";

    /**
     * 静态设置IP
     */
    public static final String DEVICE_LAN_IPADDRESS =
            "Device.LAN.IPAddress";

    /**
     * 静态设置Mask
     */
    public static final String DEVICE_LAN_SUBNETMASK =
            "Device.LAN.SubnetMask";

    /**
     * 静态设置网关
     */
    public static final String DEVICE_LAN_DEFAULTGATEWAY =
            "Device.LAN.DefaultGateway";


    private static String eth_mac = "";

    public SkyNetwork(Context context) {
        try {
            mContext = context;
            connectActivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            SkyEthMacUpdate();
            SkyWifiMacUpdate();
            // setSoftWareVersion();
            Log.d(TAG, "CONNECTIVITY_ACTION");
        } catch (Exception e) {
            Log.e(TAG, "SkyNetwork creat error, " + e.getMessage());
        }
    }

    public static boolean isConnected() {
        NetworkInfo networkInfo = connectActivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void updateAllNetworkInfo() {
//        final int iptv = SystemProperties.getInt("persist.sys.vlan.iptv.id", 3200);
//        final int acs = SystemProperties.getInt("persist.sys.vlan.acs.id", 461);
        setNetworkStatus();
        setWifiNetworkStatus();

        updateEthNetworkInfo("eth0");
        updateEthNetworkInfo("wlan0");
//        updateEthNetworkInfo("eth0." + acs);
//        updateEthNetworkInfo("eth0." + iptv);
    }

    private void setNetworkStatus() {
        String netStatus = "0";
        if (connectActivityManager != null) {
            NetworkInfo info = connectActivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

            if (info.getState() == State.DISCONNECTED) {
                netStatus = "0";
            }
            if (info.getState() == State.CONNECTED) {
                netStatus = "1";
            }
        }
        DbManager.setDBParam(SKYWORTH_PARAMS_NET_ETH_ENABLE, netStatus);
    }

    private void setWifiNetworkStatus() {
        String netStatus = "0";
        if (connectActivityManager != null) {
            NetworkInfo info = connectActivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (info.getState() == State.DISCONNECTED) {
                netStatus = "0";
            }
            if (info.getState() == State.CONNECTED) {
                netStatus = "1";
            }
        }
        DbManager.setDBParam(SKYWORTH_PARAMS_NET_WIFI_ENABLE, netStatus);
    }

    private void updateEthNetworkInfo(String key) {
        String ip_v4 = "";
        String mask_v4 = "";
        String gateway_v4 = "";
        String key_ip_v4 = "";
        String key_mask_v4 = "";
        String key_gateway_v4 = "";
//        int iptv = SystemProperties.getInt("persist.sys.vlan.iptv.id", 3200);
//        int acs = SystemProperties.getInt("persist.sys.vlan.acs.id", 461);

        // ip_v4 = getNetworkParamFromBusybox(key,"inet addr:");
        // mask_v4 = getNetworkParamFromBusybox(key,"Mask:");
        //gateway_v4 = getGatewayFromBusybox(ip_v4);
        // gateway_v4 = getGatewayFromBusybox(key); // can be found by ip route show | grep eth0.name , not grep ip

//		gateway_v4 = NetworkUtils.getGateWay(mContext) ;

        if (key.equals("eth0")) {
            if ("1".equals(DbManager.getDBParam(SKYWORTH_PARAMS_NET_ETH_ENABLE))) {
                ip_v4 = NetworkUtils.getIpv4Address(mContext);
                mask_v4 = NetworkUtils.getLanMask(mContext);
                gateway_v4 = NetworkUtils.getGateway(mContext);
            }
            key_ip_v4 = DEVICE_LAN_IPADDRESS;
            key_gateway_v4 = DEVICE_LAN_DEFAULTGATEWAY;
            key_mask_v4 = DEVICE_LAN_SUBNETMASK;
        }

        if (key.equals("wlan0")) {
            if ("1".equals(DbManager.getDBParam(SKYWORTH_PARAMS_NET_WIFI_ENABLE))) {
                ip_v4 = NetworkUtils.getIpv4Address(mContext);
                mask_v4 = NetworkUtils.getLanMask(mContext);
                gateway_v4 = NetworkUtils.getGateway(mContext);
            }
            key_ip_v4 = SKYWORTH_PARAMS_NET_WIFI_IP;
            key_gateway_v4 = SKYWORTH_PARAMS_NET_WIFI_GATEWAY;
            key_mask_v4 = SKYWORTH_PARAMS_NET_WIFI_MASK;
        }

        if (! ip_v4.equals("")) {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", ip_v4: " + ip_v4);
            DbManager.setDBParam(key_ip_v4, ip_v4);
        } else {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", ip_v4: 0.0.0.0");
            DbManager.setDBParam(key_ip_v4, "0.0.0.0");
        }

        if (! mask_v4.equals("")) {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", mask_v4: " + mask_v4);
            DbManager.setDBParam(key_mask_v4, mask_v4);
        } else {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", mask_v4: 0.0.0.0");
            DbManager.setDBParam(key_mask_v4, "0.0.0.0");
        }

        if (! gateway_v4.equals("")) {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", gateway_v4: " + gateway_v4);
            DbManager.setDBParam(key_gateway_v4, gateway_v4);
        } else {
            Log.d(TAG, "updateEthNetworkInfo key: " + key + ", gateway_v4: 0.0.0.0");
            DbManager.setDBParam(key_gateway_v4, "0.0.0.0");
        }
    }

    private int SkyWifiMacUpdate() {
        String wifi_mac = NetworkUtils.getWifiMac(mContext);
        if (! wifi_mac.equals("")) {
            DbManager.setDBParam(SKYWORTH_PARAMS_SYS_WIFIMAC, wifi_mac);
        }
        return 0;
    }

    private int SkyEthMacUpdate() {
        // eth_mac = getNetworkParamFromBusybox("eth0", "HWaddr");
        eth_mac = NetworkUtils.getEthernetMac(mContext);
        if (! eth_mac.equals("")) {
            DbManager.setDBParam(DEVICE_LAN_MACADDRESS, eth_mac);
        }
        return 0;
    }

}