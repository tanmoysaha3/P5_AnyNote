package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Notes extends Base {

    LayoutInflater inflater;

    RecyclerView noteListRecView;
    FloatingActionButton addNoteFAB;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String NoteTitle;
    FirestorePagingAdapter<NoteModel,NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_notes,null,false);
        drawerLayout.addView(contentView,0);

        noteListRecView=findViewById(R.id.notesRecView);
        addNoteFAB=findViewById(R.id.addNoteFAB);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fUser=fAuth.getCurrentUser();

        getSupportActionBar().setTitle("Notes");

        Query noteQuery=fStore.collection("Notes").document(fUser.getUid())
                .collection("MyNotes");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<NoteModel> options=new FirestorePagingOptions.Builder<NoteModel>()
                .setLifecycleOwner(this)
                .setQuery(noteQuery, config, new SnapshotParser<NoteModel>() {
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
                SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss, dd-MM-YYYY");
                String dateString=format.format(myDateTime);
                holder.dateTimeNote.setText(dateString);

                holder.titleNote.setText(model.getTitle());
                holder.contentNote.setText(model.getContent());
                holder.labelNote.setText(model.getLabel());
                Integer colorCode=getRandomColor();
                holder.noteCard.setCardBackgroundColor(holder.view.getResources().getColor(colorCode,null));
                if (model.getImportant().equals("1")){
                    holder.importantB.setVisibility(View.VISIBLE);
                }
                else {
                    holder.notImportantB.setVisibility(View.VISIBLE);
                }
                DocumentSnapshot snapshot=getItem(position);
                String noteId=snapshot.getId();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(v.getContext(), NoteDetails.class);
                        intent.putExtra("Title",model.getTitle());
                        intent.putExtra("Content",model.getContent());
                        intent.putExtra("Label",model.getLabel());
                        intent.putExtra("ColorCode",colorCode);
                        intent.putExtra("NoteId",noteId);
                        intent.putExtra("PageName","Main");
                        v.getContext().startActivity(intent);
                    }
                });

                holder.labelNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),NotesAsLabel.class);
                        intent.putExtra("Label",model.getLabel());
                        startActivity(intent);
                    }
                });

                holder.importantB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                                .collection("MyNotes").document(noteId);
                        Map<String,Object> note=new HashMap<>();
                        note.put("Important","0");
                        docRef.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Notes.this, "Note no longer marked as important", Toast.LENGTH_SHORT).show();
                                holder.importantB.setVisibility(View.INVISIBLE);
                                holder.notImportantB.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Notes.this, "Important mark couldn't be removed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                holder.notImportantB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                                .collection("MyNotes").document(noteId);
                        Map<String,Object> note=new HashMap<>();
                        note.put("Important","1");
                        docRef.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Notes.this, "Note marked as important", Toast.LENGTH_SHORT).show();
                                holder.importantB.setVisibility(View.VISIBLE);
                                holder.notImportantB.setVisibility(View.INVISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Notes.this, "Note couldn't be marked as important", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                ImageView menuIcon=holder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent intent=new Intent(getApplicationContext(), EditNote.class);
                                intent.putExtra("Title",model.getTitle());
                                intent.putExtra("Content", model.getContent());
                                intent.putExtra("Label",model.getLabel());
                                intent.putExtra("ColorCode",colorCode);
                                intent.putExtra("NoteId",noteId);
                                intent.putExtra("Important",model.getImportant());
                                startActivity(intent);
                                return false;
                            }
                        });

                        popupMenu.getMenu().add("Archive").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fStore.collection("Notes").document(fUser.getUid())
                                        .collection("Archive").document(noteId);
                                Map<String,Object> note=new HashMap<>();
                                note.put("Date", Timestamp.now());
                                note.put("Title",model.getTitle());
                                note.put("Content",model.getContent());
                                note.put("Label",model.getLabel());
                                note.put("Important",model.getImportant());
                                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Notes.this, "Created in Archive", Toast.LENGTH_SHORT).show();
                                        DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                                                .collection("MyNotes").document(noteId);
                                        docRef1.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(Notes.this, "Deleted from MyNotes", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), Notes.class));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Notes.this, "Error in deleting from MyNotes", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Notes.this, "Error in Archive", Toast.LENGTH_SHORT).show();
                                    }
                                });
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
                                note.put("Title",model.getTitle());
                                note.put("Content",model.getContent());
                                note.put("Label",model.getLabel());
                                note.put("Important",model.getImportant());
                                docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Notes.this, "Moved to Trash", Toast.LENGTH_SHORT).show();
                                        DocumentReference docRef1 = fStore.collection("Notes").document(fUser.getUid())
                                                .collection("MyNotes").document(noteId);
                                        docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(Notes.this, "Deleted from MyNotes", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(getIntent());
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Notes.this, "Error in deleting from MyNotes", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Notes.this, "Error in moving to Trash", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        //Toast.makeText(Notes.this, "Menu Icon Clicked", Toast.LENGTH_SHORT).show();
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

        noteListRecView.setHasFixedSize(true);
        noteListRecView.setLayoutManager(new LinearLayoutManager(this));
        noteListRecView.setAdapter(noteAdapter);

        addNoteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NewNote.class));
            }
        });
    }

    private class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeNote, titleNote, contentNote, labelNote;
        View view;
        CardView noteCard;
        Button importantB, notImportantB;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeNote=itemView.findViewById(R.id.dateTimeNote);
            titleNote=itemView.findViewById(R.id.titleNote);
            contentNote=itemView.findViewById(R.id.contentNote);
            labelNote=itemView.findViewById(R.id.labelNote);
            noteCard=itemView.findViewById(R.id.noteCard);
            importantB=itemView.findViewById(R.id.importantB);
            notImportantB=itemView.findViewById(R.id.notImportantB);
            view=itemView;
        }
    }

    private Integer getRandomColor() {
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