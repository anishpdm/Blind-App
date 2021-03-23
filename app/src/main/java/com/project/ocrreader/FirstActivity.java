package com.project.ocrreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }
    public void vedio(View view)
    {
     Intent i=new Intent(this,MainActivity.class);
     startActivity(i);
    }

    public void pic(View view) {

        Intent i=new Intent(this,pic.class);
        startActivity(i);

    }
}