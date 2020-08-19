package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameratest.core.ScanResult;

/**
 * 拍照/扫码结果显示Activity
 */
public class PictureResultActivity extends AppCompatActivity {

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
        ScanResult scanResult = (ScanResult) intent.getSerializableExtra("scanResult");

        if (scanResult != null){
            format.setText("格式： " + scanResult.format);
            type.setText("类型： " + scanResult.type);
            time.setText("时间： " + scanResult.dateTime);
            metadata.setText("元数据： " + scanResult.metadata);
            content.setText(scanResult.content);
        }

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
        bitmapArray = null;
    }
}
