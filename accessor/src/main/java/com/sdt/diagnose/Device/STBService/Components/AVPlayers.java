package com.sdt.diagnose.Device.STBService.Components;

import com.sdt.annotations.Tr369Get;

public class AVPlayers {

    @Tr369Get("Device.Services.STBService.1.AVPlayers.AVPlayer.1.Name")
    public String SK_TR369_GetAVPlayerName1(String path) {
        return AVPlayersManager.getInstance().getAVPlayerName1(path);
    }

    @Tr369Get("Device.Services.STBService.1.AVPlayers.AVPlayer.2.Name")
    public String SK_TR369_GetAVPlayerName2(String path) {
        return AVPlayersManager.getInstance().getAVPlayerName2(path);
    }

    @Tr369Get("Device.Services.STBService.1.AVPlayers.AVPlayer.3.Name")
    public String SK_TR369_GetAVPlayerName3(String path) {
        return AVPlayersManager.getInstance().getAVPlayerName3(path);
    }

}
