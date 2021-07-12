package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class NoteDetails extends AppCompatActivity {

    TextView titleNoteDetails, labelNoteDetails, contentNoteDetails;
    Toolbar noteDetailsToolbar;
    FloatingActionButton editNoteDetailsFAB, deleteNoteDetailsFAB, deletePermNoteDetailsFAB, archiveNoteDetailsFAB,
            restoreNoteDetailsFAB, unarchiveNoteDetailsFAB;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        PowerPreference.init(this);

        Intent data=getIntent();
        String noteId=data.getStringExtra("NoteId");
        String title=data.getStringExtra("Title");
        String content=data.getStringExtra("Content");
        String label=data.getStringExtra("Label");
        String important=data.getStringExtra("Important");
        Toast.makeText(this, "important "+important, Toast.LENGTH_SHORT).show();
        int colorCode=data.getIntExtra("ColorCode",0);
        String pageName=data.getStringExtra("PageName");

        titleNoteDetails=findViewById(R.id.titleNoteDetails);
        labelNoteDetails=findViewById(R.id.labelNoteDetails);
        contentNoteDetails=findViewById(R.id.contentNoteDetails);
        noteDetailsToolbar=findViewById(R.id.noteDetailsToolbar);
        editNoteDetailsFAB=findViewById(R.id.editNoteDeatailsFAB);
        deleteNoteDetailsFAB=findViewById(R.id.deleteNoteDetailsFAB);
        deletePermNoteDetailsFAB=findViewById(R.id.deletePermNoteDetailsFAB);
        archiveNoteDetailsFAB=findViewById(R.id.archiveNoteDetailsFAB);
        restoreNoteDetailsFAB=findViewById(R.id.restoreNoteDetailsFAB);
        unarchiveNoteDetailsFAB=findViewById(R.id.unarchiveNoteDetailsFAB);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        setSupportActionBar(noteDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        contentNoteDetails.setMovementMethod(new ScrollingMovementMethod());

        titleNoteDetails.setText(title);
        labelNoteDetails.setText(label);
        contentNoteDetails.setText(content);
        contentNoteDetails.setBackgroundColor(ContextCompat.getColor(this, colorCode));

        String pass = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionPass");
        String salt = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionSalt");
        String subCollection="Empty";

        if (pageName.equals("Notes")){
            editNoteDetailsFAB.setVisibility(View.VISIBLE);
            deleteNoteDetailsFAB.setVisibility(View.VISIBLE);
            archiveNoteDetailsFAB.setVisibility(View.VISIBLE);
            subCollection="MyNotes";
        }
        else if (pageName.equals("Trash")){
            restoreNoteDetailsFAB.setVisibility(View.VISIBLE);
            deletePermNoteDetailsFAB.setVisibility(View.VISIBLE);
            subCollection="Trash";
        }
        else if (pageName.equals("Archive")){
            unarchiveNoteDetailsFAB.setVisibility(View.VISIBLE);
            deleteNoteDetailsFAB.setVisibility(View.VISIBLE);
            subCollection="Archive";
        }

        editNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),EditNote.class);
                intent.putExtra("Title",title);
                intent.putExtra("Content",content);
                intent.putExtra("Label",label);
                intent.putExtra("ColorCode",colorCode);
                intent.putExtra("NoteId",noteId);
                intent.putExtra("Important",important);
                startActivity(intent);
            }
        });

        String finalSubCollection = subCollection;
        deleteNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String plainText="Empty";
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
                try {
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(content, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                //store or send to server
                String ciphertextString = cipherTextIvMac.toString();
                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                        .collection("Trash").document(noteId);
                Map<String,Object> note=new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title",title);
                note.put("Content",ciphertextString);
                note.put("Label",label);
                note.put("Important",important);
                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Moved to Trash", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in moving to Trash", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                        .collection(finalSubCollection).document(noteId);
                docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Deleted from "+finalSubCollection, Toast.LENGTH_SHORT).show();
                        //finish();
                        //startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in deleting from MyNotes", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                startActivity(new Intent(getApplicationContext(),Notes.class));
            }
        });

        archiveNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
                try {
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(content, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                //store or send to server
                String ciphertextString = cipherTextIvMac.toString();
                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                        .collection("Archive").document(noteId);
                Map<String,Object> note=new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title",title);
                note.put("Content",ciphertextString);
                note.put("Label",label);
                note.put("Important",important);
                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Created in Archive", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in Archive", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                        .collection("MyNotes").document(noteId);
                docRef1.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(NoteDetails.this, "Deleted from MyNotes", Toast.LENGTH_SHORT).show();
                        //finish();
                        //startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in deleting from MyNotes", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                startActivity(new Intent(getApplicationContext(),Archive.class));
            }
        });

        restoreNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
                try {
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(content, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                //store or send to server
                String ciphertextString = cipherTextIvMac.toString();
                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                        .collection("MyNotes").document(noteId);
                Map<String, Object> note = new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title", title);
                note.put("Content", ciphertextString);
                note.put("Label", label);
                note.put("Important", important);
                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Restored Note", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in restoring", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                        .collection("Trash").document(noteId);
                docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Deleted from Trash", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in deleting from trash", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                startActivity(new Intent(getApplicationContext(),Notes.class));
            }
        });

        deletePermNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                        .collection("Trash").document(noteId);
                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(getApplicationContext(),Trash.class));
                overridePendingTransition(0, 0);
            }
        });

        unarchiveNoteDetailsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
                try {
                    cipherTextIvMac = AesCbcWithIntegrity.encrypt(content, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                //store or send to server
                String ciphertextString = cipherTextIvMac.toString();
                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                        .collection("MyNotes").document(noteId);
                Map<String,Object> note=new HashMap<>();
                note.put("Date", Timestamp.now());
                note.put("Title",title);
                note.put("Content",ciphertextString);
                note.put("Label", label);
                note.put("Important", important);
                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "Note Unarchived", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Error in unarchiving", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                        .collection("Archive").document(noteId);
                docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NoteDetails.this, "deleted from archive", Toast.LENGTH_SHORT).show();
                        //finish();
                        //startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NoteDetails.this, "Note isn't deleted from archive", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                startActivity(new Intent(getApplicationContext(),Notes.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.note_details_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}