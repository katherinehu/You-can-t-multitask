package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MultiTaskMainMenu extends AppCompatActivity {

    Button btnStartGame;
    CheckBox checkTilt, checkPress, checkColor, checkTimerReaction, checkBars;
    TextView tvHighScore;

    /*
    Gives you the option to start the game, and adjust any gameplay settings right on the screen.
    I'll probably add instructions as well, so people can understand each part of the game.
    Perhaps even the option to view high scores? This time around I'll probably make google sign in automatic
     -P
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task_main_menu);

        //Hide bar at the top
        this.getSupportActionBar().hide();

        //regionDeclare UI elements
        tvHighScore = findViewById(R.id.tvHighScore);
        btnStartGame = findViewById(R.id.btnPlayGame);
        checkTilt = findViewById(R.id.checkTilt);
        checkPress = findViewById(R.id.checkPress);
        checkColor = findViewById(R.id.checkColor);
        checkTimerReaction = findViewById(R.id.checkTimerReaction);
        checkBars = findViewById(R.id.checkBars);
        //endregion

        //regionPlay the game -P
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MultiTaskGame.class));
            }
        });
        //endregion

        //regionGive user option to disable certain games -P
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = data.edit();
        boolean tiltDisabled = data.getBoolean("tiltDisabled",false);
        boolean pressDisabled = data.getBoolean("pressDisabled",false);
        boolean colorDisabled = data.getBoolean("colorDisabled",false);
        boolean timerDisabled = data.getBoolean("timerDisabled",false);
        boolean barsDisabled = data.getBoolean("barsDisabled",false);
        checkTilt.setChecked(tiltDisabled);
        checkPress.setChecked(pressDisabled);
        checkTimerReaction.setChecked(timerDisabled);
        checkColor.setChecked(colorDisabled);
        checkBars.setChecked(barsDisabled);

        final int[] gamesDisabled = {0};

        if(tiltDisabled) {
            ++gamesDisabled[0];
        }
        if(pressDisabled){
            ++gamesDisabled[0];
        }
        if(timerDisabled) {
            ++gamesDisabled[0];
        }
        if (colorDisabled) {
            ++gamesDisabled[0];
        }
        if (barsDisabled) {
            ++gamesDisabled[0];
        }

        checkTilt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //b is true when the button is checked
                if (b) {
                    editor.putBoolean("tiltDisabled",true);
                    ++gamesDisabled[0];
                } else {
                    editor.putBoolean("tiltDisabled",false);
                    --gamesDisabled[0];
                }
                editor.putInt("gamesDisabled",gamesDisabled[0]);
                editor.commit();
            }
        });

        checkPress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //b is true when the button is checked
                if (b) {
                    editor.putBoolean("pressDisabled",true);
                    ++gamesDisabled[0];
                } else {
                    editor.putBoolean("pressDisabled",false);
                    --gamesDisabled[0];
                }
                editor.putInt("gamesDisabled",gamesDisabled[0]);
                editor.commit();
            }
        });

        checkTimerReaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //b is true when the button is checked
                if (b) {
                    editor.putBoolean("timerDisabled",true);
                    ++gamesDisabled[0];
                } else {
                    editor.putBoolean("timerDisabled",false);
                    --gamesDisabled[0];
                }
                editor.putInt("gamesDisabled",gamesDisabled[0]);
                editor.commit();
            }
        });

        checkColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //b is true when the button is checked
                if (b) {
                    editor.putBoolean("colorDisabled",true);
                    ++gamesDisabled[0];
                } else {
                    editor.putBoolean("colorDisabled",false);
                    --gamesDisabled[0];
                }
                editor.putInt("gamesDisabled",gamesDisabled[0]);
                editor.commit();
            }
        });

        checkBars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //b is true when the button is checked
                if (b) {
                    editor.putBoolean("barsDisabled",true);
                    ++gamesDisabled[0];
                } else {
                    editor.putBoolean("barsDisabled",false);
                    --gamesDisabled[0];
                }
                editor.putInt("gamesDisabled",gamesDisabled[0]);
                editor.commit();
            }
        });
        //endregion

        //regionDisplay high score
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    long highScore = data.getLong("highScore",0);
                    String display = "High Score: " + addCommas(highScore) + "\n";
                    int gamesDisabled = data.getInt("gamesDisabled",0);
                    switch (gamesDisabled){
                        case 1:
                            display += "\nOh I'm sure that'll help.";
                            break;
                        case 2:
                            display += "\nCall it a compromise?";
                            break;
                        case 3:
                            display += "\nAccept your human limitations.";
                            break;
                        case 4:
                            display += "\nYou can't multitask. Just as expected.";
                            break;
                        case 5:
                            display += "\nAh yes, just give up. Give into your inferiority.";
                            break;
                        default:
                            display += "\nFeeling brave, are you?.\nLet's see how long it lasts.";
                    }
                    final String finalDisplay = display;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvHighScore.setText(finalDisplay);
                        }
                    });
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