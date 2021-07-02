package com.example.p5_anynote;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class NotesAsLabel extends Base{

    LayoutInflater inflater;
    TextView notesAsLabelText;
    TextView titleNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_notes_as_label,null,false);
        drawerLayout.addView(contentView,0);

        notesAsLabelText=findViewById(R.id.notesAsLabelText);
        notesAsLabelText.setText("NOTES AS LABEL");

        View toolbarView=inflater.inflate(R.layout.notes_as_label_toolbar,null);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(toolbarView);

        titleNewNote=findViewById(R.id.titleNewNote);
        titleNewNote.setText("Personal");
    }
}