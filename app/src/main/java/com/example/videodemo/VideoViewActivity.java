package com.example.videodemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoViewActivity extends Activity {

	private VideoView video;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoview);

		video = (VideoView) findViewById(R.id.video);
		
		
		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.video1);
		
		MediaController mc = new MediaController(this); // 创建一个MediaController对象
		
		try {
			video.setVideoURI(uri);
			video.setMediaController(mc); // 将VideoView与MediaController关联起来
			video.requestFocus(); // 设置VideoView获取焦点
			try {
				video.start(); // 播放视频
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 设置VideoView的Completion事件监听器
			video.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					Toast.makeText(VideoViewActivity.this, "视频播放完毕！",
							Toast.LENGTH_SHORT).show();
				}

			});
		} catch (Exception e) {
			Toast.makeText(this, "要播放的视频文件不存在", Toast.LENGTH_SHORT).show();
		}
	}

}
