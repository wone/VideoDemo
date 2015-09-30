package com.example.videodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ivenzhang on 15/9/27.
 */
public class WaveShakeView extends View{

    private static final String TAG = "WaveShakeView";

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
        mPaint2.setColor(Color.GRAY);
        mPaint2.setAntiAlias(true);
        mPaint2.setStrokeWidth(1);
        mPaint2.setStyle(Paint.Style.STROKE);

        mPath = new Path();

        setBackgroundColor(Color.BLACK);
    }



    float A = 0;
    int B = 400;
    float C = 0.6f;//角速度越小，波形越稀疏
    float D = 25; //相邻曲线相位偏差
    float X = 1.0f;

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

        long start = System.currentTimeMillis();

//        A = 40;

        drawSin3(canvas, mPath);

        long cost = System.currentTimeMillis() - start;
        Log.d(TAG, "onDraw(): cost =" + cost + "ms");
    }

    private void drawSin3(Canvas canvas, Path path){

        int x = 0;
        int y = 0;

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        //第一条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
//            path.quadTo(x + 1, y+1, x + 1, y);

            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint1);

        //第二条曲线
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle * X + D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
//            path.quadTo(x, y, x + 1, y);
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);


        //
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle * X + 2 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
//            path.quadTo(x, y, x + 1, y);
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);

        //
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle * X + 3 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
//            path.quadTo(x, y, x + 1, y);
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);

        //
        path.reset();
        for (int i = 0; i < mWidth; i++) {
            x = i;
            y = (int) (A * Math.sin((i * C + mAngle * X + 4 * D) * Math.PI / 180) + B);
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                path.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
//            path.quadTo(x, y, x + 1, y);
            path.lineTo(x + 1, y);
        }
        canvas.drawPath(path, mPaint2);
    }

    private void drawSin2(Canvas canvas, Paint paint){

        float length = 800;
        float height = 200;

        float startX, startY;
        float controlX1, controlY1;
        float endX1, endY1;

        float controlX2, controlY2;
        float endX2, endY2;

        Path path = new Path();

        //a->b
        startX = 0;
        startY = height;
        path.moveTo(startX, startY);
        endX1 = startX +length/2;
        endY1 = startY;
        controlX1 = (startX + endX1) / 2;
        controlY1 = 0;
        path.quadTo(controlX1, controlY1, endX1, endY1);

        //b->c
        endX2 = startX +length;
        endY2 = startY;
        controlX2 = (endX1 + endX2) / 2;
        controlY2 = height * 2;
        path.quadTo(controlX2, controlY2, endX2, endY2);

        canvas.drawPath(path, paint);
        //////////////////

        path.reset();

        //a->b
        startX = 30;
        startY = height;
        path.moveTo(startX, startY);
        endX1 = startX +length/2;
        endY1 = startY;
        controlX1 = (startX + endX1) / 2;
        controlY1 = 0;
        path.quadTo(controlX1, controlY1, endX1, endY1);

        //b->c
        endX2 = startX +length;
        endY2 = startY;
        controlX2 = (endX1 + endX2) / 2;
        controlY2 = height * 2;
        path.quadTo(controlX2, controlY2, endX2, endY2);

        canvas.drawPath(path, paint);

        //////////////////

        path.reset();

        //a->b
        startX = 60;
        startY = height;
        path.moveTo(startX, startY);
        endX1 = startX +length/2;
        endY1 = startY;
        controlX1 = (startX + endX1) / 2;
        controlY1 = 0;
        path.quadTo(controlX1, controlY1, endX1, endY1);

        //b->c
        endX2 = startX +length;
        endY2 = startY;
        controlX2 = (endX1 + endX2) / 2;
        controlY2 = height * 2;
        path.quadTo(controlX2, controlY2, endX2, endY2);

        canvas.drawPath(path, paint);


        //////////////////

        path.reset();

        //a->b
        startX = 90;
        startY = height;
        path.moveTo(startX, startY);
        endX1 = startX +length/2;
        endY1 = startY;
        controlX1 = (startX + endX1) / 2;
        controlY1 = 0;
        path.quadTo(controlX1, controlY1, endX1, endY1);

        //b->c
        endX2 = startX +length;
        endY2 = startY;
        controlX2 = (endX1 + endX2) / 2;
        controlY2 = height * 2;
        path.quadTo(controlX2, controlY2, endX2, endY2);

        canvas.drawPath(path, paint);

    }

    private  void drawSin(Canvas canvas){

        long start = System.currentTimeMillis();

        int x, y1, y2 = 0;

        double lineX = 0;
        double lineY1 = 0;
        double lineY2 = 0;


        for (int i = 0; i < mWidth; ) {

            lineX = i;
            lineY1 = A * Math.sin((i * D + mAngle) * Math.PI / 180) + B;
            lineY2 = A * Math.sin((i * D + mAngle + C) * Math.PI / 180) + B;

            x = (int) lineX;
            y1 = (int) (lineY1 + mHeight / 2);
            y2 = (int) (lineY2 + mHeight / 2);

            Log.d(TAG, "onDraw(), lineX = " + lineX + ", lineY1 = " + lineY1 + ", lineY2 = " + lineY2
                    + ",  y1 = "+ y1 +", y2 = "+ y2);

//            canvas.drawLine(x, y1, x, y2, mPaint);

            canvas.drawPoint(x, y1, mPaint1);

            i += 2;
        }

        long cost = System.currentTimeMillis() - start;

        Log.d(TAG, "onDraw(): cost =" + cost);

//        startAnimation();
    }

    volatile private int mAngle = 0;

    volatile private boolean mRun;

    public boolean isAnimationRuning(){
        return mRun;
    }

    public void runBeginAnimation(){
        A = 0;
        mRun = true;

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (mRun) {
                    //控制波形左右偏移的速度，越大速度越快
                    mAngle += 12;
                    mAngle = mAngle % 360;

                    //控制振幅
                    if (A < 32) {
                        A += 1;
                    }

                    WaveShakeView.this.postInvalidate();

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }).start();
    }

    public void runEndAnimation(){

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (A > 0) {
                    A -= 1;

                    WaveShakeView.this.postInvalidate();

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mRun = false;
            }

        }).start();
    }
}
