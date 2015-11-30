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

/**
 * Created by ivenzhang on 15/10/8.
 */
public class BreathView extends View {

    public static final String TAG = "BreathView";

    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_GROW = 1;
    public static final int ANIMATION_BREATH = 2;
    public static final int ANIMATION_SHRINK = 3;

    protected int mAinmationStep;

    private Paint mPaint;
    private Paint mPaint2;

    public static final int MSG_ANIMATION_APLPHA = 1;
    public static final int MSG_ANIMATION_SCALE = 2;

    public boolean mDrawAlpha = false;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MSG_ANIMATION_SCALE:
                    requestLayout();
                    break;

                case MSG_ANIMATION_APLPHA:
                    mDrawAlpha = !mDrawAlpha;
                    invalidate();

                    mHandler.sendEmptyMessageDelayed(MSG_ANIMATION_APLPHA, 2000);
                    break;
            }
        }
    };

    private int RADIUS_BIG = 142;
    private int RADIUS_NORMAL = 128;

    private int mRadius = RADIUS_NORMAL;

    private int mOutCircleWidth = 10;

//    private boolean mIsAlpha = false;

    private boolean mEnableBreathEffect = true;

    public BreathView(Context context){
        super(context);
        init();
    }

    public BreathView(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public BreathView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init();
    }

    private void init(){

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mOutCircleWidth);
        mPaint.setAntiAlias(true);

        mPaint2 = new Paint();
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setColor(0x51000000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(2 * mRadius, 2 * mRadius);
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw mIsAlpha =" + mDrawAlpha);

        if (mDrawAlpha) {
            mPaint.setAlpha((int) (255*0.5));
        } else {
            mPaint.setAlpha(255);
        }

        canvas.drawCircle(mRadius, mRadius, mRadius - mOutCircleWidth, mPaint);
        canvas.drawCircle(mRadius, mRadius, mRadius - mOutCircleWidth, mPaint2);
    }

    volatile private boolean mRun;

    public boolean isAnimationRuning(){
        return mRun;
    }

    private void startAnimationThread(){

        new Thread(new Runnable() {

            @Override
            public void run() {

                Log.d(TAG, "AnimationThread start...");

                mRun = true;

                while (true) {

                    if (mAinmationStep == ANIMATION_NONE) {
                        break;
                    } else if (mAinmationStep == ANIMATION_GROW) {
                        if (mRadius < RADIUS_BIG) {
                            mRadius += 1;
                        } else {
                            mRadius = RADIUS_BIG;
                            mAinmationStep = ANIMATION_BREATH;
                        }

                        mHandler.sendEmptyMessage(MSG_ANIMATION_SCALE);
                    } else if (mAinmationStep == ANIMATION_SHRINK) {
                        if (mRadius > RADIUS_NORMAL) {
                            mRadius -= 1;
                        } else {
                            mRadius = RADIUS_NORMAL;
                            mAinmationStep = ANIMATION_BREATH;
                        }

                        mHandler.sendEmptyMessage(MSG_ANIMATION_SCALE);
                    } else {
                        break;
                    }

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mRun = false;

                Log.d(TAG, "AnimationThread exit...");
            }

        }).start();
    }

    public void startGrowAnimation(){
        mAinmationStep = ANIMATION_GROW;
        mRadius = RADIUS_NORMAL;

        if (!mRun) {
            startAnimationThread();
        }

        mHandler.sendEmptyMessage(MSG_ANIMATION_APLPHA);


        Log.d(TAG, "startGrowAnimation...");
    }

    public void stopAnimation(){
        mAinmationStep = ANIMATION_NONE;
    }

    public boolean isNormalSize(){
        return mRadius == RADIUS_NORMAL;
    }

    public void startShrinkAnimation(){

        Log.d(TAG, "startShrinkAnimation... mRun = " + mRun);

        mAinmationStep = ANIMATION_SHRINK;
        if (!mRun) {
            startAnimationThread();
        }
    }

}
