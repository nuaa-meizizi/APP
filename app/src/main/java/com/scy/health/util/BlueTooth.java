package com.scy.health.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.scy.health.Interface.BluetoothInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BlueTooth {
    private static final String TAG = "BlueTooth";
    private BluetoothSocket BTSocket;
    private Context context;
    private BluetoothAdapter BTAdapter;
    private BluetoothDevice device;
    private BluetoothInterface bluetoothInterface;
    private StringBuffer cache = new StringBuffer();
    private int start;
    private int end;
    private Boolean ready = false;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!ready)
                        bluetoothInterface.onError("设备连接超时");
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public BlueTooth(Context context) {
        this.context = context;
        checkBT(context);
        registerBTReceiver();
    }

    public void start(BluetoothInterface bluetoothInterface){
        this.bluetoothInterface = bluetoothInterface;
        BTAdapter.startDiscovery();
        timeout();
    }

    public void stop() throws IOException {
        if (BTSocket != null)
            BTSocket.close();
        unregisterBTReceiver();

    }

    /**
     * 检查蓝牙
     */
    private void checkBT(Context context) {
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter != null) {
            if (!BTAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 设置蓝牙可见性，最多300秒
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                context.startActivity(intent);
            }
        } else {
            Log.e(TAG, "checkBT: 本地设备驱动异常!");
            bluetoothInterface.onError("本地设备驱动异常!");
        }
    }

    /**
     * 注册广播
     */
    public void registerBTReceiver() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        context.registerReceiver(BTReceive, intentFilter);
    }

    /**
     * 注销广播
     */
    public void unregisterBTReceiver() {
        try {
            context.unregisterReceiver(BTReceive);
        }catch (Exception e){
            Log.e(TAG, "unregisterBTReceiver: ",e );
        }
    }

    /**
     * 广播接收者
     */
    private BroadcastReceiver BTReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "onReceive: 找到的BT名:" + device.getName());
                // 如果查找到的设备符合要连接的设备，处理
                if (device.getName() != null && device.getName().equalsIgnoreCase("NUAACS")) {
                    Log.i(TAG, "onReceive: 配对"+device.getName());
                    // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                    BTAdapter.cancelDiscovery();
                    // 获取蓝牙设备的连接状态
                    int connectState = device.getBondState();
                    switch (connectState) {
                        // 未配对
                        case BluetoothDevice.BOND_NONE:
                            Log.i(TAG, "onReceive: 开始配对:");
                            try {
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                                ready = true;
                            } catch (Exception e) {
                                bluetoothInterface.onError(e.toString());
                                e.printStackTrace();
                            }
                            break;
                        // 已配对
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                Log.i(TAG, "onReceive: 开始连接:");
                                clientThread clientConnectThread = new clientThread();
                                clientConnectThread.start();
                                ready = true;
                            } catch (Exception e) {
                                bluetoothInterface.onError(e.toString());
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 获取蓝牙设备的连接状态
                int connectState = device.getBondState();
                // 已配对
                if (connectState == BluetoothDevice.BOND_BONDED) {
                    try {
                        Log.i(TAG, "onReceive: 开始连接:");
                        clientThread clientConnectThread = new clientThread();
                        clientConnectThread.start();
                        ready = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        bluetoothInterface.onError(e.toString());
                    }
                }
            }
        }
    };

    /**
     * 开启客户端
     */
    private class clientThread extends Thread {
        public void run() {
            try {
                BTSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                //连接
                Log.i(TAG, "run: 开始连接...");
                BTSocket.connect();
                Log.i(TAG, "run: 连接成功");
                //启动接受数据
                Log.i(TAG, "run: 启动接受数据");
                readThread mreadThread = new readThread();
                mreadThread.start();
                bluetoothInterface.onSuccess();
            } catch (IOException e) {
                Log.i(TAG, "run: 连接服务端异常！断开连接重新试一试");
                bluetoothInterface.onError("连接服务端异常！断开连接重新试一试");
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取数据
     */
    private class readThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = BTSocket.getInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            while (true) {
                try {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        //String s = new String(buf_data);
                        cache.append(new String(buf_data));        //拼接
                        Log.e(TAG, "run: "+cache );
                        if (cache.indexOf("{")!=-1)
                            start = cache.indexOf("{");
                        if (cache.indexOf("}")!=-1) {
                            end = cache.indexOf("}");
                            String data = cache.substring(start+1,end);
                            cache = new StringBuffer(cache.substring(end+1));     //cache去掉已被返回的数据
                            bluetoothInterface.onReceive(data);
                        }
                        if (cache.length() > 10000)
                            cache = new StringBuffer();//清空
                    }
                } catch (IOException e) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 发送数据
     */
    public void sendMessage(String s) {
        if (BTSocket == null) {
            Log.e(TAG, "sendMessage: 没有连接");
            Toast.makeText(context, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = BTSocket.getOutputStream();
            os.write(s.getBytes());
            os.flush();
            Log.i(TAG, "sendMessage: 发送信息成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    设置超时，超过20s还不能和蓝牙连接成功，则报错超时
     */
    private void timeout() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }, 10000);// 20s后超时关闭
    }
}
