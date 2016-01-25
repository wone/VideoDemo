package com.example.videodemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ivenzhang on 15/10/19.
 *
 *  PTV需求，模拟声音动画的组件，效果为高度随机变化的几个矩形。
 *
 */
public class AudioAnimationView extends View{

    private static final String TAG = "AudioAnimationView";

    //矩形宽度，单位dp
    private static final int DEFAULT_RECT_WIDTH = 3;

    //矩形间隔，单位dp
    private static final int DEFAULT_RECT_INTERVAL = 2;

    //矩形最大高度，单位dp
    private static final int RECT_HEIGHT_MAX = 12;

    //动画刷新间隔
    private static final int REFRESH_INTERVAL = 260;

    private static int H;

    private int mRectWidth;
    private int mRectInterval;

    private Resources mRes;

    private Paint mPaint;

    private Rect mR1;
    private Rect mR2;
    private Rect mR3;
    private Rect mR4;
    private Rect mR5;
    private Rect mR6;


    private int mH1;
    private int mH2;
    private int mH3;
    private int mH4;
    private int mH5;
    private int mH6;

    private volatile boolean mRunning = false;

    private Handler mAnimationHandler = new Handler();

    private Drawable mIcon;

    public AudioAnimationView(Context context){
        super(context);
        init();
    }

    public AudioAnimationView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public AudioAnimationView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mRes = this.getResources();

        mRectWidth = dp2px(DEFAULT_RECT_WIDTH, getResources());
        mRectInterval = dp2px(DEFAULT_RECT_INTERVAL, getResources());
        H = dp2px(RECT_HEIGHT_MAX, getResources());

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(4);
        mPaint.setAlpha((int) (0.8 * 256));

        mR1 = new Rect();
        mR2 = new Rect();
        mR3 = new Rect();
        mR4 = new Rect();
        mR5 = new Rect();
        mR6 = new Rect();

        updateValue();

        mIcon = mRes.getDrawable(R.drawable.ptv_voice);
        mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = mRunning ? 6 * mRectWidth + 5 * mRectInterval : mIcon.getIntrinsicWidth();
        int measureHeight = mRunning ? H : mIcon.getIntrinsicHeight();

        Log.d(TAG, "onMeasure(): measuredWidth = " + measuredWidth + ", measureHeight=" + measureHeight);

        setMeasuredDimension(measuredWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw(): mRunning = " + mRunning);

        if (mRunning) {
            mR1.set(0, mH1, mRectWidth, H - mH1);
            mR2.set(mRectWidth*1 + mRectInterval,       mH2,     mRectWidth*2 + mRectInterval,   H-mH2);
            mR3.set(mRectWidth*2 + mRectInterval*2,     mH3,     mRectWidth*3 + mRectInterval*2, H-mH3);
            mR4.set(mRectWidth * 3 + mRectInterval * 3, mH4, mRectWidth * 4 + mRectInterval * 3, H-mH4);
            mR5.set(mRectWidth * 4 + mRectInterval * 4, mH5, mRectWidth * 5 + mRectInterval * 4, H-mH5);
            mR6.set(mRectWidth * 5 + mRectInterval * 5, mH5, mRectWidth * 6 + mRectInterval * 5, H-mH5);

            canvas.drawRect(mR1, mPaint);
            canvas.drawRect(mR2, mPaint);
            canvas.drawRect(mR3, mPaint);
            canvas.drawRect(mR4, mPaint);
            canvas.drawRect(mR5, mPaint);
            canvas.drawRect(mR6, mPaint);
        } else {
            mIcon.draw(canvas);
        }

    }

    public static int dp2px(int dp, Resources res){
        return (int) (res.getDisplayMetrics().density * dp + 0.5f);
    }

    void updateValue(){
        int R = H / 2;
        mH1 = (int) (R * Math.random());
        mH2 = (int) (R * Math.random());
        mH3 = (int) (R * Math.random());
        mH4 = (int) (R * Math.random());
        mH5 = (int) (R * Math.random());
        mH6 = (int) (R * Math.random());
    }

    private class Animator implements Runnable{

        @Override
        public void run() {

            updateValue();
            mAnimationHandler.postDelayed(this, 260);

            AudioAnimationView.this.invalidate();
        }
    }

    public void startAnimation(){

        Log.d(TAG, "startAnimation()");

        if (!mRunning){
           mRunning = true;
           mAnimationHandler.post(new Animator());
       }
    }



    public void stopAnimation(){
        mRunning = false;
        mAnimationHandler.removeCallbacksAndMessages(null);
        requestLayout();
        Log.d(TAG, "stopAnimation()");
    }

    public boolean isAnimationRunning(){
        return mRunning;
    }
}
