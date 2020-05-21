package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class WonderfulCameraActivity extends AppCompatActivity {

    private WonderfulCamera wonderfulCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wonderful_camere);
        wonderfulCamera = findViewById(R.id.cameraView);
        initCameraView();
    }

    private void initCameraView(){
        wonderfulCamera.init();
    }
}
