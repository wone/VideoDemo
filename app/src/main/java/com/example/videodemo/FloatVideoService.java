package com.example.videodemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FloatVideoService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "VideoDemo.FloatService";

    //http://www.androidbegin.com/tutorial/AndroidCommercial.3gp
    //http://ws.a.yximgs.com/upic/2015/07/15/17/BMjAxNTA3MTUxNzQwMjdfMTcwNjM3NjZfMjk5MDE5MjY2XzFfMw==.mp4
//    public static String VIDEO_URL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";

    public final static int PLAY_STATE_IDLE = 0;
    public final static int PLAY_STATE_PLAYING = 1;
    public final static int PLAY_STATE_PAUSE = 2;
    public final static int PLAY_STATE_ERROR = 3;

    private int mPlayState = PLAY_STATE_IDLE;

    private Context mContext;
    private WindowManager mWindowManager;
    private RelativeLayout mContainer;
    private SurfaceView mSurfaceView;

    private MediaPlayer mMediaPlayer;

    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;

    private TextView mLoadingText;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        Log.d(TAG, "onCreate");

        initFloatUI();
    }

    private void initFloatUI(){

        mContainer = new RelativeLayout(this);
        mContainer.setBackgroundColor(Color.BLUE);

        initVideo();

        TextView closeText = new TextView(this);
        closeText.setText("关闭");
        closeText.setTextColor(Color.BLUE);
        closeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatVideoService.this.stopSelf();
            }
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        mContainer.addView(closeText, lp);

        mLoadingText = new TextView(this);
        mLoadingText.setText("正在加载...");
        mLoadingText.setTextColor(Color.WHITE);
        mLoadingText.setTextSize(18);
        mLoadingText.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.CENTER_IN_PARENT);
        mContainer.addView(mLoadingText, lp1);

        showLoadingView();

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        final WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x = 0;
        paramsF.y = 0;
        mWindowManager.addView(mContainer, paramsF);

        mContainer.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.d(TAG, "onTouch");

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(v, paramsF);
                        break;
                }
                return false;
            }
        });

    }

    private void initVideo(){

        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(mCallback);
        mSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mSurfaceViewWidth = mSurfaceView.getWidth();
                mSurfaceViewHeight = mSurfaceView.getHeight();

                Log.d(TAG, "onGlobalLayout,mSurfaceViewWidth:" + mSurfaceViewWidth + ",mSurfaceViewHeight:" + mSurfaceViewHeight);

                mSurfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.addView(mSurfaceView, lp);
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback(){

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
            play(0);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            releaseMediaPlayer();
        }

    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.i(TAG, "MediaPlayer onBufferingUpdate, percent=" + percent);
        }
    };

    void play(final int msec) {

        try {
            showLoadingView();

            Log.d(TAG, "#play#, msec=" + msec);

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferListener);

            String urlPath="android.resource://" + mContext.getPackageName()+ "/" + R.raw.video5;
            mMediaPlayer.setDataSource(mContext, Uri.parse(urlPath));
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "mMediaPlayer onPrepared: ");

                    dismissLoadingView();

                    adjustVideoRatio();

                    mMediaPlayer.start();

                    //按照初始位置播放
                    if (msec > 0) {
                        mMediaPlayer.seekTo(msec);
                    }

                    changePlayState(PLAY_STATE_PLAYING);

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "#play#, msec=" + msec, e);
            reset();
        }
    }


    public static int dp2px(int dp, Resources res){
        return (int) (dp * res.getDisplayMetrics().density + 0.5);
    }

    /**
     * 这个方法要在mediaPlayer的prepare方法之后才能调用，否则得到的getVideoWidth和getVideoHeight都是0
     */
    void adjustVideoRatio(){
        int videoWith = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();

        Log.d(TAG, "adjustVideoRatio, videoWith:" + videoWith + ",videoHeight:" + videoHeight );

        if (mSurfaceViewHeight == 0 || mSurfaceViewWidth == 0 || mMediaPlayer == null
                || videoWith == 0 || videoHeight == 0) {
            return;
        }

//        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
//        Log.d(TAG, "initFloatUI(): width = "+displayMetrics.widthPixels + ", height = " + displayMetrics.heightPixels);
//
//        mSurfaceViewHeight = displayMetrics.heightPixels/3;
//        mSurfaceViewWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(videoWith*2, videoHeight*2);
        mSurfaceView.setLayoutParams(lp);

//        float r1 = (float)mSurfaceViewHeight / (float)mSurfaceViewWidth;
//        float r2 = (float)videoHeight / (float)videoWith;
//
//        if (r1 > r2) {
//            // 如果屏幕比视频文件显得更瘦，则调节其上下的空余
//            int h = (int)((float)mSurfaceViewWidth * (float)videoHeight / (float)videoWith);
//            int margin = (int)(((float)mSurfaceViewHeight - (float)h) / 2f);
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.setMargins(0, margin, 0, margin);
//            mSurfaceView.setLayoutParams(lp);
//        } else {
//            // 如果屏幕比视频文件显得更胖，则调节其左右的空余
//            int w = (int)((float)mSurfaceViewHeight * (float)videoWith / (float)videoHeight);
//            int margin =  (int)(((float)mSurfaceViewWidth - (float)w) / 2f);
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.setMargins(margin, 0, margin, 0);
//            mSurfaceView.setLayoutParams(lp);
//        }
    }


    void pause(){
        Log.d(TAG, "#pause#");

        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mPlayState == PLAY_STATE_PLAYING) {
            mMediaPlayer.pause();
            changePlayState(PLAY_STATE_PAUSE);
        }
    }

    void resume(){
        Log.d(TAG, "#resume#");

        if (mMediaPlayer != null && mPlayState == PLAY_STATE_PAUSE) {
            mMediaPlayer.start();
            changePlayState(PLAY_STATE_PLAYING);
        }
    }

    void reset(){
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }

        changePlayState(PLAY_STATE_IDLE);
    }

    void stop(){
        Log.d(TAG, "#stop#");

        if (mMediaPlayer != null && (mPlayState == PLAY_STATE_PLAYING || mPlayState == PLAY_STATE_PAUSE)) {
            releaseMediaPlayer();
        }
    }
    void releaseMediaPlayer(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        changePlayState(PLAY_STATE_IDLE);
    }


    String getPlayStateStr(int state){
        switch (state) {
            case PLAY_STATE_IDLE:
                return " idle ";
            case PLAY_STATE_PLAYING:
                return " playing ";
            case PLAY_STATE_PAUSE:
                return " pause ";
            case PLAY_STATE_ERROR:
                return " error ";
            default:
                return "null";
        }
    }

    void changePlayState(int playState){
        if ((playState != PLAY_STATE_IDLE)
                && (playState != PLAY_STATE_PLAYING)
                && (playState != PLAY_STATE_PAUSE)
                && (playState != PLAY_STATE_ERROR)) {
            return;
        }
        mPlayState = playState;

        if (playState == PLAY_STATE_PLAYING) {
//            mCover.setVisibility(View.GONE);
//            mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_stop);
        } else {
//            mOperatorBtn.setImageResource(R.drawable.qq_shortvideo_preview_play);
        }

        Log.d(TAG, "changePlayState, playState => " + getPlayStateStr(playState));
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
//        changePlayState(PLAY_STATE_IDLE);

        play(0);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError, what=" + what + ", extra=" + extra);

        changePlayState(PLAY_STATE_ERROR);

        reset();

        dismissLoadingView();

        return false;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        mWindowManager.removeView(mContainer);
    }

    void showLoadingView() {
        if (mLoadingText.getVisibility() != View.VISIBLE) {
            Drawable loadingDrawable = this.getResources().getDrawable(R.drawable.common_loading6);
            mLoadingText.setCompoundDrawablePadding(10);
            mLoadingText.setCompoundDrawablesWithIntrinsicBounds(loadingDrawable, null, null, null);

            mLoadingText.setVisibility(View.VISIBLE);

            ((Animatable) loadingDrawable).start();
        }
    }

    void dismissLoadingView() {
        if (mLoadingText.getVisibility() == View.VISIBLE) {
            mLoadingText.setVisibility(View.GONE);
        }
    }
}
