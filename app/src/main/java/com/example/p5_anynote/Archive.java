package com.example.p5_anynote;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Archive extends Base {

    LayoutInflater inflater;
    TextView archiveText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_archive,null,false);
        drawerLayout.addView(contentView,0);

        archiveText=findViewById(R.id.archiveText);
        archiveText.setText("ARCHIVE");

        getSupportActionBar().setTitle("Archive");
    }
}