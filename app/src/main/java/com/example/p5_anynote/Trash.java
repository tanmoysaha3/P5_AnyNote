package com.example.p5_anynote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Trash extends Base {

    LayoutInflater inflater;
    TextView trashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_trash,null,false);
        drawerLayout.addView(contentView,0);

        trashText=findViewById(R.id.trashText);
        trashText.setText("TRASH");
    }
}