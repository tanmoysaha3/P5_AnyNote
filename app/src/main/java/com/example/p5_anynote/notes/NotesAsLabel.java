package com.example.p5_anynote.notes;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p5_anynote.R;
import com.example.p5_anynote.model.NoteModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NotesAsLabel extends Base{

    LayoutInflater inflater;

    RecyclerView notesAsLabelRecView;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    String NoteTitle;
    FirestorePagingAdapter<NoteModel,NoteViewHolder> noteAdapter;
    Spinner labelNALS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_notes_as_label,null,false);
        drawerLayout.addView(contentView,0);

        Intent data=getIntent();
        String label=data.getStringExtra("Label");

        notesAsLabelRecView=findViewById(R.id.notesAsLabelRecView);
        labelNALS=findViewById(R.id.labelNALS);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle(label+"Notes");
        //getSupportActionBar().setTitle(label.concat("Notes"));
        //getSupportActionBar().setTitle(new StringBuilder(label).append("Notes").toString());

        labelNALS.setVisibility(View.VISIBLE);

        CollectionReference labelsRef=fStore.collection("Labels");
        List<String> labels=new ArrayList<>();
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelNALS.setAdapter(adapter);

        labelsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    labels.add("ALL");
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        String label=documentSnapshot.getString("Label_Name");
                        labels.add(label);
                    }
                    adapter.notifyDataSetChanged();
                    labelNALS.setSelection(adapter.getPosition(label));
                }
            }
        });

        labelNALS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!label.equals(labelNALS.getSelectedItem().toString())){
                    Intent intent=new Intent(getApplicationContext(),NotesAsLabel.class);
                    intent.putExtra("Label",labelNALS.getSelectedItem().toString());
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Query query=fStore.collection("Notes").document(fUser.getUid())
                .collection("MyNotes").whereEqualTo("Label",label);


        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<NoteModel> options=new FirestorePagingOptions.Builder<NoteModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<NoteModel>() {
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
                        intent.putExtra("PageName","NotesAsLabel");
                        v.getContext().startActivity(intent);
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
                                Toast.makeText(NotesAsLabel.this, "Note no longer marked as important", Toast.LENGTH_SHORT).show();
                                holder.importantB.setVisibility(View.INVISIBLE);
                                holder.notImportantB.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NotesAsLabel.this, "Important mark couldn't be removed", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(NotesAsLabel.this, "Note marked as important", Toast.LENGTH_SHORT).show();
                                holder.importantB.setVisibility(View.VISIBLE);
                                holder.notImportantB.setVisibility(View.INVISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NotesAsLabel.this, "Note couldn't be marked as important", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                ImageView menuIcon=holder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(NotesAsLabel.this, "Menu Icon Clicked", Toast.LENGTH_SHORT).show();
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

        notesAsLabelRecView.setHasFixedSize(true);
        notesAsLabelRecView.setLayoutManager(new LinearLayoutManager(this));
        notesAsLabelRecView.setAdapter(noteAdapter);
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