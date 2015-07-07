package com.example.videodemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class VideoOnlineActivity extends Activity implements OnClickListener, OnCompletionListener, OnErrorListener, SurfaceHolder.Callback {
    static final String TAG = "VideoDemo.VideoOnlineActivity";

    public static String VIDEO_URL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";

    RelativeLayout mRoot;
    SurfaceView mSurfaceView;

    View mTitleBar;
    View mOperatorBar;

    ImageView mMenuBtn;

    TextView mProgressTime;
    SeekBar mSeekBar;
    ImageView mOperatorBtn;

    MediaPlayer mMediaPlayer;

    public final static int PLAY_STATE_IDLE = 0;
    public final static int PLAY_STATE_PLAYING = 1;
    public final static int PLAY_STATE_PAUSE = 2;
    public final static int PLAY_STATE_ERROR = 3;

    int mPlayState = PLAY_STATE_IDLE;

    boolean mIsPlaying;

    String mVideoPath = VIDEO_URL;
    int mDuration = -1;// 单位ms

    String mDurationStr;

    // 进度条延迟隐藏的时间间隔
    final static int OPERATOR_BAR_HIDE_DELAY = 2500;

    // 进度条刷新时间间隔
    final static int PROGRESS_REFRESH_INTERNAL = 500;

    final static int MSG_TYPE_SAVE_FAILURE = 1;
    final static int MSG_TYPE_SAVE_SUCCESS = 2;
    final static int MSG_TYPE_SAVE_EXITS = 3;

    int mSurfaceViewWidth;
    int mSurfaceViewHeight;

    int mCurrentPosition;

    final static String STATE_PLAY_POSITION = "state_play_position";

    // 播放成功或是失败都只上报一次

    private boolean mNeedRestore = false;
    private Bitmap mThumbBitmap;
    private ImageView mCoverIV;

    final Handler mHandler = new Handler();


    final Runnable mProgressChecker = new Runnable() {

        @Override
        public void run() {
            // 更新进度
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                if (mDuration <= 0) {
                    initDuration();
                }

                int cur = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(cur);
            }
            mHandler.postDelayed(mProgressChecker, PROGRESS_REFRESH_INTERNAL);
        }
    };

    final Runnable mStartHidingRunnable = new Runnable() {

        @Override
        public void run() {
//			if (Log.isColorLevel()) {
            Log.d(TAG,  "mStartHidingRunnable run");
//			}

            startHiding();
        }
    };

    boolean mHidden = true;

    Animation mTopDownAnimation;
    Animation mTopUpAnimation;
    Animation mBottomDownAnimation;
    Animation mBottomUpAnimation;

    AnimationListener mShowAnimationListener = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mTitleBar.setVisibility(View.VISIBLE);
            mOperatorBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    AnimationListener mHideAnimationListener = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mHidden = true;
            mTitleBar.setVisibility(View.INVISIBLE);
            mOperatorBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    // 监听screen状态广播和音视频通话开始广播，需要暂停视频播放
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//        	if (Log.isColorLevel()) {
            Log.d(TAG,  "onReceive ===>" + action);
