package com.example.cameratest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * TabView的测试与使用
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检查权限
        List<String> permissionList = permissionCheck();
        if (permissionList.isEmpty()){
            //开启预览
        }else {
            permissionRequest(permissionList,1);
        }

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
        tabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                view.setChecked(!view.isChecked());

                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                Intent intentCamera = new Intent(MainActivity.this,CameraActivity.class);
                Intent intentCameraWonderful = new Intent(MainActivity.this,WonderfulCameraActivity.class);
                startActivity(intentCameraWonderful);
            }
        });

        tabView.init();

        linearLayout.removeAllViews();
        linearLayout.addView(tabView);

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
                        Toast.makeText(MainActivity.this,"对不起，您拒绝了权限无法使用此功能！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //开启预览
            }else {
                Toast.makeText(MainActivity.this,"发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //判断是否有权限
    private boolean checkPermission(String permission){
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED;
    }
}
