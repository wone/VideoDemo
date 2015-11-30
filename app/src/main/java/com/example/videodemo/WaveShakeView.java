package com.example.videodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ivenzhang on 15/9/27.
 */
public class WaveShakeView extends View{

    private static final String TAG = "WaveShakeView";

    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_START = 1;
    public static final int ANIMATION_MOVING = 2;
    public static final int ANIMATION_END = 3;


    private int mAinmationStep = ANIMATION_NONE;

    private Paint mPaint1;
    private Paint mPaint2;

    private Path mPath;

    int mWidth;
    int mHeight;

    public WaveShakeView(Context context){
        super(context);
        init();
    }

    public WaveShakeView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public WaveShakeView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mPaint1 = new Paint();
        mPaint1.setColor(Color.WHITE);
        mPaint1.setAntiAlias(true);
        mPaint1.setStrokeWidth(2);
        mPaint1.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint();
        mPaint2.setColor(0xFFD7D7D7);
        mPaint2.setAntiAlias(true);
        mPaint2.setStrokeWidth(1);
        mPaint2.setStyle(Paint.Style.STROKE);

        mPath = new Path();
    }


    volatile int mAngle = 0;

    float A = 0;  //波幅
    int B = 0;  //波纹线原点的y坐标
    float C = 0.6f;//角速度越小，波形越稀疏
    float D = 25; //相邻曲线相位偏差

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();

//        Log.d(TAG, "onDraw(), mWdith = " + mWidth + ", mHeight = " + mHeight );

        if (B == 0) {
            B = mHeight / 2;
        }

        long start = System.currentTimeMillis();

        if (mAinmationStep != ANIMATION_NONE) {
            drawWave(canvas, mPath);
        } else {
            canvas.drawColor(0x00000000);
        }

        long cost = System.currentTimeMillis() - start;
        Log.d(TAG, "onDraw(): cost =" + cost + "ms");
    }

    private void drawWave(Canvas canvas, Path path){

        int x = 0;
        int y = 0;

        //第一条主曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint1);

        //第二条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle + D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);


        //第三条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle + 2 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);

        //第四条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle + 3 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);

        //第五条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle + 4 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);
    }


    volatile private boolean mRun;

    public boolean isAnimationRuning(){
        return mRun;
    }

    private void startAnimationThread(){
        new Thread(new Runnable() {

            @Override
            public void run() {

                mRun = true;

                while (true) {

                    if (mAinmationStep == ANIMATION_NONE) {
                        break;
                    } else if (mAinmationStep == ANIMATION_START) {

                        if (A < 32) {
                            A += 1;
                        } else {
                            mAinmationStep = ANIMATION_MOVING;
                        }

                    } else if (mAinmationStep == ANIMATION_END) {

                        if (A > 0) {
                            A -= 1;
                        } else {
                            A = 0;
                            mAinmationStep = ANIMATION_NONE;
                            break;
                        }
                    }

                    //控制波形左右偏移的速度，越大速度越快
                    mAngle += 12;
                    mAngle = mAngle % 360;

                    WaveShakeView.this.postInvalidate();

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mRun = false;
                A = 0;
            }

        }).start();
    }

    public void startAnimation(){
        mAinmationStep = ANIMATION_START;
        A = 0;

        if (!mRun) {
            startAnimationThread();
        }
    }

    public void stopAnimation(){
        mAinmationStep = ANIMATION_END;
    }
}
