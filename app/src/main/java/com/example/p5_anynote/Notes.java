package com.example.p5_anynote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Notes extends Base {

    LayoutInflater inflater;
    TextView notesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_notes,null,false);
        drawerLayout.addView(contentView,0);

        notesText=findViewById(R.id.notesText);
        notesText.setText("NOTES");
    }
}