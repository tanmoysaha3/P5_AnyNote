package com.example.p5_anynote.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p5_anynote.notes.Notes;
import com.example.p5_anynote.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {

    EditText nameReg, emailReg, passReg, confirmPassReg;
    TextView loginAC;
    Button regB;
    ProgressBar regPBar;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameReg=findViewById(R.id.nameRegister);
        emailReg=findViewById(R.id.emailRegister);
        passReg=findViewById(R.id.passwordRegister);
        confirmPassReg=findViewById(R.id.confirmPassRegister);
        loginAC=findViewById(R.id.loginAC);
        regB=findViewById(R.id.registerB);
        regPBar=findViewById(R.id.registerPBar);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        regB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=nameReg.getText().toString();
                String email=emailReg.getText().toString();
                String pass=passReg.getText().toString();
                String cPass=confirmPassReg.getText().toString();

                if(name.isEmpty()){
                    nameReg.setError("Name empty");
                    return;
                }

                if (email.isEmpty()){
                    emailReg.setError("Email empty");
                    return;
                }

                if (pass.isEmpty()){
                    passReg.setError("Password empty");
                    return;
                }

                if(!pass.equals(cPass)){
                    confirmPassReg.setError("Passwords don't match");
                    return;
                }

                regPBar.setVisibility(View.VISIBLE);

                AuthCredential credential= EmailAuthProvider.getCredential(email,pass);
                fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Register.this, "Account connected", Toast.LENGTH_SHORT).show();

                        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        fUser.updateProfile(request);
                        startActivity(new Intent(getApplicationContext(), Notes.class));
                        finish();
                        //overridePendingTransition(R.animator.slide_up,R.animator.slide_down);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Error", Toast.LENGTH_SHORT).show();
                        regPBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }
}