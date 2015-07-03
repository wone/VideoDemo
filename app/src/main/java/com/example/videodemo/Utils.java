package com.example.videodemo;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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
	
}
