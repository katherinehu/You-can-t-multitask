package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class DisplayData extends AppCompatActivity {

    RecyclerView rc;
    RecyclerView.Adapter adapter;
    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        rc = findViewById(R.id.my_recycler_view);
        rc.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(users);
        rc.setAdapter(adapter);
    }
}