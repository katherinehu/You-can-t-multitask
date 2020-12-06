package com.wave.sbauction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import android.graphics.Color;

import androidx.annotation.RequiresApi;

/*
Here, we have the entire game, which will prove to the humans that their multitasking ability is nonexistent.
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MultiTaskGame extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    static float xAcceleration = 0;
    static float yAcceleration = 0;
    static float zAcceleration = 0;

    boolean lostGame = false;
    static double timeElapsed = 0;

    Button btnRestart;

    TextView tvTimer;
    ProgressBar pbTilt;
    ImageView ivLeftRight;
    ImageView ivUpDown;

    TextView tv_randWord, tv_colorConfused;
    Button btn_green;
    Button btn_blue;
    Button btn_red;
    Button btn_orange;
    Button btn_purple;
    Button btn_yellow;
    ProgressBar pbColor;

    boolean clicked_green = false;
    boolean clicked_blue = false;
    boolean clicked_red = false;
    boolean clicked_orange = false;
    boolean clicked_purple = false;
    boolean clicked_yellow = false;

    //Color game, make this public, so that multiple threads can access it
    static int timeGivenColor = 6;
    static boolean colorCompleted = false;
    static String textOrColor = "color";
    int randTextColor;
    int randTextValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_game);

        //regionDeclare UI elements
        tvTimer = findViewById(R.id.tvTimer);

        btnRestart = findViewById(R.id.btnRestart);

        pbTilt = findViewById(R.id.pbTilt);
        ivLeftRight = findViewById(R.id.ivLeftRight);
        ivUpDown = findViewById(R.id.ivUpDown);

        tv_randWord = findViewById(R.id.tv_randWord);
        tv_colorConfused = findViewById(R.id.tv_colorConfused);
        btn_blue = (Button)findViewById(R.id.btn_blue);
        btn_green = (Button)findViewById(R.id.btn_green);
        btn_red = (Button)findViewById(R.id.btn_red);
        btn_orange = (Button)findViewById(R.id.btn_orange);
        btn_purple = (Button)findViewById(R.id.btn_purple);
        btn_yellow = (Button)findViewById(R.id.btn_yellow);
        pbColor = findViewById(R.id.pbColor);


        //endregion

        //regionDetermine which games to play
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = data.edit();
        final boolean tiltDisabled = data.getBoolean("tiltDisabled",false);
        final boolean mathDisabled = data.getBoolean("mathDisabled",false);
        final boolean colorDisabled = data.getBoolean("colorDisabled",false);
        final boolean timerDisabled = data.getBoolean("timerDisabled",false);
        final boolean barsDisabled = data.getBoolean("barsDisabled",false);
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

        //regionShow a restart button if the user loses the game
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MultiTaskGame.class));
            }
        });
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(!lostGame){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnRestart.setVisibility(View.VISIBLE);
                    }
                });
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
                int yRequirement = RandRange(0,5);
                int timeGiven = RandRange(8,14);
                long timeStart = System.currentTimeMillis();
                pbTilt.setProgress(100);
                //They will get changed to names of images, and only changed when there's a change, to
                //reduce the system load                                            //possibilities below
                String leftImage = "something different so it fills";                //check, left, right
                String rightImage = "this with something, this is just temporary";   //check, up, down
                //Determines if the player gets a break from this activity this loop
                boolean getBreak = false;
                while(!lostGame && !tiltDisabled){
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
                        yRequirement = RandRange(0,5);
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
                        pbTilt.setProgress(0);
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


        //region Color confusion -k
        new Thread() {
            @Override
            public void run() {
                final long[] timeStart = {System.currentTimeMillis()};

                //regionRunnable which restarts the game
                final Runnable resetColor = new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int max = 6;
                                int min = 1;

                                randTextColor = (int)(Math.random() * (max - min + 1) + min);
                                randTextValue = (int)(Math.random() * (max - min + 1) + min);

                                switch(randTextColor) {
                                    case 1:
                                        tv_randWord.setTextColor(Color.RED);
                                        tv_randWord.setContentDescription("1");
                                        break;
                                    case 2:
                                        tv_randWord.setTextColor(Color.parseColor("#FFA500"));
                                        tv_randWord.setContentDescription("2");
                                        break;
                                    case 3:
                                        tv_randWord.setTextColor(Color.YELLOW);
                                        tv_randWord.setContentDescription("3");
                                        break;
                                    case 4:
                                        tv_randWord.setTextColor(Color.GREEN);
                                        tv_randWord.setContentDescription("4");
                                        break;
                                    case 5:
                                        tv_randWord.setTextColor(Color.parseColor("#03A9F4"));
                                        tv_randWord.setContentDescription("5");
                                        break;
                                    case 6:
                                        tv_randWord.setTextColor(Color.parseColor("#800080"));
                                        tv_randWord.setContentDescription("6");
                                        break;
                                    default:
                                        tv_randWord.setTextColor(Color.WHITE);
                                }

                                switch(randTextValue) {
                                    case 1:
                                        tv_randWord.setText("RED");
                                        tv_randWord.setContentDescription("1");
                                        break;
                                    case 2:
                                        tv_randWord.setText("ORANGE");
                                        tv_randWord.setContentDescription("2");
                                        break;
                                    case 3:
                                        tv_randWord.setText("YELLOW");
                                        tv_randWord.setContentDescription("3");
                                        break;
                                    case 4:
                                        tv_randWord.setText("GREEN");
                                        tv_randWord.setContentDescription("4");
                                        break;
                                    case 5:
                                        tv_randWord.setText("BLUE");
                                        tv_randWord.setContentDescription("5");
                                        break;
                                    case 6:
                                        tv_randWord.setText("PURPLE");
                                        tv_randWord.setContentDescription("6");
                                        break;
                                    default:
                                        tv_randWord.setTextColor(Color.WHITE);
                                }
                            }
                        });

                        //Choose if the player will have to pick based on text, or color next time
                        boolean chooseText = new Random().nextBoolean();
                        if (chooseText) {
                            textOrColor = "text";
                            //Update view, so user knows which one to click
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_colorConfused.setText("TEXT");
                                }
                            });
                        } else {
                            textOrColor = "color";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_colorConfused.setText("COLOR");
                                }
                            });
                        }

                        //Give the user a break if they managed to complete the challenge
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //How much time is normally given to do this task
                        timeGivenColor = RandRange(5,10);
                        timeStart[0] = System.currentTimeMillis();

                        colorCompleted = false;
                    }
                };
                //endregion

                resetColor.run();
                while (!lostGame && !colorDisabled ) {
                    //Check if user has won this time around
                    if (colorCompleted) {
                        resetColor.run();
                    } else {
                        //Decrement the timer, and show the user that has happened, through the progress bar
                        double timePassed = (double) (System.currentTimeMillis() - timeStart[0]) / 1000;
                        double timeRemaining = (double) timeGivenColor - timePassed;
                        int newProgress = (int) ((timeRemaining / (double) timeGivenColor) * 100);
                        pbColor.setProgress(newProgress);

                        //If the user is out of time, lose the game
                        if (timeRemaining < 0) {
                            lostGame = true;
                            pbColor.setProgress(0);
                        }

                        //Take a quick break, so the thread isn't as resource intensive
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

        //onclick listener for color buttons
        //check to see if buttons have been clicked
        btn_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_red = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_red.getContentDescription())){
                    btn_red.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_red.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    if(!colorCompleted) {
                        btn_red.setText("×");
                        lostGame = true;
                    }
                }

            }
        });
        btn_orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_orange = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_orange.getContentDescription())){
                    btn_orange.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_orange.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    btn_orange.setText("×");
                    if(!colorCompleted) {
                        lostGame = true;
                    }
                }
            }
        });
        btn_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_yellow = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_yellow.getContentDescription())){
                    btn_yellow.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_yellow.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    if(!colorCompleted) {
                        btn_yellow.setText("×");
                        lostGame = true;
                    }
                }
            }
        });
        btn_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_green = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_green.getContentDescription())){
                    btn_green.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_green.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    if(!colorCompleted) {
                        btn_green.setText("×");
                        lostGame = true;
                    }
                }
            }
        });
        btn_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_blue = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_blue.getContentDescription())){
                    btn_blue.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_blue.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    if(!colorCompleted) {
                        btn_blue.setText("×");
                        lostGame = true;
                    }
                }
            }
        });
        btn_purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllToFalse();
                clicked_purple = true;
                String correctAnswer;
                if(textOrColor.equals("color")) {
                    correctAnswer = Integer.toString(randTextColor);
                } else {
                    //When it equals "text"
                    correctAnswer = Integer.toString(randTextValue);
                }
                if (correctAnswer.contentEquals(btn_purple.getContentDescription())){
                    btn_purple.setText("✓");
                    pbColor.setProgress(100);
                    colorCompleted = true;
                    //Take away the check mark after a few seconds
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_purple.setText("");
                                }
                            });
                        }
                    }.start();
                }
                else{
                    if(!colorCompleted) {
                        btn_purple.setText("×");
                        lostGame = true;
                    }
                }
            }
        });
        //endregion

    }


    //set all color boolean vals to be false
    public void setAllToFalse() {
        boolean clicked_green = false;
        boolean clicked_blue = false;
        boolean clicked_red = false;
        boolean clicked_orange = false;
        boolean clicked_purple = false;
        boolean clicked_yellow = false;

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