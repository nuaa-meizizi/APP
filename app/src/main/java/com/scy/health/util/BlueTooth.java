package com.scy.health.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class BlueTooth {
    private BluetoothSocket BTSocket;
    private Context context;
    private BluetoothAdapter BTAdapter;
    private BluetoothDevice device;

    private BlueTooth(Context context) {
        this.context = context;
    }

    public void start(){
        checkBT(context);
        registerBTReceiver();
    }

    public void stop() throws IOException {
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
            System.out.println("本地设备驱动异常!");
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
        context.unregisterReceiver(BTReceive);
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
                System.out.println("客户端:找到的BT名:" + device.getName());
                // 如果查找到的设备符合要连接的设备，处理
                if (device.getName().equalsIgnoreCase("NUAACS")) {
                    System.out.println("客户端:配对"+device.getName());
                    // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                    BTAdapter.cancelDiscovery();
                    // 获取蓝牙设备的连接状态
                    int connectState = device.getBondState();
                    switch (connectState) {
                        // 未配对
                        case BluetoothDevice.BOND_NONE:
                            System.out.println("客户端:开始配对:");
                            try {
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        // 已配对
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                System.out.println("客户端:开始连接:");
                                clientThread clientConnectThread = new clientThread();
                                clientConnectThread.start();
                            } catch (Exception e) {
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
                        System.out.println("客户端:开始连接:");
                        clientThread clientConnectThread = new clientThread();
                        clientConnectThread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(action);
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
                System.out.println("客户端:开始连接...");
                BTSocket.connect();
                System.out.println("客户端:连接成功");
                Toast.makeText(context,"蓝牙连接成功",Toast.LENGTH_SHORT);
                //启动接受数据
                System.out.println("客户端:启动接受数据");
                readThread mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                System.out.println("客户端:连接服务端异常！断开连接重新试一试");
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
                        String s = new String(buf_data);
                        System.out.println(s);
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
            Toast.makeText(context, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = BTSocket.getOutputStream();
            os.write(s.getBytes());
            os.flush();
            System.out.println("客户端:发送信息成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}