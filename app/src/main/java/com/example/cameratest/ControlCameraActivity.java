package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ControlCameraActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        findViewById(R.id.tesA).setOnClickListener(this);
        findViewById(R.id.tesB).setOnClickListener(this);
        findViewById(R.id.tesC).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(ControlCameraActivity.this,WonderfulCameraActivity.class);
        switch (v.getId()){
            case R.id.tesA:
                intent.putExtra("testWhich","A");
                startActivity(intent);
                break;
            case R.id.tesB:
                intent.putExtra("testWhich","B");
                startActivity(intent);
                break;
            case R.id.tesC:
                intent.putExtra("testWhich","C");
                startActivity(intent);
                break;
        }
    }
}