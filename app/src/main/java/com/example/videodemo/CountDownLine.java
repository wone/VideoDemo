package com.example.videodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by ivenzhang on 15/9/8.
 *
 * 倒计时进度条
 */
public class CountDownLine extends View {

    private static final String TAG = "CountDownLine";

    /**
     * 默认进度颜色
     */
    public static final int DEFAULT_PROGRESS_COLOR = Color.BLUE;

    /**
     * 默认持续时长
     */
    public static final long DEFAULT_DURATION = 4 * 1000;//默认4秒

    /**
     * 进度刷新间隔单位ms
     */
    public static final int REFRESH_DELAY = 60;

    public interface Callback{
        /**
         * 进度条走完的回调
         */
        public void onComplete();
    }


    private Paint mPaint;

    private int mWidth;

    private int mHeight;

    private float mProgressDelta = 1f;

    private float mProgress = 0f;

    private float mStartX;

    private float mStopX;

    private static final int MSG_TYPE_START = 0;
    private static final int MSG_TYPE_RUNNING = 1;

    private AnimationHandler mHandler;

    private WeakReference <Callback> mCallback;

    /**
     * 倒计时持续时长
     */
    private long mCountDownDuration = DEFAULT_DURATION;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CountDownLine(Context context) {
        super(context);
        init();
    }

    public CountDownLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(DEFAULT_PROGRESS_COLOR);

        mHandler = new AnimationHandler();

        //FIXME
        setBackgroundColor(Color.BLACK);
    }

    private void setProgress(float progress) {

        Log.d(TAG, "setProgress progress=" + progress);

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }

        if (progress != mProgress) {
            mProgress = progress;
            invalidate();
        }
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mWidth == 0) {
            mWidth = this.getMeasuredWidth();
            mHeight = this.getMeasuredHeight();

            Log.d(TAG, "onDraw mWidth = " + mWidth + ", mHeight=" + mHeight);
        }

        mStartX = mWidth * 0.5f * (mProgress / 100f);
        mStopX = mWidth - mStartX;

//        Log.d(TAG, "onDraw mStartX = " + mStartX);
//        Log.d(TAG, "onDraw mStopX = " + mStopX);

        canvas.drawRect(mStartX, 0, mStopX, mHeight, mPaint);
    }

    /**
     * 设置监听回调
     * @param cb
     */
    public void setCallback(Callback cb){
        if (cb == null) {
            throw new RuntimeException("Callback should not be null!");
        }

        mCallback = new WeakReference<Callback>(cb);
    }

    /**
     * 开始倒计时动画
     */
    public void startCountDown() {
        Log.d(TAG, "startCountDown");
        mHandler.sendEmptyMessage(MSG_TYPE_START);
    }

    /**
     * 终止倒计时动画
     */
    public void stopCountDown() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置倒计时的持续时间，should be called before {@link #startCountDown()}
     */
    public void setCountDownDuration(long duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration =" + duration + " should not <= 0 !");
        }
        mCountDownDuration = duration;
    }

    /**
     * 设置进度的颜色，should be called before {@link #startCountDown()}
     *
     * @param color
     */
    public void setProgressColor(int color) {
        mPaint.setColor(color);

    }

    private void onComplete(){
        Log.d(TAG, "OnComplete");

        setProgress(100);

        mHandler.removeCallbacksAndMessages(null);

        if (mCallback != null && mCallback.get() != null) {
            mCallback.get().onComplete();
        }
    }

    class AnimationHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TYPE_START) {

                setProgress(0);
                sendEmptyMessageDelayed(MSG_TYPE_RUNNING, REFRESH_DELAY);
                mProgressDelta = 100f * REFRESH_DELAY / mCountDownDuration;

                Log.d(TAG, "MSG_TYPE_START mProgressDelta：" + mProgressDelta);

            } else if (msg.what == MSG_TYPE_RUNNING) {
                if (mProgress < 100 - mProgressDelta) {

                    setProgress(mProgress + mProgressDelta);
                    sendEmptyMessageDelayed(MSG_TYPE_RUNNING, REFRESH_DELAY);
                } else {
                    onComplete();
                }
            }
        }
    }

}
