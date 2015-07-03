package com.example.videodemo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;



public class FloatVideo2Activity extends Activity  {

    private static final String TAG = "VideoDemo.FloatService";

    public final static int PLAY_STATE_IDLE = 0;
    public final static int PLAY_STATE_PLAYING = 1;
    public final static int PLAY_STATE_PAUSE = 2;
    public final static int PLAY_STATE_ERROR = 3;

    private  static int mPlayState = PLAY_STATE_IDLE;

    private static Context mContext;
    private static WindowManager mWindowManager;
    private static RelativeLayout mContainer;
    private static SurfaceView mSurfaceView;

    private static MediaPlayer mMediaPlayer;

    private static int mSurfaceViewWidth;
    private static int mSurfaceViewHeight;


//    private static String mVideoPath = Utils.PATH;

    private static boolean mRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_video);

        mContext = this.getApplicationContext();


        final Button operateBtn = (Button) findViewById(R.id.operateBtn);

        if (mRunning) {
            operateBtn.setText("stop");
        } else {
            operateBtn.setText("start");
        }

        operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRunning) {

                    if (initFloatUI()) {
                        mRunning = true;
                        operateBtn.setText("stop");
                    }

                } else {
                    removeFloatUI();

                    mRunning = false;
                    operateBtn.setText("start");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_float_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p/>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();

        mContext = this.getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mContext = this.getApplicationContext();
    }


    private static boolean initFloatUI(){
        if (mContext == null) {
            Log.e(TAG, "initFloatUi, mContext == null");
            return false;
        }

        mContainer = new RelativeLayout(mContext);
        mContainer.setBackgroundColor(Color.BLUE);
//        mContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                removeFloatUI();
//            }
//        });
//        mContainer.setBackgroundResource(R.drawable.ic_launcher);


        initVideo();

        mWindowManager = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);

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

        return true;
    }

    static void removeFloatUI(){
        if (mWindowManager != null && mContainer != null) {
            mWindowManager.removeView(mContainer);
//            mContainer = null;
//            mWindowManager = null;
        }
    }

    private static void initVideo(){
//        if (TextUtils.isEmpty(mVideoPath)) {
//            Toast.makeText(mContext, mVideoPath + " empty!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        File file = new File(mVideoPath);
//        if (!file.exists() || file.length() == 0) {
//            Toast.makeText(mContext, mVideoPath + " no exists!", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, mVideoPath + " no exists!");
//            return;
//        }

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

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.addView(mSurfaceView, lp);
    }

    private static SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback(){

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

    static void  play(final int msec) {

        try {
            Log.d(TAG, "#play#, msec=" + msec);

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play(0);
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "onError, what=" + what + ", extra=" + extra);

                    changePlayState(PLAY_STATE_ERROR);

                    reset();

                    return false;
                }
            });

            String urlPath="android.resource://" + mContext.getPackageName()+ "/" + R.raw.video5;
            mMediaPlayer.setDataSource(mContext, Uri.parse(urlPath));
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "mMediaPlayer onPrepared: ");

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
    static void adjustVideoRatio(){
        int videoWith = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();

        Log.d(TAG, "adjustVideoRatio, videoWith:" + videoWith + ",videoHeight:" + videoHeight );

        if (mSurfaceViewHeight == 0 || mSurfaceViewWidth == 0 || mMediaPlayer == null
                || videoWith == 0 || videoHeight == 0) {
            return;
        }

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
//                    RelativeLayout.LayoutParams.FILL_PARENT,
//                    RelativeLayout.LayoutParams.FILL_PARENT);
//            lp.setMargins(0, margin, 0, margin);
//            mSurfaceView.setLayoutParams(lp);
//        } else {
//            // 如果屏幕比视频文件显得更胖，则调节其左右的空余
//            int w = (int)((float)mSurfaceViewHeight * (float)videoWith / (float)videoHeight);
//            int margin =  (int)(((float)mSurfaceViewWidth - (float)w) / 2f);
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.FILL_PARENT,
//                    RelativeLayout.LayoutParams.FILL_PARENT);
//            lp.setMargins(margin, 0, margin, 0);
//            mSurfaceView.setLayoutParams(lp);
//        }
    }


    static void pause(){
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

   static void reset(){
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }

        changePlayState(PLAY_STATE_IDLE);
    }

    static void stop(){
        Log.d(TAG, "#stop#");

        if (mMediaPlayer != null && (mPlayState == PLAY_STATE_PLAYING || mPlayState == PLAY_STATE_PAUSE)) {
            releaseMediaPlayer();
        }
    }
   static void releaseMediaPlayer(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        changePlayState(PLAY_STATE_IDLE);
    }


   static String getPlayStateStr(int state){
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

    static void changePlayState(int playState){
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
    }
}
