package com.example.p5_anynote.security;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p5_anynote.notes.FirstNote;
import com.example.p5_anynote.R;
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
import static com.tozny.crypto.android.AesCbcWithIntegrity.generateSalt;
import static com.tozny.crypto.android.AesCbcWithIntegrity.saltString;

public class CreateEncryptionPass extends AppCompatActivity {

    EditText encryptionPass, encryptionCPass;
    Button createEncryptionPass;
    String encryptPass;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_encryption_pass);

        PowerPreference.init(this);

        encryptionPass=findViewById(R.id.encryptionPass);
        encryptionCPass=findViewById(R.id.encryptionCPass);
        createEncryptionPass=findViewById(R.id.createEncryptionPass);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        createEncryptionPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass=encryptionPass.getText().toString();
                String cPass=encryptionCPass.getText().toString();

                if ((!pass.isEmpty()) && pass.equals(cPass)){
                    encryptPass=pass;
                    String salt = null;
                    try {
                        salt = saltString(generateSalt());
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }

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

                    PowerPreference.getDefaultFile().putString("AnyNoteEncryptionPass",encryptPass);
                    PowerPreference.getDefaultFile().putString("AnyNoteEncryptionSalt",salt);

                    DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                            .collection("Security").document("Salt");
                    Map<String,Object> note=new HashMap<>();
                    note.put("Salt",salt);
                    note.put("Content",ciphertextString);
                    note.put("Date", Timestamp.now());
                    docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CreateEncryptionPass.this, "Successfully saved salt", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateEncryptionPass.this, "Error occurred in saving salt", Toast.LENGTH_SHORT).show();
                        }
                    });

                    startActivity(new Intent(getApplicationContext(), FirstNote.class));
                }
                else {
                    encryptionCPass.setError("Passwords don't match");
                }
            }
        });
    }
}