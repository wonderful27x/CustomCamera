package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        findViewById(R.id.tabView).setOnClickListener(this);
        findViewById(R.id.customSnakeBar).setOnClickListener(this);
        findViewById(R.id.cameraHelper).setOnClickListener(this);
        findViewById(R.id.wonderfulCamera).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.tabView:
                intent = new Intent(MainActivity.this,TabViewTestActivity.class);
                startActivity(intent);
                break;
            case R.id.customSnakeBar:
                intent = new Intent(MainActivity.this,CustomSnakeBarTestActivity.class);
                startActivity(intent);
                break;
            case R.id.cameraHelper:
                intent = new Intent(MainActivity.this,CameraHelperTestActivity.class);
                startActivity(intent);
                break;
            case R.id.wonderfulCamera:
                intent = new Intent(MainActivity.this,ControlCameraActivity.class);
                startActivity(intent);
                break;
        }
    }
}