package com.example.p5_anynote.options;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.example.p5_anynote.R;

public class Tips extends AppCompatActivity {

    Toolbar tipsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        tipsToolbar=findViewById(R.id.tipsToolbar);

        setSupportActionBar(tipsToolbar);
        getSupportActionBar().setTitle("Tips");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}