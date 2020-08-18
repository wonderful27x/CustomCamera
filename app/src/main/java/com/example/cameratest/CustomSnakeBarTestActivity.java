package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.cameratest.core.CustomSnakeBar;
import com.example.cameratest.core.TabView;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomSnakeBar的测试与使用范例
 */
public class CustomSnakeBarTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        test1();
//        test2();
        test3();
    }

    //简单模式，全部默认
    private void test1(){
        CustomSnakeBar<String> snakeBar = findViewById(R.id.snake);
        //中间按钮
        List<String> views = new ArrayList<>();
        views.add("abc");
        views.add("abc");
        views.add("abc");
        views.add("abc");
        views.add("中间");
        snakeBar.addChildren(views);
        //边缘按钮
        snakeBar.enableLeftButton("left");
        snakeBar.enableRightButton("right");
        //初始化
        snakeBar.init();

        //设置监听
        snakeBar.setOnItemClickListener(new CustomSnakeBar.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: " + position,Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setLeftClickListener(new CustomSnakeBar.LeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: left",Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setRightClickListener(new CustomSnakeBar.RightClickListener() {
            @Override
            public void onRightClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: right",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //中间按钮String，左按钮使用TabView
    private void test2(){
        CustomSnakeBar<String> snakeBar = findViewById(R.id.snake);

        List<String> views = new ArrayList<>();
        views.add("abc");
        views.add("abc");
        views.add("abc");
        views.add("abc");
        views.add("中间");
        snakeBar.addChildren(views);

        TabView tabView = new TabView(this);
        tabView.setResponseMode(1);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("left");
        tabView.setBottomTitle("left");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));

        snakeBar.enableLeftButton(tabView);
        snakeBar.enableRightButton("right");

        snakeBar.init();

        snakeBar.setOnItemClickListener(new CustomSnakeBar.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: " + position,Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setLeftClickListener(new CustomSnakeBar.LeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: left",Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setRightClickListener(new CustomSnakeBar.RightClickListener() {
            @Override
            public void onRightClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: right",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //中间按钮使用TabView，左右按钮使用默认
    private void test3(){
        CustomSnakeBar<TabView> snakeBar = findViewById(R.id.snake);

        List<TabView> views = new ArrayList<>();
        for (int i=0; i<2; i++){
            TabView tabView = new TabView(this);
            tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
            tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
            tabView.setTopTitle("德芙");
            tabView.setBottomTitle("德芙");
            tabView.setTopTitleColor(Color.parseColor("#333333"));
            tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
            tabView.setBackgroundColor(Color.GREEN);
            views.add(tabView);
        }

        TabView tabView = new TabView(this);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("德芙abcdef");
        tabView.setBottomTitle("德芙abcdef");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.setBackgroundColor(Color.GREEN);
        views.add(tabView);

        snakeBar.addChildren(views);

        snakeBar.enableLeftButton(null);
        snakeBar.enableRightButton(null);

        snakeBar.init();

        snakeBar.setOnItemClickListener(new CustomSnakeBar.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: " + position,Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setLeftClickListener(new CustomSnakeBar.LeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: left",Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setRightClickListener(new CustomSnakeBar.RightClickListener() {
            @Override
            public void onRightClick(View view) {
                Toast.makeText(CustomSnakeBarTestActivity.this,"click: right",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private Drawable getDrawableFromSource(int sourceId){
        return getResources().getDrawable(sourceId);
    }
}