//        	}

            if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏或者音视频通话开始

                pause();

                if (mTitleBar != null) {
                    mTitleBar.setVisibility(View.VISIBLE);
                }

                if (mOperatorBar != null) {
                    mOperatorBar.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_play);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(STATE_PLAY_POSITION);
        }

        initData(super.getIntent());

        mRoot = (RelativeLayout) findViewById(R.id.root);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mSurfaceViewWidth = mRoot.getWidth();
                mSurfaceViewHeight = mRoot.getHeight();

                Log.d(TAG, "onGlobalLayout,mSurfaceViewWidth:" + mSurfaceViewWidth + ",mSurfaceViewHeight:" + mSurfaceViewHeight);

                mRoot.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }

        });


        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mTitleBar = findViewById(R.id.titleBar);
        mOperatorBar = findViewById(R.id.operatorBar);
        mCoverIV = (ImageView) findViewById(R.id.coverIV);

        mMenuBtn = (ImageView) findViewById(R.id.menuBtn);
        mMenuBtn.setOnClickListener(this);

        mProgressTime = (TextView) findViewById(R.id.progressTime);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mOperatorBtn = (ImageView) findViewById(R.id.operatorBtn);
        mOperatorBtn.setOnClickListener(this);

        mSurfaceView.setOnClickListener(this);
        //fix 2.x 机型播放短视频异常问题
        //设置mSurfaceView不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送过来
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mTopUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_top_up);
        mTopDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_top_down);

        mBottomUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_up);
        mBottomDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_down);

        mTopDownAnimation.setAnimationListener(mShowAnimationListener);
        mBottomUpAnimation.setAnimationListener(mShowAnimationListener);

        mTopUpAnimation.setAnimationListener(mHideAnimationListener);

        //监听锁屏和音视频通话的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

    }



    void initData(Intent intent) {
        if (TextUtils.isEmpty(mVideoPath)) {
            Log.e(TAG, "initData(), mVideoPath or mMD5 is empty, finish");
            super.finish();
        }


        Log.d(TAG, "mVideoPath=" + mVideoPath);

//		File file = new File(mVideoPath);
//		if (!file.exists()) {
//		    Log.e(TAG, "video file not exists!");
//		}

    }

    void startShowing() {
        Log.d(TAG, "startShowing : mHidden = " + mHidden);

        if (!mHidden) {
            return;
        }

        cancelHiding();

        mTitleBar.clearAnimation();
        mOperatorBar.clearAnimation();

        //不加上这两行代码在2.3手机上不会执行显示动画  WTF..
//		if (!VersionUtils.isIceScreamSandwich()) {
        mTitleBar.setVisibility(View.VISIBLE);
        mOperatorBar.setVisibility(View.VISIBLE);
//		}

        mTitleBar.startAnimation(mTopDownAnimation);
        mOperatorBar.startAnimation(mBottomUpAnimation);
        mHidden = false;
    }

    void delayStartHiding() {
        Log.d(TAG,  "delayStartHiding");

        cancelHiding();
        if (mPlayState == PLAY_STATE_PLAYING && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mHandler.postDelayed(mStartHidingRunnable, OPERATOR_BAR_HIDE_DELAY);
        }
    }

    void startHiding() {
        Log.d(TAG,  "startHiding : mHidden = " + mHidden + ",playState:" + getPlayStateStr(mPlayState));

        if (mHidden) {
            return;
        }

        // 其他状态不隐藏底部Bar
        if (mPlayState != PLAY_STATE_PLAYING) {
            return;
        }

        if (mOperatorBar.getVisibility() == View.VISIBLE) {
            mOperatorBar.clearAnimation();
            mOperatorBar.startAnimation(mBottomDownAnimation);
        }

        if (mTitleBar.getVisibility() == View.VISIBLE) {
            mTitleBar.clearAnimation();
            mTitleBar.startAnimation(mTopUpAnimation);
        }
    }

    void cancelHiding() {
        mHandler.removeCallbacks(mStartHidingRunnable);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //如果从暂停中恢复，需要恢复播放现场
        if (mNeedRestore) {

            Log.d(TAG, "onResume, restore last pause....,mCurrentPosition="+mCurrentPosition);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mVideoPath);

            mThumbBitmap = retriever.getFrameAtTime(mCurrentPosition * 1000);

            mCoverIV.setVisibility(View.VISIBLE);
            mCoverIV.setImageBitmap(mThumbBitmap);

            if (mHidden) {
                mOperatorBar.setVisibility(View.VISIBLE);
                mTitleBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
//		if (VersionUtils.isrFroyo()) {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//		}

        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
//		if (VersionUtils.isrFroyo()) {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).abandonAudioFocus(null);
//		}
        super.onStop();

        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();

            Log.d(TAG,  "pause mCurrentPosition:" + mCurrentPosition);
        }

        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }

        outState.putInt(STATE_PLAY_POSITION, mCurrentPosition);

        Log.d(TAG, "onSaveInstanceState: mCurrentPosition: " + mCurrentPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG,  "onDestroy");

        // 避免内存泄露
        mHandler.removeCallbacksAndMessages(null);

        releaseMediaPlayer();

        unregisterReceiver(mReceiver);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: mCurrentPosition:" + mCurrentPosition + ", playState:" + getPlayStateStr(mPlayState) + ", mNeedRestore:" + mNeedRestore);

        //如果暂停切后台，再切回前台的话，需要保持暂停
        if (mNeedRestore) {
            return;
        }

        // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
        if (mCurrentPosition > 0) {
            play(mCurrentPosition);
            mCurrentPosition = 0;
        } else {
            play(0);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }

        Log.d(TAG, "surfaceDestroyed mCurrentPosition:" + mCurrentPosition + ", playState:" + getPlayStateStr(mPlayState));

        if (mPlayState == PLAY_STATE_PAUSE) {
            mNeedRestore = true;
        }


        Log.d(TAG, "surfaceDestroyed,  mCurrentPosition = " + mCurrentPosition + ",mNeedRestore = "+mNeedRestore);

        releaseMediaPlayer();
    }

    void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        changePlayState(PLAY_STATE_IDLE);
    }

    void initDuration() {
        int duration = mMediaPlayer.getDuration();

        Log.d(TAG, "initDuration: duration=" + duration);

        if (duration != 0) {
            mDuration = duration;
            mSeekBar.setMax(mDuration);
            mDurationStr = stringForTime(mDuration);
        }
    }

    String getPlayStateStr(int state) {
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

    void changePlayState(int playState) {
        if ((playState != PLAY_STATE_IDLE)
                && (playState != PLAY_STATE_PLAYING)
                && (playState != PLAY_STATE_PAUSE)
                && (playState != PLAY_STATE_ERROR)) {
            return;
        }
        mPlayState = playState;

        if (playState == PLAY_STATE_PLAYING) {
            mIsPlaying = true;
            mOperatorBtn.setImageResource(R.drawable.qq_player_stop);

            mHandler.post(mProgressChecker);
        } else {
            mIsPlaying = false;
            mOperatorBtn.setImageResource(R.drawable.qq_player_start);

            mHandler.removeCallbacks(mProgressChecker);
        }

        Log.d(TAG, "changePlayState, playState=" + getPlayStateStr(playState) + ", mIsPlaying=" + mIsPlaying);
    }


    /**
     * 根据视频原始比例和布局宽高，计算最终应该显示的布局参数
     * @return
     */
    void adjustVideoRatio(){
        int videoWith = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();

        Log.d(TAG,  "adjustVideoRatio, videoWith:" + videoWith + ",videoHeight:" + videoHeight);

        if (mSurfaceViewHeight == 0 || mSurfaceViewWidth == 0 || videoWith == 0 || videoHeight == 0) {
            return ;
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        float r1 = (float) mSurfaceViewHeight / (float) mSurfaceViewWidth;
        float r2 = (float) videoHeight / (float) videoWith;

        if (r1 > r2) {
            // 如果屏幕比视频文件显得更瘦，则调节其上下的空余
            int h = (int) ((float) mSurfaceViewWidth * (float) videoHeight / (float) videoWith);
            int margin = (int) (((float) mSurfaceViewHeight - (float) h) / 2f);

            lp.setMargins(0, margin, 0, margin);
            mSurfaceView.setLayoutParams(lp);
        } else {
            // 如果屏幕比视频文件显得更胖，则调节其左右的空余
            int w = (int) ((float) mSurfaceViewHeight * (float) videoWith / (float) videoHeight);
            int margin = (int) (((float) mSurfaceViewWidth - (float) w) / 2f);
            lp.setMargins(margin, 0, margin, 0);
            mSurfaceView.setLayoutParams(lp);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "MediaPlayer onCompletion");

        mSeekBar.setProgress(mDuration);

        changePlayState(PLAY_STATE_IDLE);
        startShowing();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG,  "MediaPlayer onError what=" + what + ",extra=" + extra);

        changePlayState(PLAY_STATE_ERROR);

        reset();

        handleError();

        return false;
    }

    void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }

        changePlayState(PLAY_STATE_IDLE);
    }

    private MediaPlayer.OnBufferingUpdateListener mOnBufferListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.i(TAG, "MediaPlayer onBufferingUpdate, percent=" + percent);
        }
    };

    void play(final int msec) {

        try {
            Log.d(TAG,  "#play#, msec=" + msec);

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferListener);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener(){

                @Override
                public void onPrepared(MediaPlayer mp) {

                    Log.d(TAG, "MediaPlayer onPrepared: mDuration=" + mDuration);

                    if (mDuration <= 0) {
                        initDuration();
                    }

                    adjustVideoRatio();

                    mMediaPlayer.start();

                    //按照初始位置播放
                    if (msec > 0) {
                        mMediaPlayer.seekTo(msec);
                        mSeekBar.setProgress(msec);
                    }

                    changePlayState(PLAY_STATE_PLAYING);

                    delayStartHiding();

                    if (mMediaPlayer.getVideoWidth() == 0 ||  mMediaPlayer.getVideoHeight() == 0) {
                        changePlayState(PLAY_STATE_ERROR);
                        handleError();
                        return;
                    }
                }

            });


        } catch (Exception e) {
            Log.e(TAG,  "#play#, msec=" + msec, e);

            reset();

            handleError();
        }
    }

    void handleError() {
        Toast.makeText(this, "Play Error...", Toast.LENGTH_SHORT).show();
    }

    void stop() {
        Log.d(TAG, "#stop#");

        if (mMediaPlayer != null && (mPlayState == PLAY_STATE_PLAYING || mPlayState == PLAY_STATE_PAUSE)) {
            releaseMediaPlayer();
        }
    }

    void pause() {
        Log.d(TAG, "#pause#");

        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mPlayState == PLAY_STATE_PLAYING) {
            //记录暂停的位置
            mCurrentPosition = mMediaPlayer.getCurrentPosition();

            mMediaPlayer.pause();
            changePlayState(PLAY_STATE_PAUSE);
        }

        if (mHidden) {
            startShowing();
        }
    }

    void resume() {
        Log.d(TAG,  "#resume#");

        if (mMediaPlayer != null && mPlayState == PLAY_STATE_PAUSE) {
            mMediaPlayer.start();
            changePlayState(PLAY_STATE_PLAYING);
        }
    }

    OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                Log.d(TAG,  "onProgressChanged: progress = " + progress + ",fromUser=" + fromUser);
            }

            setProgessTime(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 取得当前进度条的刻度
            int progress = mSeekBar.getProgress();

            Log.d(TAG,  "onStartTrackingTouch: progress = " + progress);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 当进度条停止修改的时候触发
            // 取得当前进度条的刻度
            int progress = mSeekBar.getProgress();

            Log.d(TAG, "onStopTrackingTouch: progress = " + progress);

            if (mPlayState == PLAY_STATE_PLAYING || mPlayState == PLAY_STATE_PAUSE) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(progress);
                }
            } else {
                play(progress);
            }

        }
    };

    void handleClick() {
        Log.d(TAG,  "handleClick: mPlayState = " + getPlayStateStr(mPlayState) + ", mCurrentPosition="+mCurrentPosition);

        switch (mPlayState) {
            case PLAY_STATE_IDLE:
                if (mNeedRestore) {
                    play(mCurrentPosition);
                    mCoverIV.setVisibility(View.GONE);
                    mNeedRestore = false;
                } else {
                    play(0);
                }

                break;
            case PLAY_STATE_PLAYING:
                pause();
                break;
            case PLAY_STATE_PAUSE:
                resume();
                break;
            case PLAY_STATE_ERROR:
                play(0);
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.surfaceView:

                Log.d(TAG,  "onClick surfaceView, mHidden=" + mHidden);

                if (mHidden) {
                    startShowing();
                    delayStartHiding();
                } else {
                    startHiding();
                }
                break;
            case R.id.operatorBtn:
                handleClick();
                break;
            case R.id.menuBtn:

                if (mPlayState == PLAY_STATE_PLAYING) {
                    pause();
                }
                Toast.makeText(this,"click menu...", Toast.LENGTH_SHORT).show();
                break;
        }
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

    void setProgessTime(int millis) {
        if (mProgressTime != null) {
            mProgressTime.setText(stringForTime(millis) + "/" + mDurationStr);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
