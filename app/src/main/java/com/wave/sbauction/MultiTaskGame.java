package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
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

    TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_game);

        //regionDeclare UI elements

        //regionSet up a timer so the user knows how long they've survived. -P
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Use this to calibrate, since thread.sleep isn't always perfect
                long firstTime = System.currentTimeMillis();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
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
}