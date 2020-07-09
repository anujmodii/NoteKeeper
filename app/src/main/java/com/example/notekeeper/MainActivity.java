package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.notekeeper.Adapter.NotesAdapter;
import com.example.notekeeper.Model.Notes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private List<Notes> mNotes;
    MaterialEditText search_notes;
    FirebaseUser fuser;
    FirebaseAuth auth;
    Button sortBy,gridView;
    Boolean gridLayout;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_notes = findViewById(R.id.searchTitle);
        sortBy=findViewById(R.id.sortByButton);
        gridView=findViewById(R.id.layoutTypeSwitch);

        recyclerView=findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mNotes = new ArrayList<>();
        sp=getSharedPreferences("layouttype",MODE_PRIVATE);
        gridLayout=sp.getBoolean("layout", Boolean.parseBoolean("true"));
        layoutChange(gridView);
//        readUsers();
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();

        search_notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence!=null){
                    searchNotes(charSequence.toString().toLowerCase());
                }else{
                    readUsers();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private  void  searchNotes(final  String s){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("Notes")
                .orderByChild("title").startAt(s).endAt(s+"\uf8ff");
        try {
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mNotes.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notes note = snapshot.getValue(Notes.class);
                        assert note != null;
                        assert firebaseUser != null;
                            mNotes.add(note);

                    }
                    notesAdapter = new NotesAdapter(getApplicationContext(), mNotes,gridLayout);
                    recyclerView.setAdapter(notesAdapter);
                    Log.i("Notes:",mNotes.toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.addNote){
            Intent intent=new Intent(getApplicationContext(),Notepad.class);
            startActivity(intent);
        }else if(item.getItemId()==R.id.logout){

            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.logout)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent=new Intent(getApplicationContext(),StartActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No",null)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void readUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("Notes");
        try {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mNotes.clear();
                    if (search_notes.getText().toString().equals("")) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notes note = snapshot.getValue(Notes.class);
                            assert note != null;
                            assert firebaseUser != null;
                            mNotes.add(note);

                        }
                        Log.i("Notes:",mNotes.toString());
                        notesAdapter = new NotesAdapter(getApplicationContext(), mNotes, gridLayout);
                        recyclerView.setAdapter(notesAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){

        }
    }

    public void sortBy(View view){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("Notes");
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.sort)
                .setTitle("Sort By")
                .setPositiveButton("Priority", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query query = reference.orderByChild("priority");
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mNotes.clear();
                                if (search_notes.getText().toString().equals("")) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Notes note = snapshot.getValue(Notes.class);
                                        assert note != null;
                                        assert firebaseUser != null;
                                        mNotes.add(note);
                                    }
                                    notesAdapter = new NotesAdapter(getApplicationContext(), mNotes,gridLayout);
                                    recyclerView.setAdapter(notesAdapter);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }})
                .setNegativeButton("Importance", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query query = reference.orderByChild("importance");
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mNotes.clear();
                                if (search_notes.getText().toString().equals("")) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Notes note = snapshot.getValue(Notes.class);
                                        assert note != null;
                                        assert firebaseUser != null;
                                        mNotes.add(note);
                                    }
                                    notesAdapter = new NotesAdapter(getApplicationContext(), mNotes,gridLayout);
                                    recyclerView.setAdapter(notesAdapter);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .setNeutralButton("Date", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query query = reference.orderByChild("searchdate");
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mNotes.clear();
                                if (search_notes.getText().toString().equals("")) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Notes note = snapshot.getValue(Notes.class);
                                        assert note != null;
                                        assert firebaseUser != null;
                                        mNotes.add(note);
                                    }
                                    notesAdapter = new NotesAdapter(getApplicationContext(), mNotes,gridLayout);
                                    recyclerView.setAdapter(notesAdapter);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                })
                .show();
    }

    public void layoutChange(View View){
        SharedPreferences.Editor editor=sp.edit();

        if(gridLayout) {
            gridLayout = false;
            editor.putBoolean("layout",true);
        }else{
            gridLayout=true;
            editor.putBoolean("layout",false);
        }
        editor.commit();
        recyclerView.setLayoutManager(gridLayout ? new LinearLayoutManager(this) : new GridLayoutManager(this, 2));
        readUsers();
    }

}