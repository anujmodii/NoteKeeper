package com.example.notekeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SelectColor extends AppCompatActivity {

    public void colorSelected(View view){
        String color = view.getTag().toString();
        Intent i= new Intent();
        i.putExtra("colorSelected",color);
        setResult(1,i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_color);
    }
}
