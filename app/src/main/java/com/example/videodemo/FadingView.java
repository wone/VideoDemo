package com.example.videodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class FadingView extends View {

	private static final String TAG = "FadingView";
	
	private static final int DEFAULT_ANIMATION_TIME = 250; 
	
	private long mDuration = DEFAULT_ANIMATION_TIME;
	
	private long mAnimationStartTime;
	
	private boolean mFadingStart = false;
	
	private Interpolator mInterpolator = new AccelerateInterpolator();
	
	public FadingView(Context context) {
		super(context);
	}
	
	public FadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mFadingStart) {
			if (mAnimationStartTime == 0) {
				mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
			}
			
			long now = AnimationUtils.currentAnimationTimeMillis();
			long elapseTime = now - mAnimationStartTime;
			
			if (elapseTime > mDuration) {
				mFadingStart = false;
				mAnimationStartTime = 0;
				canvas.drawARGB(255, 0, 0, 0);
			} else {
				float progress = (float)elapseTime/(float)mDuration;
				
				progress = mInterpolator.getInterpolation(progress);
				
				progress  = progress >= 1 ? 1 : progress; 
				
				int alpha = (int) (255 * progress);
				
				Log.d(TAG, "on Draw alpha:"+alpha);
						
				canvas.drawARGB(alpha, 0, 0, 0);
				
				invalidate();
			}
		} else {
			canvas.drawARGB(255, 0, 0, 0);
		}
		
	}
	
	public void setInterpolator(Interpolator i){
		if (i == null) {
			return;
		}
		mInterpolator = i;
	}

	public void setDuration(long duration){
		mDuration = duration;
	}
	
	public void startFading(){
		mFadingStart = true;
		invalidate();
	}

}
