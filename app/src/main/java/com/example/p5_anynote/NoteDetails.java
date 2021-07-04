package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteDetails extends AppCompatActivity {

    TextView titleNoteDetails, labelNoteDetails, contentNoteDetails;
    Toolbar noteDetailsToolbar;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        Intent data=getIntent();
        String noteId=data.getStringExtra("NoteId");
        String title=data.getStringExtra("Title");
        String content=data.getStringExtra("Content");
        String label=data.getStringExtra("Label");
        int colorCode=data.getIntExtra("ColorCode",0);
        String pageName=data.getStringExtra("PageName");

        titleNoteDetails=findViewById(R.id.titleNoteDetails);
        labelNoteDetails=findViewById(R.id.labelNoteDetails);
        contentNoteDetails=findViewById(R.id.contentNoteDetails);
        noteDetailsToolbar=findViewById(R.id.noteDetailsToolbar);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        setSupportActionBar(noteDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        contentNoteDetails.setMovementMethod(new ScrollingMovementMethod());

        titleNoteDetails.setText(title);
        labelNoteDetails.setText(label);
        contentNoteDetails.setText(content);
        contentNoteDetails.setBackgroundColor(ContextCompat.getColor(this, colorCode));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.note_details_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}