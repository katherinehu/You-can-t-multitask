package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
Here, we have the entire game, which will prove to the humans that their multitasking ability is nonexistent.
 */
public class MultiTaskGame extends Activity implements SensorEventListener {

    static float xAcceleration = 0;
    static float yAcceleration = 0;
    static float zAcceleration = 0;

    boolean lostGame = false;
    static double timeElapsed = 0;

    TextView tvTimer;
    ImageView ivBallGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_game);

        //regionDeclare UI elements
        tvTimer = findViewById(R.id.tvTimer);
        ivBallGame = findViewById(R.id.ivBallGame);
        //endregion

        //regionSet up a timer so the user knows how long they've survived. -P
        new Thread(){
            @Override
            public void run() {
                //Use this to calibrate, since thread.sleep isn't always perfect
                long firstTime = System.currentTimeMillis();
                while(!lostGame) {
                    long currentTime = System.currentTimeMillis();
                    long timeDiff = currentTime - firstTime;
                    timeElapsed = (double)timeDiff / 1000;
                    String time = "Time Survived: " + Round(timeElapsed,1);
                    new updateTimer().execute(time);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("timeElapsed","Time: " + timeElapsed);
                }
            }
        }.start();
        //endregion

        //regionFirst game, keep the ball inside the box -P
        new Thread() {
            @Override
            public void run() {
                Bitmap firstMap = BitmapFactory.decodeResource(getResources(),R.drawable.canvas);
                Bitmap editableMap = firstMap.copy(Bitmap.Config.ARGB_8888,true);
                int h = editableMap.getHeight();
                int w = editableMap.getWidth();
                while(!lostGame){

                }
            }
        }.start();
        //endregion
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xAcceleration = event.values[0];
        yAcceleration = event.values[1];
        zAcceleration = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Method to round numbers to specified level of precision -P
    public float Round(float input,int scale) {
        if (scale > 30) {
            scale = 30;
        }
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    //include one for doubles as well
    public double Round(double input,int scale) {
        if (scale > 30) {
            scale = 30;
        }
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Can't update views
    class updateTimer extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String time) {
            super.onPostExecute(time);
            tvTimer.setText(time);
        }
    }
}