package com.wave.sbauction;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/*
Here, we have the entire game, which will prove to the humans that their multitasking ability is nonexistent.
 */
public class MultiTaskGame extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    static float xAcceleration = 0;
    static float yAcceleration = 0;
    static float zAcceleration = 0;

    boolean lostGame = false;
    static double timeElapsed = 0;

    TextView tvTimer;
    ProgressBar pbTilt;
    ImageView ivLeftRight;
    ImageView ivUpDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_game);

        //regionDeclare UI elements
        tvTimer = findViewById(R.id.tvTimer);
        pbTilt = findViewById(R.id.pbTilt);
        ivLeftRight = findViewById(R.id.ivLeftRight);
        ivUpDown = findViewById(R.id.ivUpDown);
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
                }
            }
        }.start();
        //endregion

        //regionFirst game, tilt the phone to the specified tilts before time runs out -P
        //Setup accelerometer so game can be controlled
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread() {
            @Override
            public void run() {
                int xRequirement = RandRange(-5,5);
                int yRequirement = RandRange(-5,5);
                int timeGiven = RandRange(8,14);
                long timeStart = System.currentTimeMillis();
                pbTilt.setProgress(100);
                //They will get changed to names of images, and only changed when there's a change, to
                //reduce the system load                                            //possibilities below
                String leftImage = "something different so it fills";                //check, left, right
                String rightImage = "this with something, this is just temporary";   //check, up, down
                //Determines if the player gets a break from this activitiy this loop
                boolean getBreak = false;
                while(!lostGame){
                    //If the player has completed the challenge, give them a moment
                    if (getBreak) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //On the chance the user was given a break, revoke it once it happens.
                    getBreak = false;

                    //Figure out how the phone is currently positioned
                    int userX = (int)Round(yAcceleration,0);
                    int userY = (int)Round(xAcceleration,0);

                    //Based on where the phone is, determine if the arrows need changing
                    final String leftImageCurrent;
                    if (userX > xRequirement) {
                        leftImageCurrent = "left";
                    } else if (userX < xRequirement) {
                        leftImageCurrent = "right";
                    } else {
                        leftImageCurrent = "check";
                    }
                    final String rightImageCurrent;
                    if (userY > yRequirement) {
                        rightImageCurrent = "up";
                    } else if (userY < yRequirement) {
                        rightImageCurrent = "down";
                    } else {
                        rightImageCurrent = "check";
                    }

                    //Update imageviews only if a difference is found since last time
                    if (!leftImage.equals(leftImageCurrent)) {
                        //Have to do this, because can't access views from inside this thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if("left".equals(leftImageCurrent)) {
                                    ivLeftRight.setImageResource(R.drawable.left_arrow);
                                } else if ("right".equals(leftImageCurrent)) {
                                    ivLeftRight.setImageResource(R.drawable.right_arrow);
                                } else {
                                    ivLeftRight.setImageResource(R.drawable.checkmark);
                                }
                            }
                        });

                        //After updating, change so that the next one will update
                        leftImage = leftImageCurrent;
                    }
                    if (!rightImage.equals(rightImageCurrent)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if("up".equals(rightImageCurrent)) {
                                    ivUpDown.setImageResource(R.drawable.up_arrow);
                                } else if ("down".equals(rightImageCurrent)) {
                                    ivUpDown.setImageResource(R.drawable.down_arrow);
                                } else {
                                    ivUpDown.setImageResource(R.drawable.checkmark);
                                }
                            }
                        });
                        rightImage = rightImageCurrent;
                    }

                    //Check to see if the user has solved the puzzle, if so, reset the timer, and give
                    //the player a quick break from this challenge. The break is mostly so they see two check marks, and know they did it right
                    if (rightImageCurrent.equals("check") && leftImageCurrent.equals("check")){
                        timeGiven = RandRange(8,14);
                        timeStart = System.currentTimeMillis();
                        xRequirement = RandRange(-5,5);
                        yRequirement = RandRange(-5,5);
                        getBreak = true;
                        pbTilt.setProgress(100);
                    }

                    //Decrement the timer, and show the user that has happened, through the progress bar
                    double timePassed = (double)(System.currentTimeMillis()-timeStart)/1000;
                    double timeRemaining = (double)timeGiven - timePassed;
                    int newProgress = (int)((timeRemaining/(double)timeGiven)*100);
                    pbTilt.setProgress(newProgress);
                    //If the user is out of time, lose the game
                    if (timeRemaining < 0) {
                        lostGame = true;
                    }
                    //Give the system a chance to catch up
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //Method to generate random numbers in a range -P
    public int RandRange(int min,int max){
        return new Random().nextInt((max - min) + 1) + min;
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

    //Can't update views without using asynctasks (turns out I'm wrong)
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