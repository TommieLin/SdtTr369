package com.sdt.diagnose.common;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import green_green_avk.ptyprocess.PtyProcess;
import io.socket.client.IO;
import io.socket.client.Socket;


public class XshellClient {
    private static final String TAG = "XshellClient";
    private static PtyProcess sPtyProcess;
    static OutputStream ptyProcessOutputStream;
    static InputStream ptyProcessInputStream;
    static Socket mSocket;
    static Thread forwardThread;

    public static void start(String serverUrl) {
        if (mSocket != null) stop();

        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{"websocket"};
            // 失败重连次数
            options.reconnectionAttempts = 5;
            // 失败重连的时间间隔(ms)
            options.reconnectionDelay = 1000;
            // 连接超时时间(ms)
            options.timeout = 10000;
            // userId: 唯一标识 传给服务端存储
            mSocket = IO.socket(serverUrl, options);

            String workPath = GlobalContext.getContext().getDataDir().getPath();
            if (!PtyProcess.setWorkPath(workPath)) {
                Log.e(TAG, "Failed to set work path: " + workPath);
            }

            Map<String, String> env = new HashMap<>(System.getenv());
            //env.put("TERM", "xterm");
            sPtyProcess = PtyProcess.system(null, env);
            ptyProcessOutputStream = sPtyProcess.getOutputStream();
            ptyProcessInputStream = sPtyProcess.getInputStream();

            mSocket.on(Socket.EVENT_CONNECT, XshellClient::onConnected)
                    .on(Socket.EVENT_DISCONNECT, XshellClient::onDisConnected)
                    .on(Socket.EVENT_PONG, objects -> Log.i(TAG, "IoSocket pong..."))
                    .on(Socket.EVENT_CONNECT_ERROR, XshellClient::onConnectError)
                    // 自定义事件`SEND_DATA_EVENT` -> 接收服务端消息
                    .on(Socket.EVENT_MESSAGE, XshellClient::onMessage);

            // 读取服务器终端返回的stream信息
            forwardThread = new ForwardThread();
            mSocket.connect();
        } catch (Exception e) {
            Log.e(TAG, "XshellClient start error, " + e.getMessage());
        }
    }

    public static void stop() {
        try {
            if (ptyProcessOutputStream != null) ptyProcessOutputStream.close();
            if (ptyProcessInputStream != null) ptyProcessInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "pty ProcessStream error, " + e.getMessage());
        } finally {
            ptyProcessOutputStream = null;
            ptyProcessInputStream = null;
        }

        if (sPtyProcess != null) {
            sPtyProcess.destroy();
            sPtyProcess = null;
        }

        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
        if (forwardThread != null) {
            forwardThread.interrupt();
            forwardThread = null;
        }
    }


    private static void onConnected(Object[] objects) {
        Log.i(TAG, "XshellClient connected...");
        forwardThread.start();
    }

    private static void onDisConnected(Object[] objects) {
        Log.i(TAG, "XshellClient disconnect...");
        stop();
    }

    private static void onMessage(Object[] objects) {
        try {
            byte[] data = AESUtil.decrypt((byte[]) objects[0]);
            ptyProcessOutputStream.write(data);
            ptyProcessOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void onConnectError(Object[] objects) {
        Log.i(TAG, "XshellClient onConnectError: " + objects[0].toString());
        stop();
    }

    private static class ForwardThread extends Thread {
        @Override
        public void run() {
            try {
                Log.i(TAG, "XshellClient forwardThread started......");
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                while (!Thread.interrupted() && (len = ptyProcessInputStream.read(bytes)) != - 1) {
                    baos.write(bytes, 0, len);
                    mSocket.emit(Socket.EVENT_MESSAGE, AESUtil.encrypt(baos.toByteArray()));
                    baos = new ByteArrayOutputStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "XshellClient forwardThread end...");
        }
    }
}

