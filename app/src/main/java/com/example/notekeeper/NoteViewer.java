package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.notekeeper.Adapter.NotesAdapter;
import com.example.notekeeper.Model.Notes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoteViewer extends AppCompatActivity {

    TextView title,note,date;
    ImageView photo;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    String noteid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_viewer);
        title=findViewById(R.id.titleView);
        date=findViewById(R.id.dateView);
        note=findViewById(R.id.noteView);
        photo=findViewById(R.id.photoView);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("Notes");
        Intent intent2=getIntent();
        noteid=intent2.getStringExtra("noteID");

        try {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notes notes = snapshot.getValue(Notes.class);
                            assert notes != null;
                            assert noteid != null;
                            if (noteid.equalsIgnoreCase(notes.getnoteid())) {
                                title.setText(notes.getTitle());
                                date.setText(notes.getDate());
                                note.setText(notes.getNote());

                                try {
                                    Glide.with(getApplicationContext()).load(notes.getImageURL()).into(photo);
                                } catch (Exception e) {
                                    photo.setVisibility(View.GONE);
                                }
                                break;
                            }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){ }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.edit){
            Intent intent=new Intent(getApplicationContext(),Notepad.class);
            intent.putExtra("noteID",noteid);
            startActivity(intent);
        }else if(item.getItemId()==R.id.delete){

            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.logout)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Notes notes = snapshot.getValue(Notes.class);
                                        assert notes != null;
                                        assert noteid != null;
                                        if (noteid.equalsIgnoreCase(notes.getnoteid())) {
                                            snapshot.getRef().removeValue();
                                            break;
                                        }
                                    }
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("No",null)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }
}
