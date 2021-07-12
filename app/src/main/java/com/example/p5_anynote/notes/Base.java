package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.example.p5_anynote.account.CheckUser;
import com.example.p5_anynote.account.Login;
import com.example.p5_anynote.account.Register;
import com.example.p5_anynote.options.Tips;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Base extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar mainToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navView;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mainToolbar=findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        mainToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fUser=fAuth.getCurrentUser();

        navView=findViewById(R.id.navView);
        drawerLayout=findViewById(R.id.drawerLayout);

        navView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,mainToolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        View headerView=navView.getHeaderView(0);
        TextView userName=headerView.findViewById(R.id.showUserName);
        TextView userEmail=headerView.findViewById(R.id.showUserEmail);

        if (fUser.isAnonymous()){
            userName.setText("Temporary User");
            userEmail.setVisibility(View.GONE);
        }
        else {
            userName.setText(fUser.getDisplayName());
            userEmail.setText(fUser.getEmail());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notesNavM:
                startActivity(new Intent(getApplicationContext(),Notes.class));
                break;
            case R.id.newNoteNavM:
                startActivity(new Intent(getApplicationContext(),NewNote.class));
                break;
            case R.id.archiveNavM:
                startActivity(new Intent(getApplicationContext(), Archive.class));
                break;
            case R.id.trashNavM:
                startActivity(new Intent(getApplicationContext(), Trash.class));
                break;
            case R.id.importantNavM:
                Intent intent=new Intent(getApplicationContext(),Notes.class);
                intent.putExtra("Importance","1");
                //Toast.makeText(this, "labelLabel" +intent, Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
            case R.id.editLabelsNavM:
                startActivity(new Intent(getApplicationContext(),EditLabels.class));
                break;
            case R.id.connectAC:
                if (fUser.isAnonymous()){
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
                else {
                    Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.logout:
                checkUser();
                break;
            case R.id.deleteAC:
                displayACDeleteAlert();
                break;
            case R.id.tipsNavM:
                startActivity(new Intent(getApplicationContext(), Tips.class));
                break;
            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser(){
        if (fUser.isAnonymous()){
            displayLogoutAlert();
        }
        else {
            fAuth.signOut();
            startActivity(new Intent(getApplicationContext(), CheckUser.class));
            finish();
        }
    }

    private void displayLogoutAlert(){
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Are you sure")
                .setMessage("You are logged in with temporary account. Logging out without connecting a permanent account will delete all notes")
                .setPositiveButton("Connect Account", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                });
        warning.show();
    }

    private void deleteAccount(){
        Toast.makeText(Base.this, "Deleting temporary account's notes", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(Base.this, "Successfully deleted notes", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Base.this, "Error happened. Try again please.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),Login.class));
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(Base.this, "Error in deleting notes", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fStore.collection("Notes").document(fUser.getUid()).collection("Archive")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        fStore.collection("Notes").document(fUser.getUid())
                                .collection("Archive").document(document.getId()).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Base.this, "Successfully deleted archived notes", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Base.this, "Error happened. Try again please.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),Login.class));
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(Base.this, "Error in deleting notes", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fStore.collection("Notes").document(fUser.getUid()).collection("Trash")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        fStore.collection("Notes").document(fUser.getUid())
                                .collection("Trash").document(document.getId()).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Base.this, "Successfully deleted trash notes", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Base.this, "Error happened. Try again please.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),Login.class));
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(Base.this, "Error in deleting notes", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Toast.makeText(Base.this, "Deleting Temporary account", Toast.LENGTH_SHORT).show();
        fUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Base.this, "Successfully deleted account", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),Notes.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Base.this, "Error in deleting account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayACDeleteAlert(){
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Are you sure")
                .setMessage("Deleting account will delete all your data permanently. No data is recoverable.")
                .setPositiveButton("No. Go back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Base.this, "Deleting", Toast.LENGTH_SHORT).show();
                        deleteAccount();
                    }
                });
        warning.show();
    }
}