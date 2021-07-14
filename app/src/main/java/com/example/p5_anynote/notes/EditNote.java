package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class EditNote extends AppCompatActivity {

    EditText titleEditNote, contentEditNote;
    ImageButton saveEditNote;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    ProgressBar editNotePBar;
    Toolbar editNoteToolbar;
    Spinner labelEditNoteS;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        PowerPreference.init(this);

        Intent data=getIntent();
        String noteId=data.getStringExtra("NoteId");
        String title=data.getStringExtra("Title");
        String content=data.getStringExtra("Content");
        int colorCode=data.getIntExtra("ColorCode",0);
        String label=data.getStringExtra("Label");
        String important=data.getStringExtra("Important");

        editNoteToolbar=findViewById(R.id.editNoteToolbar);
        setSupportActionBar(editNoteToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleEditNote=findViewById(R.id.titleEditNote);
        contentEditNote=findViewById(R.id.contentEditNote);
        saveEditNote=findViewById(R.id.saveEditNote);
        editNotePBar=findViewById(R.id.editNotePBar);
        labelEditNoteS=findViewById(R.id.labelEditNoteS);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        titleEditNote.setText(title);
        contentEditNote.setText(content);
        contentEditNote.setBackgroundColor(getResources().getColor(colorCode,null));

        TextWatcher textWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                saveEditNote.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        titleEditNote.addTextChangedListener(textWatcher);
        contentEditNote.addTextChangedListener(textWatcher);

        CollectionReference labelsRef=fStore.collection("Notes").document(fUser.getUid())
                .collection("Labels");
        List<String> labels=new ArrayList<>();
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelEditNoteS.setAdapter(adapter);
        labelEditNoteS.setSelection(labels.indexOf(label));

        labelsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        String label=documentSnapshot.getString("Label_Name");
                        labels.add(label);
                    }
                    adapter.notifyDataSetChanged();
                    //Spinner default selected value
                    labelEditNoteS.setSelection(adapter.getPosition(label));
                }
            }
        });

        labelEditNoteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                saveEditNote.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nTitle=titleEditNote.getText().toString();
                String nContent=contentEditNote.getText().toString();

                if (nTitle.isEmpty()){
                    nTitle="Empty";
                }
                if (nContent.isEmpty()){
                    Toast.makeText(EditNote.this, "Note content is empty. Write down something.", Toast.LENGTH_SHORT).show();
                    return;
                }

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
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(nContent, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                String ciphertextString = cipherTextIvMac.toString();

                editNotePBar.setVisibility(View.VISIBLE);

                DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                        .collection("MyNotes").document(noteId);
                Map<String,Object> note=new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title",nTitle);
                note.put("Content",ciphertextString);
                note.put("Label",labelEditNoteS.getSelectedItem().toString());
                docRef.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditNote.this, "Note Edited.", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), Notes.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error. Try again please.", Toast.LENGTH_SHORT).show();
                        editNotePBar.setVisibility(View.INVISIBLE);
                    }
                });
                startActivity(new Intent(getApplicationContext(), Notes.class));
            }
        });
    }
}