package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.example.p5_anynote.model.NoteModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.preference.PowerPreference;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKeyFromPassword;

public class Archive extends Base {

    LayoutInflater inflater;
    RecyclerView archiveRecView;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    String NoteTitle;
    FirestorePagingAdapter<NoteModel, NoteViewHolder> noteAdapter;
    AesCbcWithIntegrity.SecretKeys keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_archive,null,false);
        drawerLayout.addView(contentView,0);

        PowerPreference.init(this);
        String sortField = PowerPreference.getDefaultFile().getString("AnyNoteSortFieldArchive", "Date");
        String sortOrder = PowerPreference.getDefaultFile().getString("AnyNoteSortOrderArchive","Descending");

        Query.Direction order;
        if (sortOrder=="Descending"){
            order = Query.Direction.DESCENDING;
        }
        else {
            order = Query.Direction.ASCENDING;
        }

        archiveRecView=findViewById(R.id.archiveRecView);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle("Archive");

        Query.Direction DESCENDING = Query.Direction.DESCENDING;

        Query cacheDataQuery = fStore.collection("Notes").document(fUser.getUid())
                .collection("Archive").orderBy("Date", DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        //Recycler Option
        FirestorePagingOptions<NoteModel> options =new FirestorePagingOptions.Builder<NoteModel>()
                .setLifecycleOwner(this)
                .setQuery(cacheDataQuery, config, new SnapshotParser<NoteModel>() {
                    @NonNull
                    @Override
                    public NoteModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        NoteModel noteModel=snapshot.toObject(NoteModel.class);
                        NoteTitle=snapshot.getString("Title");
                        noteModel.setTitle(NoteTitle);
                        return noteModel;
                    }
                })
                .build();

        noteAdapter=new FirestorePagingAdapter<NoteModel, NoteViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull NoteModel model) {
                Date myDateTime =model.getDate().toDate();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss, dd-MM-YYYY");
                String dateString = format.format(myDateTime);
                holder.dateTimeNote.setText(dateString);

                holder.titleNote.setText(model.getTitle());
                String pass = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionPass");
                String salt = PowerPreference.getDefaultFile().getString("AnyNoteEncryptionSalt");
                String plainText="Empty";
                try {
                    keys = generateKeyFromPassword(pass, salt);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(model.getContent());
                try {
                    plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                holder.contentNote.setText(plainText);
                Integer colorCode=getRandomColor();
                holder.noteCard.setCardBackgroundColor(holder.view.getResources().getColor(colorCode,null));
                DocumentSnapshot snapshot=getItem(position);
                String noteId=snapshot.getId();

                String finalPlainText = plainText;
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),NoteDetails.class);
                        intent.putExtra("Title",model.getTitle());
                        intent.putExtra("Content", finalPlainText);
                        intent.putExtra("ColorCode",colorCode);
                        intent.putExtra("Label",model.getLabel());
                        intent.putExtra("NoteId",noteId);
                        intent.putExtra("Important",model.getImportant());
                        intent.putExtra("PageName","Archive");
                        startActivity(intent);
                    }
                });

                holder.importantB.setVisibility(View.GONE);
                holder.notImportantB.setVisibility(View.GONE);

                ImageView menuIcon=holder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);

                        popupMenu.getMenu().add("Unarchive").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                                        .collection("MyNotes").document(noteId);
                                Map<String,Object> note=new HashMap<>();
                                note.put("Date", Timestamp.now());
                                note.put("Title",model.getTitle());
                                note.put("Content",model.getContent());
                                note.put("Label",model.getLabel());
                                note.put("Important",model.getImportant());
                                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Archive.this, "Note Unarchived", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Archive.this, "Error in unarchiving", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                                        .collection("Archive").document(noteId);
                                docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Archive.this, "deleted from archive", Toast.LENGTH_SHORT).show();
                                        //finish();
                                        //startActivity(getIntent());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Archive.this, "Note isn't deleted from archive", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                finish();
                                startActivity(getIntent());
                                return false;
                            }
                        });

                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                                        .collection("Trash").document(noteId);
                                Map<String,Object> note=new HashMap<>();
                                note.put("Date", Timestamp.now());
                                note.put("Title", model.getTitle());
                                note.put("Content", model.getContent());
                                note.put("Label",model.getLabel());
                                note.put("Important",model.getImportant());
                                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Archive.this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Archive.this, "Error in moving to trash", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                                        .collection("Archive").document(noteId);
                                docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Archive.this, "Deleted from archive", Toast.LENGTH_SHORT).show();
                                        //finish();
                                        //startActivity(getIntent());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Archive.this, "Error in deleting from archive", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                finish();
                                startActivity(getIntent());
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notes,parent,false);
                return new NoteViewHolder(view);
            }
        };
        archiveRecView.setHasFixedSize(true);
        archiveRecView.setLayoutManager(new LinearLayoutManager(this));
        archiveRecView.setAdapter(noteAdapter);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeNote, titleNote, contentNote;
        View view;
        CardView noteCard;
        Button importantB, notImportantB;
        public NoteViewHolder(View itemView) {
            super(itemView);
            dateTimeNote = itemView.findViewById(R.id.dateTimeNote);
            titleNote = itemView.findViewById(R.id.titleNote);
            contentNote = itemView.findViewById(R.id.contentNote);
            noteCard = itemView.findViewById(R.id.noteCard);
            importantB=itemView.findViewById(R.id.importantB);
            notImportantB=itemView.findViewById(R.id.notImportantB);
            view = itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.archive_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.latestArchiveM){
            PowerPreference.getDefaultFile().putString("AnyNoteSortFieldArchive","Date");
            PowerPreference.getDefaultFile().putString("AnyNoteSortOrderArchive","Descending");
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
        else if (item.getItemId()==R.id.oldestArchiveM){
            PowerPreference.getDefaultFile().putString("AnyNoteSortFieldArchive","Date");
            PowerPreference.getDefaultFile().putString("AnyNoteSortOrderArchive","Ascending");
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
        else if (item.getItemId()==R.id.azArchiveM){
            PowerPreference.getDefaultFile().putString("AnyNoteSortFieldArchive","Title");
            PowerPreference.getDefaultFile().putString("AnyNoteSortOrderArchive","Ascending");
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
        else if (item.getItemId()==R.id.zaArchiveM){
            PowerPreference.getDefaultFile().putString("AnyNoteSortFieldArchive","Title");
            PowerPreference.getDefaultFile().putString("AnyNoteSortOrderArchive","Descending");
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    private int getRandomColor() {
        List<Integer> colorCode=new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notgreen);
        Random randomColor=new Random();
        int number=randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }
}