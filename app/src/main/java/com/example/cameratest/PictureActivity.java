package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureActivity extends AppCompatActivity {

    public static Bitmap bitmap;
    public static byte[] bitmapArray;

    private ImageView imageView;
    private TextView format;
    private TextView type;
    private TextView time;
    private TextView metadata;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        imageView = findViewById(R.id.picture);
        format = findViewById(R.id.format);
        type = findViewById(R.id.type);
        time = findViewById(R.id.time);
        metadata = findViewById(R.id.metadata);
        content = findViewById(R.id.content);

        Intent intent = getIntent();
        String formatStr = intent.getStringExtra("format");
        String typeStr = intent.getStringExtra("type");
        String timeStr = intent.getStringExtra("time");
        String metadataStr = intent.getStringExtra("metadata");
        String contentStr = intent.getStringExtra("content");

        format.setText(formatStr);
        type.setText(typeStr);
        time.setText(timeStr);
        metadata.setText(metadataStr);
        content.setText(contentStr);

        if (bitmapArray != null){
            bitmap = byteToBitmap(bitmapArray);
        }

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
