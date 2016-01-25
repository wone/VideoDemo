package com.example.videodemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WaveViewActivity extends Activity {

    private WaveShakeView mWaveShakeView;

    private BreathView mBreathView;

    private AudioAnimationView mAudioAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_view);

        mWaveShakeView = (WaveShakeView) this.findViewById(R.id.waveShakeView);
        mBreathView = (BreathView) this.findViewById(R.id.breathView);
        mAudioAnimView = (AudioAnimationView) this.findViewById(R.id.audioAnimView);

        TextView button = (TextView) this.findViewById(R.id.runBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBreathView.isNormalSize()) {
                    mBreathView.startGrowAnimation();
                } else {
                    mBreathView.startShrinkAnimation();
                }

                if (!mAudioAnimView.isAnimationRunning()) {
                    mAudioAnimView.startAnimation();
                } else {
                    mAudioAnimView.stopAnimation();
                }
            }
        });

//        mAudioAnimView.startAnimation();

        RelativeLayout root = (RelativeLayout) this.findViewById(R.id.root);

        LinearLayout ll = (LinearLayout) this.findViewById(R.id.ll);


        for (int i = 0; i<=5; i++) {
            LinearLayout.LayoutParams tempLps = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            tempLps.weight = 1;
            tempLps.gravity= Gravity.CENTER_VERTICAL;

            RedDotImageView iv = new RedDotImageView(this);
            iv.setImageResource(R.drawable.skin_aio_panel_ptv_nor);
            iv.showRedDot(true);

            ll.addView(iv,tempLps);
        }

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

        if (mBreathView.isAnimationRuning()) {
            mBreathView.stopAnimation();
        }

        if (mWaveShakeView.isAnimationRuning()) {
            mWaveShakeView.stopAnimation();
        }

        if (mAudioAnimView.isAnimationRunning()) {
            mAudioAnimView.stopAnimation();
        }
    }
}
