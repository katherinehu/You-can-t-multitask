package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MultiTaskMainMenu extends AppCompatActivity {

    Button btnStartGame;

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
        //endregion

        //regionPlay the game -P
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MultiTaskGame.class));
            }
        });
        //endregion
    }
}