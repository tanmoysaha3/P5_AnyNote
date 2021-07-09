package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.preference.PowerPreference;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class CheckUser extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);

        PowerPreference.init(this);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        Toast.makeText(this, "Wait please", Toast.LENGTH_SHORT).show();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Handler handler=new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline()){
                    //this preference still not used anywhere
                    PowerPreference.getDefaultFile().putString("NetworkStatus","Online");
                    if (fAuth.getCurrentUser()!=null){
                        startActivity(new Intent(getApplicationContext(), Notes.class));
                        finish();
                    }
                    //create anonymous account
                    else {
                        fAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(CheckUser.this, "Logged in with a temporary account", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), FirstNote.class));
                                startActivity(new Intent(getApplicationContext(), CreateEncryptionPass.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CheckUser.this, "Error", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }
                else {
                    PowerPreference.getDefaultFile().putString("NetworkStatus","Offline");
                    offlineAlert();
                    Toast.makeText(CheckUser.this, "Offline", Toast.LENGTH_SHORT).show();
                }
            }
        },2000);
    }

    private boolean isOnline(){
        try {
            int timeoutMs=1500;
            Socket socket=new Socket();
            SocketAddress socketAddress=new InetSocketAddress("8.8.8.8",53);

            socket.connect(socketAddress,timeoutMs);
            socket.close();
            return true;
        } catch (IOException e){
            return false;
        }
    }

    private void offlineAlert(){
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Network Error")
                .setMessage("You are not connected with internet")
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                    }
                }).setNegativeButton("Browse Offline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (fAuth.getCurrentUser()!=null){
                            startActivity(new Intent(getApplicationContext(),Notes.class));
                            finish();
                        }
                        else {
                            Toast.makeText(CheckUser.this, "Need internet for first time use", Toast.LENGTH_SHORT).show();
                        }
                        //startActivity(new Intent(getApplicationContext(), Notes.class));
                        //startActivity(new Intent(getApplicationContext(), OfflineMainActivity.class));
                    }
                });
        warning.show();
    }
}