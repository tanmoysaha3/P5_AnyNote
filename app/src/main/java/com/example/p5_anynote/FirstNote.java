package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirstNote extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        Resources res = getResources();
        String[] systemLabels = res.getStringArray(R.array.SystemLabels);

        DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                .collection("MyNotes").document("Sample");
        Map<String,Object> note=new HashMap<>();
        note.put("Date", Timestamp.now());
        note.put("Title","Sample Note Title");
        note.put("Content","Let start writing your own notes.");
        note.put("Label","None");
        note.put("Important","0");
        docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FirstNote.this, "First note created", Toast.LENGTH_SHORT).show();
                for (int i = 0; i <= 3; i++){
                    DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                            .collection("Labels").document(systemLabels[i]);
                    Map<String,Object> label=new HashMap<>();
                    label.put("Label_Name",systemLabels[i]);
                    docRef.set(label).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(FirstNote.this, "System labels created", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FirstNote.this, "System labels created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                startActivity(new Intent(getApplicationContext(), Notes.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FirstNote.this, "Error in creating first note", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),CheckUser.class));
            }
        });

    }
}