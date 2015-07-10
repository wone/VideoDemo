package com.example.videodemo.proxy;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class HttpGetProxy {
    private final String TAG = "VideoDemo.HttpGetProxy";

    final private String LOCAL_IP_ADDRESS = "127.0.0.1";
    final private int HTTP_PORT = 80;

    private ServerSocket localServer = null;
    private Socket localSocket = null;
    private Socket remoteSocket = null;
    private String remoteIPAddress;

    private InputStream in_remoteSocket;
    private OutputStream out_remoteSocket;
    private InputStream in_localSocket;
    private OutputStream out_localSocket;

    private interface OnFinishListener {
        void onFinishListener();
    }

    public HttpGetProxy(int localport) {

        // --------建立代理中转服务器-----------//  
        try {
            localServer = new ServerSocket(localport, 1,
                    InetAddress.getByName(LOCAL_IP_ADDRESS));
        } catch (UnknownHostException e) {
            e.printStackTrace();

            Log.d(TAG, "HttpGetProxy()", e);
        } catch (IOException e) {
            Log.d(TAG, "HttpGetProxy()", e);
        } catch (Exception e){
            Log.d(TAG, "HttpGetProxy()", e);
        }
    }

    /**
     * 结束时，清除所有资源 
     */
    private OnFinishListener finishListener =new OnFinishListener(){

        @Override
        public void onFinishListener() {
            Log.e(TAG, "..........release all..........");

            try {
                in_localSocket.close();
                out_remoteSocket.close();

                in_remoteSocket.close();
                out_localSocket.close();

                localSocket.close();
                remoteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };



    public void startProxy(String remoteIpAddr) throws IOException {
        remoteIPAddress = remoteIpAddr;
        SocketAddress address = new InetSocketAddress(remoteIPAddress, HTTP_PORT);

        // --------连接目标服务器---------//
        remoteSocket = new Socket();
        remoteSocket.connect(address);

        in_remoteSocket = remoteSocket.getInputStream();
        out_remoteSocket = remoteSocket.getOutputStream();

        Log.e(TAG, "startProxy..........remote Server connected..........");

        /**
         * 接收本地request，并转发到远程服务器
         */
        new Thread() {
            public void run() {
                int bytes_read;
                byte[] local_request = new byte[5120];
                try {
                    // 本地Socket
                    localSocket = localServer.accept();

                    Log.d(TAG, "..........localSocket connected..........");

                    in_localSocket = localSocket.getInputStream();
                    out_localSocket = localSocket.getOutputStream();

                    Log.d(TAG, "..........init local Socket I/O..........");

                    String buffer = "";
                    while ((bytes_read = in_localSocket.read(local_request)) != -1) {
                        String str = new String(local_request);

                        Log.e(TAG, "localSocket---->"+str);

                        buffer = buffer + str;

                        if (buffer.contains("GET")
                                && buffer.contains("\r\n\r\n")) {

                            //---把request中的本地ip改为远程ip---//
                            buffer = buffer.replace(LOCAL_IP_ADDRESS, remoteIPAddress);

                            Log.d(TAG , "已经替换IP, remoteIPAddress="+remoteIPAddress);

                            out_remoteSocket.write(buffer.getBytes());
                            out_remoteSocket.flush();
                            continue;

                        } else{
                            out_remoteSocket.write(buffer.getBytes());
                            out_remoteSocket.flush();
                        }
                    }

                    Log.e(TAG, "..........local finish receive..........");
                    finishListener.onFinishListener();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }.start();

        /**
         * 接收远程服务器reply，并转发到本地客户端
         */
        new Thread() {
            public void run() {
                int bytes_read;
                byte[] remote_reply = new byte[5120];
                try {
                    Log.e(TAG,"..........remote ready to receive..........");

                    while ((bytes_read = in_remoteSocket.read(remote_reply)) != -1) {

                        //System.out.println("remoteSocket     " + remote_reply.length);
                        //System.out.println("remoteSocket     " + new String(remote_reply));

                        Log.d(TAG,"..........remote receiving..........");

                        out_localSocket.write(remote_reply, 0, bytes_read);
                        out_localSocket.flush();
                    }

                    Log.e(TAG, "..........remote finish receive..........");

                } catch (IOException e) {
                    e.printStackTrace();

                    Log.d(TAG, "", e);
                }
            }
        }.start();
    }
}  