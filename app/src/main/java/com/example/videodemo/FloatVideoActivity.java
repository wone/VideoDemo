package com.example.videodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class FloatVideoActivity extends Activity {

    private static final String TAG = "VideoDemo.FloatActivity";

    boolean mRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_video);

        final Button operateBtn = (Button) findViewById(R.id.operateBtn);
        operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (!mRunning) {
                   Intent intent = new Intent(FloatVideoActivity.this, FloatVideoService.class);
                   startService(intent);

                   mRunning = true;
                   operateBtn.setText("stop");
               } else {
                   Intent intent = new Intent(FloatVideoActivity.this, FloatVideoService.class);
                   stopService(intent);

                   mRunning = false;
                   operateBtn.setText("start");
               }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_float_video, menu);
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

}
