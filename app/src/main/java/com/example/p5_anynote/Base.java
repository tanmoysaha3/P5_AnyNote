package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class Base extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar mainToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mainToolbar=findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        mainToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));

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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notesNavM:
                startActivity(new Intent(getApplicationContext(),Notes.class));
                break;
            /*case R.id.newNoteNavM:
                startActivity(new Intent(getApplicationContext(),NewNote.class));
                break;*/
            case R.id.archiveNavM:
                startActivity(new Intent(getApplicationContext(), Archive.class));
                break;
            case R.id.trashNavM:
                startActivity(new Intent(getApplicationContext(), Trash.class));
                break;
            case R.id.personalNavM:
                Intent intent=new Intent(getApplicationContext(), NotesAsLabel.class);
                intent.putExtra("Label","Personal");
                startActivity(intent);
                break;
            /*case R.id.homeNavM:
                Intent intent1=new Intent(getApplicationContext(),NotesAsLabel.class);
                intent1.putExtra("Label","Home");
                startActivity(intent1);
                break;
            case R.id.workNavM:
                Intent intent2=new Intent(getApplicationContext(),NotesAsLabel.class);
                intent2.putExtra("Label","Work");
                startActivity(intent2);
                break;
            case R.id.studyNavM:
                Intent intent3=new Intent(getApplicationContext(),NotesAsLabel.class);
                intent3.putExtra("Label","Study");
                startActivity(intent3);
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
                break;*/
            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}