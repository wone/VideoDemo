package com.example.videodemo.proxy;


import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载模块
 *
 * @author hellogv
 */
public class DownloadThread extends Thread {
    static private final String TAG = "VideoDemo.DownloadThread";
    private String mUrl;
    private String mPath;

    private int mDownloadSize;

    private int mTargetSize;
    private boolean mStop, mDeleteFile;
    private boolean mDownloading;
    private boolean mStarted;
    private boolean mError;

    public DownloadThread(String url, String savePath, int targetSize) {
        mUrl = url;
        mPath = savePath;
        mTargetSize = targetSize;
        mDownloadSize = 0;

        mStop = false;
        mDeleteFile = false;
        mDownloading = false;
        mStarted = false;
        mError = false;
    }

    @Override
    public void run() {
        mDownloading = true;
        download();
    }

    /**
     * 启动下载线程
     */
    public void startThread() {
        if (!mStarted) {
            this.start();

            // 只能启动一次
            mStarted = true;
        }
    }

    /**
     * 停止下载线程, deleteFile是否要删除临时文件
     */
    public void stopThread(boolean deleteFile) {
        mStop = true;
        mDeleteFile = deleteFile;

    }

    /**
     * 是否正在下载
     */
    public boolean isDownloading() {
        return mDownloading;
    }

    /**
     * 是否下载异常
     *
     * @return
     */
    public boolean isError() {
        return mError;
    }

    public int getDownloadedSize() {
        return mDownloadSize;
    }

    /**
     * 是否下载成功
     */
    public synchronized boolean isDownloadSuccessed() {
        return (mDownloadSize != 0 && mDownloadSize == mTargetSize);
    }

    private synchronized void download() {
        int mTotalSize = 0;
        InputStream is = null;
        FileOutputStream os = null;
        if (mStop) {
            return;
        }
        try {
            URL url = new URL(mUrl);
            URLConnection con = url.openConnection();

            mTotalSize = con.getContentLength();
            is = con.getInputStream();
            os = new FileOutputStream(mPath);
            Log.e(TAG, mPath);
            int len = 0;
            byte[] bs = new byte[1024];
            if (mStop) {
                return;
            }
            while (!mStop //未强制停止
                    && mDownloadSize < mTargetSize //未下载足够
                    && ((len = is.read(bs)) != -1)) {//未全部读取
                os.write(bs, 0, len);
                mDownloadSize += len;
            }
        } catch (Exception e) {
            mError = true;
            Log.e(TAG, "download error:" + e.toString() + "");
            Log.e(TAG, ProxyUtils.getExceptionMessage(e));
        } finally {
            Log.e(TAG, "Download Over....., mTotalSize:" + mTotalSize + ",mTargetSize:" + mTargetSize + ", mDownloadSize:"+mDownloadSize);
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            if (mDeleteFile) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }

                File file = new File(mPath);
                file.delete();
            }
            mDownloading = false;
        }
    }
}
