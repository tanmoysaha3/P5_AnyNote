package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.preference.PowerPreference;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKeyFromPassword;

public class NewNote extends AppCompatActivity {

    EditText titleNewNote, contentNewNote;
    Toolbar newNoteToolbar;
    ProgressBar newNotePBar;
    ImageView saveNewNote;
    Spinner labelNewNoteS;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        PowerPreference.init(this);

        titleNewNote=findViewById(R.id.titleNewNote);
        contentNewNote=findViewById(R.id.contentNewNote);
        newNoteToolbar=findViewById(R.id.newNoteToolbar);
        newNotePBar=findViewById(R.id.newNotePBar);
        saveNewNote=findViewById(R.id.saveNewNote);
        labelNewNoteS=findViewById(R.id.labelNewNoteS);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        setSupportActionBar(newNoteToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextWatcher textWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                saveNewNote.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        contentNewNote.addTextChangedListener(textWatcher);

        CollectionReference labelsRef=fStore.collection("Notes").document(fUser.getUid())
                .collection("Labels");
        List<String> labels=new ArrayList<>();
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelNewNoteS.setAdapter(adapter);

        labelsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        String label=documentSnapshot.getString("Label_Name");
                        labels.add(label);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        saveNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title=titleNewNote.getText().toString();
                String content=contentNewNote.getText().toString();

                if (title.isEmpty()){
                    title="Untitled";
                }
                if (content.isEmpty()){
                    Toast.makeText(NewNote.this, "Empty notes can't be saved", Toast.LENGTH_SHORT).show();
                    return;
                }

                /*List<String> subTitles=new ArrayList<>();
                String subTitle="0";
                for (int i=0;i<title.length();i++){
                    for (int j=0;j<title.length();j++){
                        subTitle=title.substring(i);
                    }
                    if (i==title.length()-1){
                        break;
                    }
                    subTitles.add(subTitle);
                }*/

                //Toast.makeText(NewNote.this, "subTitles"+subTitles, Toast.LENGTH_SHORT).show();

                String pass = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionPass");
                String salt = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionSalt");
                //String plainText="Empty";
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
                try {
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(content, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                //store or send to server
                String ciphertextString = cipherTextIvMac.toString();

                newNotePBar.setVisibility(View.VISIBLE);
                DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                        .collection("MyNotes").document();
                Map<String,Object> note=new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title", title);
                note.put("Content", ciphertextString);
                note.put("Label",labelNewNoteS.getSelectedItem().toString());
                note.put("Important","0");
                //note.put("SubTitles",subTitles);
                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewNote.this, "New note created", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), Notes.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewNote.this, "Error", Toast.LENGTH_SHORT).show();
                        newNotePBar.setVisibility(View.INVISIBLE);
                    }
                });
                startActivity(new Intent(getApplicationContext(), Notes.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.new_note_menu,menu);
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