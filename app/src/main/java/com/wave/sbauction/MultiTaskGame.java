package com.wave.sbauction;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
                }
            }
        }.start();
        //endregion

        //regionFirst game, keep the ball inside the box -P
        //Setup accelerometer so game can be controlled
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                //Get an image of what the playing field looks like
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap firstMap = BitmapFactory.decodeResource(getResources(),R.drawable.canvas,options);
                int h = firstMap.getHeight();
                int w = firstMap.getWidth();
                int [] canvasPixels = new int[h*w];
                firstMap.getPixels(canvasPixels,0,w,0,0,w,h);
                //The color of the background, for reference, stored here
                int baseColor = canvasPixels[1];

                //Velocity of the ball is stored here, which is used to adjust the position
                //of the ball until it is no longer on the screen, at which point the player fails
                //this particular challenge. Starts off at a random value to keep things interesting.
                double xVelocity = 0;//ThreadLocalRandom.current().nextDouble(-10,10);
                double yVelocity = 0;//ThreadLocalRandom.current().nextDouble(-10,10);
                //How much position had changed since the start of the game
                double xPosition = 0;
                double yPosition = 0;
                //Set how fast this game will refresh, also use that time for physics calculations
                int refreshMilliseconds = 500;
                //Make all the movements less dramatic so the user actually has time to react, this is adjusted until the game runs reasonably
                double scalingFactor = 1;

                while(!lostGame){
                    //Locate where the ball is in the canvas
                    ArrayList<Integer> ballLocations = new ArrayList<>();
                    for (int i = 0; i < h * w; ++i) {
                        if (canvasPixels[i] == Color.RED) {
                            ballLocations.add(i);
                            Log.d("ballLocation",Integer.toString(i));
                            //Set to the base color, so that that the position of the ball can be updated.
                            canvasPixels[i] = baseColor;
                        }
                    }
                    //Calculate new ball velocity
//                    xVelocity += yAcceleration*yAcceleration*((double)refreshMilliseconds/1000) / scalingFactor * 0.1;
//                    yVelocity -= xAcceleration*xAcceleration*((double)refreshMilliseconds/1000) / scalingFactor * 0.1;
                    //Calculate new ball position
                    xPosition += yAcceleration*((double)refreshMilliseconds/1000) / scalingFactor;
                    yPosition += xAcceleration*((double)refreshMilliseconds/1000) / scalingFactor;

                    //Store where the ball will be now.
                    ArrayList<Integer> newBallLocations = new ArrayList<>();
                    for(int locations:ballLocations){
                        //Check to see if it went out the left or right bound.
                        int currentRow = locations % w;
                        if ((currentRow + (int)xPosition) > w || (currentRow + (int)xPosition) < 0){
                            lostGame = true;
                            break;
                        }
                        newBallLocations.add(locations + (int)xPosition + w * (int)yPosition);
                    }

                    //Try to update the image, if it doesn't work, that means player has failed game
                    for(int locations:newBallLocations) {
                        try {
                            canvasPixels[locations] = Color.RED;
                        } catch (Exception e) {
                            //This means out of bounds, and the player has failed the game
                            lostGame = true;
                        }
                    }

                    //If the above worked, update the image
                    if (!lostGame) {
                        ivBallGame.setImageBitmap(Bitmap.createBitmap(canvasPixels, w, h, Bitmap.Config.ARGB_8888));
                    }

                    //Delay a bit so the player has time to react.
                    try {
                        Thread.sleep(refreshMilliseconds);
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