package com.example.videodemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class CountDownActivity extends Activity {

    private final String TAG = "CountDownActivity";

    private Button mButton;
    private CountDownLine mCountDownLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        mButton = (Button) findViewById(R.id.startBtn);
        mCountDownLine = (CountDownLine) findViewById(R.id.countDownLine);

        mButton.setOnTouchListener(mOnTouchListener);
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {

                Log.d(TAG, "onTouch ACTION_DOWN");

                mCountDownLine.startCountDown();

                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {

//                Log.d(TAG, "onTouch ACTION_MOVE");
                return true;
            } else if (action == MotionEvent.ACTION_UP) {

                mCountDownLine.stopCountDown();

                Log.d(TAG, "onTouch ACTION_UP");
                return false;
            }

            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_count_down, menu);
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
