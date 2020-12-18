package com.wave.sbauction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

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

    CardView cardGameOver;
    Button btnRestart, btnQuit;
    TextView txtGameInfo;

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

    Button btnTime1, btnTime2, btnTime3;
    ProgressBar pbTime;

    Button btnPress;
    ProgressBar pbPress;

    SeekBar sb1, sb2;
    ProgressBar pbBar1, pbBar2;

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

    //Timer game
    static int button1Number = 1;
    static int button2Number = 4;
    static int button3Number = 9;
    static boolean completedTimer = false;

    //Pressing game
    static double timeLeft = 30;
    static double buttonRate = 0;

    //Balancing game
    static int barRate1;
    static int barRate2;
    static int bar1 = 500;
    static int bar2 = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_game);

        //regionDeclare UI elements
        tvTimer = findViewById(R.id.tvTimer);

        btnRestart = findViewById(R.id.btnRestart);
        btnQuit = findViewById(R.id.btnQuit);
        cardGameOver = findViewById(R.id.cardGameOver);
        txtGameInfo = findViewById(R.id.txtGameInfo);

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

        btnTime1 = findViewById(R.id.btnTime1);
        btnTime2 = findViewById(R.id.btnTime2);
        btnTime3 = findViewById(R.id.btnTime3);
        pbTime = findViewById(R.id.pbTimer);

        btnPress = findViewById(R.id.btnPress);
        pbPress = findViewById(R.id.pbPress);

        sb1 = findViewById(R.id.sb1);
        sb2 = findViewById(R.id.sb2);
        pbBar1 = findViewById(R.id.pbBar);
        pbBar2 = findViewById(R.id.pbBar2);
        //endregion

        //regionDetermine which games to play
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = data.edit();
        final boolean tiltDisabled = data.getBoolean("tiltDisabled",false);
        final boolean pressDisabled = data.getBoolean("pressDisabled",false);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String time = "Time Survived: " + Round(timeElapsed,1);
                            tvTimer.setText(time);
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion

        //regionWhat happens once the game is over
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MultiTaskGame.class));
            }
        });
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MultiTaskMainMenu.class));
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
                timeLeft = 30;
                bar1 = 500;
                bar2 = 500;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardGameOver.setVisibility(View.VISIBLE);
                    }
                });
                //Calculate score, store it if it is a high score
                SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                final SharedPreferences.Editor editor = data.edit();
                int gamesDisabled = data.getInt("gamesDisabled",0);
                long timeSurvived = (long)timeElapsed;
                long score = 1;
                for(int i = 0; i < (5-gamesDisabled); ++i){
                    score = score * timeSurvived;
                }
                long highScore = data.getLong("highScore",0);
                if (score > highScore) {
                    editor.putLong("highScore",score);
                    editor.commit();
                }
                final String display = "Score: " + addCommas(score) + "\nHigh Score: " + addCommas(highScore);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtGameInfo.setText(display);
                    }
                });
                timeElapsed = 0;
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
                int timeGiven = RandRange(8,14,"max");
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
                        timeGiven = RandRange(8,14,"max");
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
                        timeGivenColor = RandRange(5,10,"max");
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

        //regionDigit determination game, press buttons while that number exists in the timer -P
        new Thread(){
            @Override
            public void run() {
                super.run();
                long timeStart = System.currentTimeMillis();
                int timeGiven = RandRange(7,20,"min");

                //Make sure that the other 2 buttons are different
                boolean success2 = false;
                while(!success2) {
                    if(button2Number == button1Number) {
                        button2Number = RandRange(0,9);
                    } else {
                        success2 = true;
                    }
                }
                boolean success3 = false;
                while(!success3) {
                    if(button2Number == button3Number || button1Number == button3Number) {
                        button3Number = RandRange(0,9);
                    } else {
                        success3 = true;
                    }
                }

                //Label the buttons so the user knows which ones to press
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String btnText1 = "   " + button1Number + "   ";
                        String btnText2 = "   " + button2Number + "   ";
                        String btnText3 = "   " + button3Number + "   ";
                        btnTime1.setText(btnText1);
                        btnTime2.setText(btnText2);
                        btnTime3.setText(btnText3);
                    }
                });
                pbTime.setProgress(100);

                while(!lostGame && !timerDisabled) {
                    //If the player does the task successfully
                    if(completedTimer) {
                        button1Number = RandRange(0,9);
                        button2Number = RandRange(0,9);
                        button3Number = RandRange(0,9);
                        //Make sure that the other 2 buttons are different
                        success2 = false;
                        while(!success2) {
                            if(button2Number == button1Number) {
                                button2Number = RandRange(0,9);
                            } else {
                                success2 = true;
                            }
                        }
                        success3 = false;
                        while(!success3) {
                            if(button2Number == button3Number || button1Number == button3Number) {
                                button3Number = RandRange(0,9);
                            } else {
                                success3 = true;
                            }
                        }

                        pbTime.setProgress(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String btnText1 = "   " + button1Number + "   ";
                                String btnText2 = "   " + button2Number + "   ";
                                String btnText3 = "   " + button3Number + "   ";
                                btnTime1.setText(btnText1);
                                btnTime2.setText(btnText2);
                                btnTime3.setText(btnText3);
                                btnTime1.setEnabled(false);
                                btnTime2.setEnabled(false);
                                btnTime3.setEnabled(false);
                            }
                        });
                        //Disable the buttons for a moment so users don't double tap and lose
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTime1.setEnabled(true);
                                btnTime2.setEnabled(true);
                                btnTime3.setEnabled(true);
                            }
                        });
                        timeStart = System.currentTimeMillis();
                        timeGiven = RandRange(7,20,"min");
                        completedTimer = false;
                    } else {
                        //Decrement the timer if nothing happened
                        double timePassed = (double)(System.currentTimeMillis()-timeStart)/1000;
                        double timeRemaining = (double)timeGiven - timePassed;
                        int newProgress = (int)((timeRemaining/(double)timeGiven)*100);
                        pbTime.setProgress(newProgress);
                        if (timeRemaining < 0) {
                            lostGame = true;
                        }
                        //Take a break so the thread doesn't use too much processing power
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

        btnTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTime1.setEnabled(false);
                String currentTime = tvTimer.getText().toString();
                if(currentTime.contains(Integer.toString(button1Number))) {
                    completedTimer = true;
                } else {
                    lostGame = true;
                }
            }
        });

        btnTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTime2.setEnabled(false);
                String currentTime = tvTimer.getText().toString();
                if(currentTime.contains(Integer.toString(button2Number))) {
                    completedTimer = true;
                } else {
                    lostGame = true;
                }
            }
        });

        btnTime3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTime3.setEnabled(false);
                String currentTime = tvTimer.getText().toString();
                if(currentTime.contains(Integer.toString(button3Number))) {
                    completedTimer = true;
                } else {
                    lostGame = true;
                }
            }
        });
        //endregion

        //regionPress button game -P
        //The button will change colors, and while you hold it, it either goes up, or goes down.

        //regionRun the game
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (!lostGame && !pressDisabled){
                    timeLeft -= 0.1;
                    if (btnPress.isPressed()) {
                        timeLeft += buttonRate;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (buttonRate > 0) {
                                    btnPress.setText(">>>>>>>>>>>>>>>>>>>>>>>>>");
                                } else {
                                    btnPress.setText("<<<<<<<<<<<<<<<<<<<<<<<<<");
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnPress.setText("");
                            }
                        });
                    }
                    if (timeLeft < 0) {
                        lostGame = true;
                    }
                    pbPress.setProgress((int)(100*(timeLeft/30)));
                    if (timeLeft > 30) {
                        timeLeft = 30;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion

        //regionChange the button property
        new Thread(){
            @Override
            public void run() {
                super.run();
                int negativeCount = 0;
                while (!lostGame && !pressDisabled){
                    if (negativeCount < 3.0) {
                        buttonRate = (double)(RandRange(-100,100))/100;
                        if (buttonRate < 0) {
                            ++negativeCount;
                        } else {
                            negativeCount = 0;
                        }
                    } else {
                        negativeCount = 0;
                        buttonRate = 0.5;
                    }
                    //Change the button to a new random color so the user knows something happened
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int red = RandRange(0,255);
                            int green = RandRange(0,255);
                            int blue = RandRange(0,255);
                            int color = Color.rgb(red,green,blue);
                            btnPress.setBackgroundColor(color);
                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion

        //endregion

        //region Bar balancing game -P
        //regionGame part that user controls, and increments progressbars
        new Thread(){
            @Override
            public void run() {
                super.run();
                //Put both seekbars in the middle
                sb1.setProgress(50);
                sb2.setProgress(50);
                //Put both bars in center
                pbBar1.setProgress(50);
                pbBar2.setProgress(50);

                while(!lostGame && !barsDisabled) {
                    //The user rate is determined by where the user puts the button
                    int userRate1 = (sb1.getProgress() - 50) / 5;
                    int userRate2 = (sb2.getProgress() - 50) / 5;
                    //barRate is determined in the next thread, and randomly changes, so the user
                    //has to constantly change up the bars so it doesn't reach the ends
                    bar1 += userRate1 + barRate1;
                    bar2 += userRate2 + barRate2;

                    //Show progress bars so the user knows where the bars are at
                    pbBar1.setProgress((int) (100.0*((double)bar1/(double)1000)));
                    pbBar2.setProgress((int) (100.0*((double)bar2/(double)1000)));

                    //End the game if the bars reach either end
                    if (bar1 < 0 || bar1 > 1000){
                        lostGame = true;
                    }

                    if(bar2 < 0 || bar2 > 1000) {
                        lostGame = true;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion

        //regionChange up the rates every so often
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(!lostGame){
                    if (new Random().nextBoolean()) {
                        barRate1 = RandRange(-7,7,"min");
                    } else {
                        barRate1 = RandRange(-7,7,"max");
                    }

                    if (new Random().nextBoolean()) {
                        barRate2 = RandRange(-7,7,"min");
                    } else {
                        barRate2 = RandRange(-7,7,"max");
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion

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

    //Method to generate random numbers in a range, but weighted -P
    public int RandRange(int min, int max, String direction) {
        int middle = (max - min)/2 + min;
        int choice = new Random().nextInt((max - min) + 1) + min;
        boolean chosen = false;
        while(!chosen){
            //Takes two directions, max or min, and will skew results towards that direction
            if(direction.equals("max")){
                if (choice < middle) {
                    if(new Random().nextInt(10) < 2) {
                        chosen = true;
                    } else {
                        choice = new Random().nextInt((max-min) + 1) + min;
                    }
                } else {
                    chosen = true;
                }
            } else if (direction.equals("min")) {
                if (choice > middle) {
                    if(new Random().nextInt(10) < 2) {
                        chosen = true;
                    } else {
                        choice = new Random().nextInt((max-min) + 1) + min;
                    }
                } else {
                    chosen = true;
                }
            } else {
                //Crash the app so I know I screwed up
                Toast.makeText(getApplicationContext(),"you messed up",Toast.LENGTH_SHORT).show();
            }
        }
        return choice;
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

    //Add commas to numbers -P
    public String addCommas(long inputNumber){
        String digits = Long.toString(inputNumber);
        StringBuilder result = new StringBuilder();
        for (int i=1; i <= digits.length(); ++i) {
            char ch = digits.charAt(digits.length() - i);
            if (i % 3 == 1 && i > 1) {
                result.insert(0, ",");
            }
            result.insert(0, ch);
        }
        return result.toString();
    }
}