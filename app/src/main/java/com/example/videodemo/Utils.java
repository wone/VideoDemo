package com.example.videodemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

	private static final String TAG = "Utils";

	public static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Movies" + File.separator + "5.mp4";


	 /**
	  * 获取视频的缩略图
	  * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	  * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	  * @param videoPath 视频的路径
	  * @param width 指定输出视频缩略图的宽度
	  * @param height 指定输出视频缩略图的高度度
	  * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	  *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	  * @return 指定大小的视频缩略图
	  */
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		Log.d(TAG, "w" + bitmap.getWidth());
		Log.d(TAG, "h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	/**
	 * 耗时操作，建议在子线程执行
	 * @param videoPath
	 * @return
	 */
	public static long getDuration(String videoPath){
		long duration = -1;
		
		File file = new File(videoPath);
		if (!file.exists()) {
			Log.e(TAG, "Path:"+videoPath+", not exits!");
		}
		
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(videoPath);
			mediaPlayer.prepare();
			duration = mediaPlayer.getDuration();
		}  catch (Exception e) {
			e.printStackTrace();
			duration = -1;
			Log.e(TAG, "getDuration", e);
		} finally {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		
		return duration;
	}
	
	public static String stringForTime(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /****** for proxy ****/


	/**
	 * 获取重定向后的URL，即真正有效的链接
	 * @param urlString
	 * @return
	 */
	public static String getRedirectUrl(String urlString){
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setInstanceFollowRedirects(false);
			if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_MOVED_PERM)
				return urlConnection.getHeaderField("Location");

			if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_MOVED_TEMP)
				return urlConnection.getHeaderField("Location");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlString;
	}

	public static String getExceptionMessage(Exception ex){
		String result="";
		StackTraceElement[] stes = ex.getStackTrace();
		for(int i=0;i<stes.length;i++){
			result=result+stes[i].getClassName()
					+ "." + stes[i].getMethodName()
					+ "  " + stes[i].getLineNumber() +"line"
					+"\r\n";
		}
		return result;
	}


	/**
	 * 异步清除过多的缓存文件
	 * @param dir 缓存文件的路径
	 * @param MAX 缓存上限
	 */
	public static void clearCacheFile(final String dir) {
		File cacheDir = new File(dir);
		if (cacheDir.exists() == false) {
			return;
		}
		// 防止listFiles()导致ANR
		File[] files = cacheDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
		Log.e(dir,"--------共有"+cacheDir.listFiles().length+"个缓存文件");
	}

	static public String urlToFileName(String fileName)
	{
		String str=fileName;
		str=str.replace("\\","");
		str=str.replace("/","");
		str=str.replace(":","");
		str=str.replace("*","");
		str=str.replace("?","");
		str=str.replace("\"","");
		str=str.replace("<","");
		str=str.replace(">","");
		str=str.replace("|","");
		str=str.replace(" ","");    //前面的替换会产生空格,最后将其一并替换掉
		return str;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			//如果仅仅是用来判断网络连接
			//则可以使用 cm.getActiveNetworkInfo().isAvailable();
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
