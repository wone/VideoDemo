package com.example.videodemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

public class TestActivity extends Activity{
	private static final String TAG = "VideoPreviewActivity";
	
	private ImageView mCover;
	private TextView mText;
	
	
	final static String path = ShortVideoPlayActivity.PATH;
	
	private long duration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_test);
		
		mCover = (ImageView)this.findViewById(R.id.cover);
		mText = (TextView)this.findViewById(R.id.text);
		
		mCover.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener (){

			@Override
			public void onGlobalLayout() {
				int w = mCover.getWidth();
				int h = mCover.getHeight();
				Bitmap b = Utils.getVideoThumbnail(path, w, h, MediaStore.Images.Thumbnails.MINI_KIND);
//				Bitmap b = getVideoThumbnail(path, 1000);
				mCover.setImageBitmap(b);
				
				duration = Utils.getDuration(path);
				String str = Utils.stringForTime(duration);
				
				Log.d(TAG, "duration: "+duration+", str: "+str);
				
				mText.setText(str);
				
				mCover.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
			
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); 
	}

	
	
}
