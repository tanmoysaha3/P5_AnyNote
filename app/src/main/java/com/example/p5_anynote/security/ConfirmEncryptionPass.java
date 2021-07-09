package com.example.p5_anynote.security;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p5_anynote.notes.Notes;
import com.example.p5_anynote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.preference.PowerPreference;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKeyFromPassword;

public class ConfirmEncryptionPass extends AppCompatActivity {

    EditText encryptionCPassword;
    Button confirmEncryptionPass;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    String salt;
    AesCbcWithIntegrity.SecretKeys keys;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_encryption_pass);

        encryptionCPassword=findViewById(R.id.encryptionCPassword);
        confirmEncryptionPass=findViewById(R.id.confirmEncryptionPass);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        confirmEncryptionPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass=encryptionCPassword.getText().toString();

                if (!pass.isEmpty()) {
                    DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                            .collection("Security").document("Salt");
                    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            salt=value.getString("Salt");

                            Toast.makeText(ConfirmEncryptionPass.this, "pass= "+pass, Toast.LENGTH_SHORT).show();
                            Toast.makeText(ConfirmEncryptionPass.this, "salt= "+salt, Toast.LENGTH_SHORT).show();

                            try {
                                keys = generateKeyFromPassword(pass, salt);
                            } catch (GeneralSecurityException e) {
                                e.printStackTrace();
                            }

                            DocumentReference docRef1=fStore.collection("Notes").document(fUser.getUid())
                                    .collection("MyNotes").document("Sample");
                            docRef1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    content=value.getString("Content");

                                    String plainText="Empty";
                                    AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(content);
                                    try {
                                        plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    } catch (GeneralSecurityException e) {
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(ConfirmEncryptionPass.this, "plainText= "+plainText, Toast.LENGTH_SHORT).show();

                                    if (plainText.equals("Let start writing your own notes.")){
                                        PowerPreference.getDefaultFile().putString("AnyNoteEncryptionPass",pass);
                                        PowerPreference.getDefaultFile().putString("AnyNoteEncryptionSalt",salt);

                                        startActivity(new Intent(getApplicationContext(), Notes.class));
                                    }

                                    else {
                                        Toast.makeText(ConfirmEncryptionPass.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
                else {
                    encryptionCPassword.setError("Empty");
                }
            }
        });
    }
}