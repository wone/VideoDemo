package com.example.videodemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class WaveViewActivity extends Activity {

    private WaveShakeView mWaveShakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_view);

        mWaveShakeView = (WaveShakeView) this.findViewById(R.id.waveShakeView);
        this.findViewById(R.id.runBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWaveShakeView.isAnimationRuning()) {
                    mWaveShakeView.stopAnimation();
                } else {
                    mWaveShakeView.startAnimation();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wave_view, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWaveShakeView.isAnimationRuning()) {
            mWaveShakeView.stopAnimation();
        }
    }
}
