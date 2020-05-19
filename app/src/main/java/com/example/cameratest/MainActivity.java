package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * TabView的测试与使用
 */
public class MainActivity extends AppCompatActivity {

    //TODO Git test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this,Main2Activity.class);
        Intent intentCamera = new Intent(MainActivity.this,CameraActivity.class);
        startActivity(intentCamera);
//        final TabView view = findViewById(R.id.text);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this,"click",Toast.LENGTH_SHORT).show();
////                view.setChecked(!view.isChecked());
//            }
//        });

        LinearLayout linearLayout = findViewById(R.id.parent);
        TabView tabView = new TabView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);

        tabView.setBackgroundColor(Color.WHITE);
        tabView.setResponseMode(1);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.icon_yfh));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.icon_ysh));
        tabView.setTopTitle("哈哈哈");
        tabView.setBottomTitle("XYZ");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));

        tabView.setClickable(true);
//        tabView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TabView view = (TabView) v;
//                view.setChecked(!view.isChecked());
//            }
//        });

        tabView.init();

        linearLayout.removeAllViews();
        linearLayout.addView(tabView);

    }

    private Drawable getDrawableFromSource(int sourceId){
        return getResources().getDrawable(sourceId);
    }
}
