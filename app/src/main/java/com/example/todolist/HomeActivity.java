package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.DragStartHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mrecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    private Toolbar toolbar;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserId;
    private ProgressDialog loader;

    private String key = "";
    private String task;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        loader = new ProgressDialog(this);


        toolbar = findViewById(R.id.homeToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("To Do List");
        mAuth = FirebaseAuth.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mrecyclerView.setHasFixedSize(true);
        mrecyclerView.setLayoutManager(linearLayoutManager);

        mUser = mAuth.getCurrentUser();
        onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserId);


        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);


        View view = layoutInflater.inflate(R.layout.input_file, null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = view.findViewById(R.id.task);
        final EditText description = view.findViewById(R.id.desc);
        Button save = view.findViewById(R.id.saveBtn);
        Button cancel = view.findViewById(R.id.cancelBtn);

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            String mTask = task.getText().toString().trim();
            String mDescription = description.getText().toString().trim();
            String id = reference.push().getKey();
            String date = DateFormat.getDateInstance().format(new Date());

            if (TextUtils.isEmpty(mTask)) {
                task.setError("Task Required");
                return;
            }
            if (TextUtils.isEmpty(mDescription)) {
                description.setError("Description Required");
                return;
            } else {
                loader.setMessage("Adding Your Task");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                Model model = new Model(mTask,mDescription,id,date);
                reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task Has Been Added Successfully", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }else {
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed " + error, Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference,Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, RecyclerViewHolder> adapter = new FirebaseRecyclerAdapter<Model, RecyclerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int i, @NonNull Model model) {
                recyclerViewHolder.setDate(model.getDate());
                recyclerViewHolder.setTask(model.getTask());
                recyclerViewHolder.setDescription(model.getDescription());

                recyclerViewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(i).getKey();
                        task = model.getTask();
                        description = model.getDescription();


                        updateTask();

                    }
                });
            }

            @NonNull
            @Override
            public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.return_layout, parent, false);
                return new RecyclerViewHolder(view);
            }
        };

        mrecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText mTask = view.findViewById(R.id.updateTask);
        EditText mDescription = view.findViewById(R.id.updateDesc);

        mTask.setText(task);
        mTask.setSelection(task.length());
        mDescription.setText(description);
        mDescription.setSelection(description.length());

        Button delButton = view.findViewById(R.id.deleteBtn);
        Button updateButton = view.findViewById(R.id.updateBtn);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = mTask.getText().toString().trim();
                description = mDescription.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());

                Model model = new Model(task,description,key,date);

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task Has Been Updated Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Update Failed" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();

            }
        });


        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed To Delete Task" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOut:
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}