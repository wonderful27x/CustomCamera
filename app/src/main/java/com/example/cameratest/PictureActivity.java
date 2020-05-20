package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class PictureActivity extends AppCompatActivity {

    public static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ImageView imageView = findViewById(R.id.picture);

        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 将byte类型的原始数据转换成bitmap
     */
    private Bitmap byteToBitmap(byte[] data){
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
    }
}
