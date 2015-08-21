package com.example.videodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;

public class MainActivity extends Activity implements OnClickListener{

	private FadingView mBgView;
	private SeekBar mSeekBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSeekBar = (SeekBar) findViewById(R.id.seekBar);
		mSeekBar.setMax(100);
		
		this.findViewById(R.id.previewBtn).setOnClickListener(this);
		this.findViewById(R.id.playBtn).setOnClickListener(this);
		this.findViewById(R.id.floatVideoBtn).setOnClickListener(this);
        this.findViewById(R.id.floatVideo2Btn).setOnClickListener(this);
		this.findViewById(R.id.videoOnlineBtn).setOnClickListener(this);
		this.findViewById(R.id.musicOnlineBtn).setOnClickListener(this);
		this.findViewById(R.id.videoCacheBtn).setOnClickListener(this);

//		Log.e("VideoDemo", "testBegin:" + "\r\n\r\n" + ":testEnd");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.previewBtn:
			startActivity(new Intent(this, VideoPreviewActivity.class));
			overridePendingTransition(R.anim.zoom_enter, 0);
			break;
		case R.id.playBtn:
			startActivity(new Intent(this, ShortVideoPlayActivity.class));
//			overridePendingTransition(R.anim.zoom_enter, 0); 
//			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); 
			break;

		case R.id.floatVideoBtn:
			startActivity(new Intent(this, FloatVideoActivity.class));
			break;
        case R.id.floatVideo2Btn:
            startActivity(new Intent(this, FloatVideo2Activity.class));
            break;
		case R.id.videoOnlineBtn:
			startActivity(new Intent(this, VideoOnlineActivity.class));
			break;
		case R.id.musicOnlineBtn:
			startActivity(new Intent(this, MusicOnlineActivity.class));
			break;
		case R.id.videoCacheBtn:
			startActivity(new Intent(this, VideoActivity.class));
			break;
		}
	}

}
