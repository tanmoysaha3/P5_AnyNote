package com.example.p5_anynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditLabels extends Base {

    LayoutInflater inflater;
    EditText createLabel;
    Button createLabelB;
    RecyclerView labelRecView;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    String LabelName;
    FirestorePagingAdapter<LabelModel, LabelViewHolder> labelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_edit_labels,null,false);
        drawerLayout.addView(contentView,0);

        createLabel=findViewById(R.id.createLabel);
        createLabelB=findViewById(R.id.createLabelB);
        labelRecView=findViewById(R.id.labelsRecView);

        fAuth=FirebaseAuth.getInstance();
        fUser=fAuth.getCurrentUser();
        fStore=FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle("Edit Labels");

        createLabelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String labelName=createLabel.getText().toString();
                if (labelName.isEmpty()){
                    createLabel.setError("Enter a label name");
                    return;
                }

                DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                        .collection("Labels").document(labelName);
                Map<String,Object> label=new HashMap<>();
                label.put("Label_Name",labelName);
                docRef.set(label).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditLabels.this, "New label created", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditLabels.this, "Label wasn't created", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Query cacheLabelQuery=fStore.collection("Notes").document(fUser.getUid())
                .collection("Labels");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<LabelModel> options=new FirestorePagingOptions.Builder<LabelModel>()
                .setLifecycleOwner(this)
                .setQuery(cacheLabelQuery, config, new SnapshotParser<LabelModel>() {
                    @NonNull
                    @Override
                    public LabelModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        LabelModel labelModel=snapshot.toObject(LabelModel.class);
                        LabelName=snapshot.getString("Label_Name");
                        labelModel.setLabel_Name(LabelName);
                        return labelModel;
                    }
                })
                .build();

        labelAdapter=new FirestorePagingAdapter<LabelModel, LabelViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LabelViewHolder holder, int position, @NonNull LabelModel model) {
                holder.labelName.setText(model.getLabel_Name());
                holder.labelDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Query labelNoteQuery=fStore.collection("Notes").document(fUser.getUid())
                                .collection("MyNotes").whereEqualTo("Label",model.getLabel_Name()).limit(1);
                        labelNoteQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                QuerySnapshot querySnapshot=task.getResult();
                                if (querySnapshot.size()>0){
                                    Toast.makeText(EditLabels.this, "Label is attached with one or more notes", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    DocumentReference docRef=fStore.collection("Notes").document(fUser.getUid())
                                            .collection("Labels").document(model.getLabel_Name());
                                    docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditLabels.this, "Label successfully deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditLabels.this, "Error in deleting label", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditLabels.this, "Label wasn't deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public LabelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_labels,parent,false);
                return new LabelViewHolder(view);
            }
        };
        labelRecView.setHasFixedSize(true);
        labelRecView.setLayoutManager(new LinearLayoutManager(this));
        labelRecView.setAdapter(labelAdapter);
    }

    public class LabelViewHolder extends RecyclerView.ViewHolder{
        TextView labelName;
        ImageButton labelDelete;
        public LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            labelName=itemView.findViewById(R.id.labelName);
            labelDelete=itemView.findViewById(R.id.labelDelete);
        }
    }
}