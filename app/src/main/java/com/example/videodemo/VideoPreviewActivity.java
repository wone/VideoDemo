package com.example.videodemo;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPreviewActivity extends Activity implements OnClickListener{
	private static final String TAG = "VideoPreviewActivity";
	
	private SurfaceView mSurfaceView;
	
	private ImageView mCover;
	private TextView mCancelBtn;
	private TextView mChooseBtn;
	private ImageView mOperatorBtn;
	
	private MediaPlayer mMediaPlayer;
	
	private boolean mPreparing;
	
	final static String path = ShortVideoPlayActivity.PATH;
	
	private long duration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_videopreview);
	
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		
		mCover = (ImageView)this.findViewById(R.id.cover);
		
		mCancelBtn = (TextView)this.findViewById(R.id.cancelBtn);
		mCancelBtn.setOnClickListener(this);
		
		mChooseBtn = (TextView)this.findViewById(R.id.chooseBtn);
		mChooseBtn.setOnClickListener(this);
		
		mOperatorBtn = (ImageView)this.findViewById(R.id.operatorBtn);
		mOperatorBtn.setOnClickListener(this);
		
		mSurfaceView.getHolder().addCallback(mCallback);
		mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mCover.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener (){

			@Override
			public void onGlobalLayout() {
				int w = mCover.getWidth();
				int h = mCover.getHeight();
//				Bitmap b = Utils.getVideoThumbnail(path, w, h, MediaStore.Images.Thumbnails.MINI_KIND);
				Bitmap b = getVideoThumbnail(path, 3000);
				mCover.setImageBitmap(b);
				
				duration = Utils.getDuration(path);
				String str = Utils.stringForTime(duration);
				
				Log.d(TAG, "duration: "+duration+", str: "+str);
				Toast.makeText(getApplicationContext(), "时长："+str, Toast.LENGTH_SHORT).show();
				
				mCover.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
			
		});
	}

	
	
	private Callback mCallback = new Callback(){

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.d(TAG, "surfaceChanged");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
		}
		
	};
	
	private void play(final int msec){
		Log.d(TAG, "#play#, msec="+msec);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			
			
			File file = new File(path);
			
			Log.d(TAG, "#play#, path=" + path);
			
			if (!file.exists()) {
				Toast.makeText(this, "file not exits!", Toast.LENGTH_LONG).show();
				return ;
			}
			
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.setDisplay(mSurfaceView.getHolder());
			mPreparing = true;
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.d(TAG, "mMediaPlayer onPrepared");
					
					mCover.setVisibility(View.GONE);
					
					mPreparing = false;
					mMediaPlayer.start();
					mMediaPlayer.seekTo(msec);
					
					mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_stop);
				}
				
			});
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener (){

				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.d(TAG, "mMediaPlayer onCompletion");
					mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_play);
				}
				
			});
			
			mMediaPlayer.setOnErrorListener(new OnErrorListener(){

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.d(TAG, "mMediaPlayer onError");
					reset();
					return false;
				}
				
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private void reset(){
		Log.d(TAG, "#stop#");
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
			mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_play);
		}
	}
	
	private void stop(){
		Log.d(TAG, "#stop#");
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_play);
		}
	}		
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancelBtn:
			finish();
			break;
		case R.id.operatorBtn:
			//如果正在播放的时候
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				stop();
			} else {
				if (!mPreparing){
					play(0);
				} else {
					Log.d(TAG, "preparing...");
				}
			}
			break;
		default:
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		overridePendingTransition(0, R.anim.zoom_exit);
	}
	
	@SuppressLint("NewApi")
    private Bitmap getVideoThumbnail(String filePath, long time){
        Bitmap bitmap = null;
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(time);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        
        return bitmap;
    }
	
}
