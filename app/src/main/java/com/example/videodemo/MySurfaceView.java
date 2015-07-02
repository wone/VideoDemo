package com.example.videodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView {

	public MySurfaceView(Context context) {
		super(context, null);
	}
	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Canvas c = getHolder().lockCanvas();
		c.drawColor(0x000000);
		getHolder().unlockCanvasAndPost(c);
		
		super.draw(canvas);
	}
	
	

}
