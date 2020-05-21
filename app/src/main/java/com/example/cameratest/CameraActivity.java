package com.example.cameratest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;
    private SurfaceView surfaceView;
    private TabView takePic;
    private ImageView rect;
    private CustomSnakeBar<TabView> snakeBar;
    private int choosePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.surfaceView);
        takePic = findViewById(R.id.takePic);
        rect = findViewById(R.id.rect);

        initSnake();

        //初始化相机
        cameraHelper = new CameraHelper(this);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.takePicture();
            }
        });

        //检查权限
        List<String> permissionList = permissionCheck();
        if (permissionList.isEmpty()){
            //开启预览
            cameraHelper.setHolder(surfaceView.getHolder());
//            cameraHelper.startPreview();
        }else {
            permissionRequest(permissionList,1);
        }

    }

    //中间按钮使用TabView，左右按钮使用默认
    private void initSnake(){
        snakeBar = findViewById(R.id.snake);

        List<TabView> views = new ArrayList<>();

        TabView tabView0 = new TabView(this);
        tabView0.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView0.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView0.setTopTitle("扫描");
        tabView0.setBottomTitle("扫描");
        tabView0.setTopTitleColor(Color.parseColor("#333333"));
        tabView0.setBottomTitleColor(Color.parseColor("#aaaaaa"));

        TabView tabView1 = new TabView(this);
        tabView1.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView1.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView1.setTopTitle("拍照");
        tabView1.setBottomTitle("拍照");
        tabView1.setTopTitleColor(Color.parseColor("#333333"));
        tabView1.setBottomTitleColor(Color.parseColor("#aaaaaa"));

        views.add(tabView0);
        views.add(tabView1);

        snakeBar.addChildren(views);

//        snakeBar.enableLeftButton(null);
//        snakeBar.enableRightButton(null);

        snakeBar.init();

        snakeBar.setOnItemClickListener(new CustomSnakeBar.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(CameraActivity.this,"click: " + position,Toast.LENGTH_SHORT).show();
                choosePosition = position;
                if (choosePosition == 1){
                    rect.setVisibility(View.GONE);
                    takePic.setVisibility(View.VISIBLE);
                }else {
                    rect.setVisibility(View.VISIBLE);
                    takePic.setVisibility(View.GONE);
                }
            }
        });

        snakeBar.setLeftClickListener(new CustomSnakeBar.LeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                Toast.makeText(CameraActivity.this,"click: left",Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setRightClickListener(new CustomSnakeBar.RightClickListener() {
            @Override
            public void onRightClick(View view) {
                Toast.makeText(CameraActivity.this,"click: right",Toast.LENGTH_SHORT).show();
            }
        });

        snakeBar.setChoosePosition(0);
    }

    private Drawable getDrawableFromSource(int sourceId){
        return getResources().getDrawable(sourceId);
    }

    //判断是否授权所有权限
    private List<String> permissionCheck(){
        List<String> permissions = new ArrayList<>();
        if (!checkPermission(Manifest.permission.CAMERA)){
            permissions.add(Manifest.permission.CAMERA);
        }
        return permissions;
    }

    //发起权限申请
    private void permissionRequest(List<String> permissions,int requestCode){
        String[] permissionArray = permissions.toArray(new String[permissions.size()]);
        ActivityCompat.requestPermissions(this,permissionArray,requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            if (grantResults.length >0){
                for (int result:grantResults){
                    if (result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(CameraActivity.this,"对不起，您拒绝了权限无法使用此功能！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //开启预览
                cameraHelper.setHolder(surfaceView.getHolder());
//                cameraHelper.startPreview();
            }else {
                Toast.makeText(CameraActivity.this,"发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //判断是否有权限
    private boolean checkPermission(String permission){
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED;
    }
}
