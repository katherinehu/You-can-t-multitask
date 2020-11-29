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
import android.widget.ImageView;

public class MultiTaskMainMenu extends AppCompatActivity {

    Button btnStartGame;
    CheckBox checkTilt, checkMath, checkColor, checkTimerReaction, checkBars;

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

        //regionDeclare UI elements
        btnStartGame = findViewById(R.id.btnPlayGame);
        checkTilt = findViewById(R.id.checkTilt);
        checkMath = findViewById(R.id.checkMath);
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

        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = data.edit();
        boolean tiltDisabled = data.getBoolean("tiltDisabled",false);
        boolean mathDisabled = data.getBoolean("mathDisabled",false);
        boolean colorDisabled = data.getBoolean("colorDisabled",false);
        boolean timerDisabled = data.getBoolean("timerDisabled",false);
        boolean barsDisabled = data.getBoolean("barsDisabled",false);
        checkTilt.setChecked(tiltDisabled);
        checkMath.setChecked(mathDisabled);
        checkTimerReaction.setChecked(timerDisabled);
        checkColor.setChecked(colorDisabled);
        checkBars.setChecked(barsDisabled);

        final int[] gamesDisabled = {0};

        if(tiltDisabled) {
            ++gamesDisabled[0];
        }
        if(mathDisabled){
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
        //endregion

    }
}