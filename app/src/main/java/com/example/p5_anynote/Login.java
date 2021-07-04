package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {

    EditText emailLogin, passLogin;
    Button loginB;
    TextView forgetPass, createAC;
    ProgressBar loginPBar;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin=findViewById(R.id.emailLogin);
        passLogin=findViewById(R.id.passwordLogin);
        loginB=findViewById(R.id.loginB);
        forgetPass=findViewById(R.id.forgerPassword);
        createAC=findViewById(R.id.createAC);
        loginPBar=findViewById(R.id.loginPBar);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        showWarning();

        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailLogin.getText().toString();
                String pass=passLogin.getText().toString();

                if (email.isEmpty()){
                    emailLogin.setError("Email empty");
                    return;
                }
                if (pass.isEmpty()){
                    passLogin.setError("Password empty");
                    return;
                }

                loginPBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Login.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), ConfirmEncryptionPass.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                        loginPBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void showWarning(){
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Are you sure")
                .setMessage("Linking with existing account will delete all temporary notes. Create new account to save them.")
                .setPositiveButton("Save Notes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(),Register.class));
                        finish();
                    }
                }).setNegativeButton("It's ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Login.this, "Deleting temporary account's notes", Toast.LENGTH_SHORT).show();
                        fStore.collection("Notes").document(fUser.getUid()).collection("MyNotes")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        fStore.collection("Notes").document(fUser.getUid())
                                                .collection("MyNotes").document(document.getId()).delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(Login.this, "Successfully deleted notes", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Login.this, "Error happened. Try again please.", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),Login.class));
                                            }
                                        });
                                    }
                                }
                                else {
                                    Toast.makeText(Login.this, "Error in deleting notes", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        Toast.makeText(Login.this, "Deleting Temporary account", Toast.LENGTH_SHORT).show();
                        fUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Successfully deleted account", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error in deleting account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        warning.show();
    }
}