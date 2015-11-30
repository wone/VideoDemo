package com.example.videodemo;

/**
 * Created by ivenzhang on 15/11/27.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;



/**
 * @author jazzyfu
 *
 */
public class RedDotImageView extends ImageView {

    private int mRedDotXOffsetDp = 13+2;
    private int mRedDotYOffsetDp = 13;

    private Drawable redDotDrawable = null;
    private boolean isShowRedDot = false;

    private float mDensity;



    public RedDotImageView(Context context) {
        this(context, null);
    }

    public RedDotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDensity = getResources().getDisplayMetrics().density;

    }

    public RedDotImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDensity = getResources().getDisplayMetrics().density;
    }

    public void showRedDot(boolean isShow) {
        if (isShowRedDot != isShow) {
            isShowRedDot = isShow;
            if (isShowRedDot && redDotDrawable == null) {
                redDotDrawable = getResources().getDrawable(R.drawable.skin_tips_dot);
            }
            this.postInvalidate();
        }
    }

    public void setRedDotDrawable(int resId){

    }

    public boolean isShowingRedDot() {
        return isShowRedDot;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShowRedDot && redDotDrawable != null) {
            redDotDrawable.setState(getDrawableState());

            int x = (int) Math.ceil((0.5 * getWidth() + mDensity * mRedDotXOffsetDp - 0.5 * redDotDrawable.getIntrinsicWidth()));
            int y = (int) Math.ceil((0.5 * getHeight() - mDensity * mRedDotYOffsetDp - 0.5 * redDotDrawable.getIntrinsicHeight()));

            redDotDrawable.setBounds(x, y, x + redDotDrawable.getIntrinsicWidth(), y + redDotDrawable.getIntrinsicHeight());
            redDotDrawable.draw(canvas);
        }
    }


}
