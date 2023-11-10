package com.sdt.diagnose.Device.Ethernet;

import com.sdt.annotations.Tr369Get;
import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.NetworkUtils;

/**
 * @ClassName: LinkX
 * @Description: Device.Ethernet.Link.{i}.
 * Enable/Status/Name/MACAddress
 * @CreateDate: 2021/8/12 14:01
 */
public class LinkX {

    @Tr369Get("Device.Ethernet.Link.1.Enable")
    public String SK_TR369_GetEthEnable(String path) {
        boolean ret = NetworkUtils.isEthernetConnected(GlobalContext.getContext());
        return Boolean.toString(ret);
    }

    @Tr369Get("Device.Ethernet.Link.1.Status")
    public String SK_TR369_GetEthStatus(String path) {
        return NetworkUtils.getEthernetInterfaceStatus(GlobalContext.getContext());
    }

    @Tr369Get("Device.Ethernet.Link.1.Name")
    public String SK_TR369_GetEthName(String path) {
        return "eth0";
    }

    @Tr369Get("Device.Ethernet.Link.1.MACAddress")
    public String SK_TR369_GetEthMACAddress(String path) {
        return NetworkUtils.getEthernetMacAddress();
    }

}
