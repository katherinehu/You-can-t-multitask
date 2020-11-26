package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/*
Main menu where so you can either go to the multitask game or you can go to the auction
Fairly simple, not much to see here.
-P
 */
public class MainActivity extends AppCompatActivity{

    Button btnMultitask, btnAuction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Menu");

        //regionAssign the button views
        btnMultitask = findViewById(R.id.btnGame);
        btnAuction = findViewById(R.id.btnAuctionMenu);
        //endregion

        //regionOpen the right activities when buttons clicked
        final Intent openGame = new Intent(this,MultiTaskMainMenu.class);
        btnMultitask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(openGame);
            }
        });

        final Intent openAuction = new Intent(this,AuctionMainMenu.class);
        btnAuction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(openAuction);
            }
        });
        //endregion
    }
}