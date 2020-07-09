package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notekeeper.Model.Notes;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Notepad extends AppCompatActivity implements View.OnTouchListener,GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    String color;
    ImageView photo;
    Button setColor;
    MaterialEditText title, note;
    SeekBar priority, importance;
    DatabaseReference reference;
    FirebaseUser fuser;
    FirebaseAuth auth;
    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    GridLayout photoContainer;
    Boolean photoAdded, colorSet,photochanged;
    GestureDetector gestureDetector;
    String currentDate;
    String mUri="",noteid,key="",originalImageURL;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);
        photoAdded = false;
        colorSet = false;
        photochanged=false;
        noteid="empty";

        title = findViewById(R.id.titleText);
        photo = findViewById(R.id.photo);
        note = findViewById(R.id.noteText);
        priority = findViewById(R.id.priorityLevel);
        importance = findViewById(R.id.importanceLevel);
        setColor = findViewById(R.id.setColorButton);
        photoContainer = findViewById(R.id.photoContainer);
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();
        //photo.setOnTouchListener(this);
        gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);
        photo.setOnTouchListener(this);

        try {
                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(fuser.getUid()).child("Notes");
                Intent intent = getIntent();
                noteid = intent.getStringExtra("noteID");
                Log.i("noteID", noteid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notes notes = snapshot.getValue(Notes.class);
                            key=snapshot.getKey();
                            Log.i("keyID", key);
                            assert notes != null;
                            assert noteid != null;
                            if (noteid.equalsIgnoreCase(notes.getnoteid())) {
                                title.setText(notes.getTitle());
                                note.setText(notes.getNote());
                                importance.setProgress(Integer.parseInt(notes.getImportance()));
                                priority.setProgress(Integer.parseInt(notes.getPriority()));
                                setColor.setBackgroundColor(Color.parseColor(notes.getColor()));
                                colorSet=true;
                                color=notes.getColor();
                                currentDate=notes.getDate();
                                originalImageURL=notes.getImageURL();
                                if(!originalImageURL.equalsIgnoreCase("default")){
                                    photoAdded=true;
                                    photo.requestLayout();
                                    Resources r = getResources();
                                    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, r.getDisplayMetrics());
                                    photo.getLayoutParams().height = px;
                                    photo.getLayoutParams().width = photoContainer.getWidth();
                                    Glide.with(getApplicationContext()).load(notes.getImageURL()).into(photo);
                                } else {
                                    photo.setImageResource(R.drawable.add_photoo);
                                }
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }catch(Exception e){
                noteid="empty";
                Log.i("noteID", noteid);
            }
    }

    public void addPhotoClicked(View view) {
        if (noteid.equalsIgnoreCase("empty")) {
            if (photoAdded == false) {
                photoAdded = true;
                openImage();
            } else {
                Toast.makeText(getApplicationContext(), "Long press to change photo\nDouble tap to delete photo", Toast.LENGTH_SHORT).show();
            }
        }else{
            if (photoAdded == false) {
                photoAdded = true;
                photochanged=true;
                openImage();
            } else {
                Toast.makeText(getApplicationContext(), "Long press to change photo\nDouble tap to delete photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveButtonClicked(View view) {
        if (!colorSet) {
            color = "#89D1CFD0";
        }
        if (title.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Title cannot be blank", Toast.LENGTH_SHORT).show();
        } else {

            if (noteid.equalsIgnoreCase("empty")) {
                Calendar calender = Calendar.getInstance();
                currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calender.getTime());
                if (photoAdded) {
                    storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");
                    uploadImage();
                } else {
                    uploadToDB();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                if(photochanged){
                    storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");
                    uploadImage();
                } else {
                    updateDB();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        }
    }

    private void updateDB(){
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("noteid",noteid);
        hashMap.put("title", title.getText().toString());
        hashMap.put("note", note.getText().toString());
        hashMap.put("color", color);
        hashMap.put("priority", Integer.toString(priority.getProgress()));
        hashMap.put("importance", Integer.toString(importance.getProgress()));
        hashMap.put("date",currentDate);
        if(!photochanged) {
            hashMap.put("imageURL", originalImageURL);
        }else{
            hashMap.put("imageURL", mUri);
        }
        reference.child(key).updateChildren(hashMap);

    }

    private void uploadToDB(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        if (photoAdded) {
            hashMap.put("imageURL", mUri);
        }else{
            hashMap.put("imageURL", "default");
        }
        hashMap.put("noteid",UUID.randomUUID().toString());
        hashMap.put("title", title.getText().toString());
        hashMap.put("note", note.getText().toString());
        hashMap.put("color", color);
        hashMap.put("priority", Integer.toString(priority.getProgress()));
        hashMap.put("importance", Integer.toString(importance.getProgress()));
        hashMap.put("date","created on "+currentDate);
        hashMap.put("searchdate",currentDate);

        reference.child("Notes").push().setValue(hashMap);
    }
    public void setColorClicked(View view) {
        colorSet = true;
        Intent i = new Intent(getApplicationContext(), SelectColor.class);
        startActivityForResult(i, 1);
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(UUID.randomUUID().toString()
                    + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                mUri = downloadUri.toString();
                                if (noteid.equalsIgnoreCase("empty")) {
                                    uploadToDB();
                                }else{
                                    updateDB();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1) {
                color = "#" + data.getStringExtra("colorSelected");
                setColor.setBackgroundColor(Color.parseColor(color));
            }
        }
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Upload is in progress", Toast.LENGTH_LONG).show();
            } else {
                photo.requestLayout();
                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, r.getDisplayMetrics());
                photo.getLayoutParams().height = px;
                photo.getLayoutParams().width = photoContainer.getWidth();
                Glide.with(getApplicationContext()).load(imageUri).into(photo);
            }
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
//        photoAdded=false;
        addPhotoClicked(photo);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        photo.setImageResource(R.drawable.add_photoo);
        photoAdded=false;
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        photoAdded = false;
        addPhotoClicked(photo);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

}