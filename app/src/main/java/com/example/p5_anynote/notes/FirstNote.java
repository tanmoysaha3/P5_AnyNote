package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.example.p5_anynote.account.CheckUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.preference.PowerPreference;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKeyFromPassword;

public class FirstNote extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerPreference.init(this);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        String pass = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionPass");
        String salt = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionSalt");

        try {
            keys = generateKeyFromPassword(pass, salt);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
        try {
            cipherTextIvMac = AesCbcWithIntegrity.encrypt("Let start writing your own notes.", keys);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        //store or send to server
        String ciphertextString = cipherTextIvMac.toString();

        Resources res = getResources();
        String[] systemLabels = res.getStringArray(R.array.SystemLabels);

        DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                .collection("MyNotes").document("Sample");
        Map<String,Object> note=new HashMap<>();
        note.put("Date", Timestamp.now());
        note.put("Title","Sample Note Title");
        note.put("Content",ciphertextString);
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
                startActivity(new Intent(getApplicationContext(), CheckUser.class));
            }
        });

    }
}