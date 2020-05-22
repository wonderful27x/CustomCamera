package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.List;

public class WonderfulCameraActivity extends AppCompatActivity implements CameraInterface{

    private static final String TAG = "WonderfulCameraActivity";

    private WonderfulCamera wonderfulCamera;

    private CameraDataHandle cameraDataHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wonderful_camere);
        cameraDataHandle = new CameraDataHandle();
        testA();
//        testB();
//        testC();
    }

    /**
     * 测试A，使用默认设置，最简单的使用
     */
    private void testA(){
        wonderfulCamera = findViewById(R.id.cameraView);

        //设置大小发生改变的监听，可获取控件的size，默认只要大小改变就会回调
        //一般情况下会回调两次，第二次才是准确的
        //通过设置wonderfulCamera.keepValueSize = false，可截断回调
        wonderfulCamera.setSizeChangedListener(new WonderfulCamera.SizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                Log.d(TAG,"width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this,"width: " + width + " - height: " + height,Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG,"onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this,"onLeftClick - back",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG,"onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this,"onRightClick - album",Toast.LENGTH_SHORT).show();
            }
        });

        ////相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraModeSelectListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, int position, String message) {
                Log.d(TAG,"onModeSelect -> position: " + position + " mode: " + message);
                Toast.makeText(WonderfulCameraActivity.this,"onModeSelect -> position: " + position + " mode: " + message,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(int position) {
                Log.d(TAG,"center -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"center -> mode: " + position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(int position) {
                Log.d(TAG,"bottom -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"bottom -> mode: " + position,Toast.LENGTH_SHORT).show();
            }
        });


        //设置数据监听接口
        wonderfulCamera.setCameraInterface(this);

        //初始化
        wonderfulCamera.init();

    }


    //如默认设置无法满足需要可以修改默认熟悉
    //例如当前只支持拍照和扫一扫
    //需求1：将扫一扫修改为扫码，并替换图标
    //需求2：想多增加一个识别按钮
    private void testB(){
        wonderfulCamera = findViewById(R.id.cameraView);

        //设置大小发生改变的监听，可获取控件的size，默认只要大小改变就会回调
        //一般情况下会回调两次，第二次才是准确的
        //通过设置wonderfulCamera.keepValueSize = false，可截断回调
        wonderfulCamera.setSizeChangedListener(new WonderfulCamera.SizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                //截断测试
                wonderfulCamera.keepValueSize = false;
                Log.d(TAG,"width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this,"width: " + width + " - height: " + height,Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG,"onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this,"onLeftClick - back",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG,"onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this,"onRightClick - album",Toast.LENGTH_SHORT).show();
            }
        });

        ////相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraModeSelectListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, int position, String message) {
                Log.d(TAG,"onModeSelect -> position: " + position + " mode: " + message);
                Toast.makeText(WonderfulCameraActivity.this,"onModeSelect -> position: " + position + " mode: " + message,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(int position) {
                Log.d(TAG,"center -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"center -> mode: " + position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(int position) {
                Log.d(TAG,"bottom -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"bottom -> mode: " + position,Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * 修改默认设置
         */
        //获取元件仓库集合
        List<WonderfulCamera.ComponentDepot> componentDepotList = wonderfulCamera.forceRequestComponentDepot();
        //获取扫一扫元件仓库
        WonderfulCamera.ComponentDepot scan = componentDepotList.get(1);
        //获取导航按钮并修改为扫码，并替换图标
        TabView tabView = (TabView) scan.targetTypeView;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setTopTitle("扫码");
        tabView.setBottomTitle("扫码");
        tabView.init();

        /**
         * 添加自己的识别按钮
         */
        //先构建一个元件仓库
        WonderfulCamera.ComponentDepot depot = wonderfulCamera.createComponentDepot();
        //添加自己的按钮属性
        //导航栏按钮
        tabView = (TabView) depot.targetTypeView;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("识别");
        tabView.setBottomTitle("识别");
        tabView.init();
        //中心shape
        tabView = (TabView) depot.centerShape;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.sel_nor));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.sel_nor));
        tabView.init();
        //中心按钮
        tabView = (TabView) depot.centerButton;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("打开手电");
        tabView.setBottomTitle("关闭手电");
        tabView.init();
        //底部按钮
        tabView = (TabView) depot.bottomButton;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("点击识别");
        tabView.setBottomTitle("点击识别");
        tabView.init();

        //添加到集合中
        componentDepotList.add(depot);

        //可以指定选择导航按钮的位置，默认为bottom
        wonderfulCamera.setSnakePosition("top");

        //修改导航按钮的间隔
        wonderfulCamera.setSnakeHorizontalGap(10);

        //初始化
        wonderfulCamera.init();

        //设置当前选中的模式，默认为0
        wonderfulCamera.setCurrentMode(1);
    }

    /**
     * 如果通过修改增加默认属性仍然无法满足需求，那么使用自定义View
     */
    private void testC(){
        wonderfulCamera = findViewById(R.id.cameraView);

        //构建一个CoordinateView，用于设定自己的自定义view
        final WonderfulCamera.CoordinateView coordinateView = wonderfulCamera.createCoordinateView();;

        //设置大小发生改变的监听，可获取控件的size，默认只要大小改变就会回调
        //一般情况下会回调两次，第二次才是准确的
        //通过设置wonderfulCamera.keepValueSize = false，可截断回调
        wonderfulCamera.setSizeChangedListener(new WonderfulCamera.SizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                //TODO 截断测试，请不要截断，否则坐标和宽高不准确
//                wonderfulCamera.keepValueSize = false;
                //TODO 在坐标发生改变是我们再设置自定义的坐标，这样就能得到各控件的坐标宽高作为参考
                //TODO 将自定view置于wonderfulCamera:水平居中，垂直四分之一处
                coordinateView.x = wonderfulCamera.getMeasuredWidth() / 2 - coordinateView.view.getMeasuredWidth() / 2;
                coordinateView.y = wonderfulCamera.getMeasuredHeight() / 4 - coordinateView.view.getMeasuredHeight()/2;
                Log.d(TAG,"width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this,"width: " + width + " - height: " + height,Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG,"onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this,"onLeftClick - back",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG,"onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this,"onRightClick - album",Toast.LENGTH_SHORT).show();
            }
        });

        //相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraModeSelectListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, int position, String message) {
                Log.d(TAG,"onModeSelect -> position: " + position + " mode: " + message);
                Toast.makeText(WonderfulCameraActivity.this,"onModeSelect -> position: " + position + " mode: " + message,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(int position) {
                Log.d(TAG,"center -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"center -> mode: " + position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(int position) {
                Log.d(TAG,"bottom -> mode: " + position);
                Toast.makeText(WonderfulCameraActivity.this,"bottom -> mode: " + position,Toast.LENGTH_SHORT).show();
            }
        });

        //强制获取一个自定义View集合
        List<WonderfulCamera.CoordinateView> coordinateViews = wonderfulCamera.forceRequestCoordinateView();
        //定义自己的按钮
        TabView tabView = new TabView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);

        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("我的按钮");
        tabView.setBottomTitle("我的按钮");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.init();
        coordinateView.view = tabView;
        //TODO 任意的坐标设置没有基准，在这里并不合适
//        coordinateView.x = 200;
//        coordinateView.y = 200;

        //设置他的点击事件
        tabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                view.setChecked(!view.isChecked());
                Toast.makeText(WonderfulCameraActivity.this,"我的按钮",Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 这个集合包含了两个默认的左右按钮，根据需求可以删除
        coordinateViews.clear();
        //添加到集合中，
        coordinateViews.add(coordinateView);

        //初始化
        wonderfulCamera.init();

    }

    private Drawable getDrawableFromSource(int sourceId){
        return getResources().getDrawable(sourceId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wonderfulCamera.releaseCamera();
    }


    @Override
    public void pictureFetch(Bitmap bitmap, byte[] data) {
        Bitmap picBitmap = cameraDataHandle.byteToBitmap(data);
        PictureActivity.bitmap = picBitmap;
        Intent intent = new Intent(this,PictureActivity.class);
        startActivity(intent);
    }
}