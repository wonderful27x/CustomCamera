package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
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
import com.example.cameratest.google_zxing.BeepManager;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import java.text.DateFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

/**
 * wonderfulCamera 使用手册
 */
public class WonderfulCameraActivity extends AppCompatActivity implements CameraDataTransport,ScanResultListener{

    private static final String TAG = "WonderfulCameraActivity";

    private WonderfulCamera wonderfulCamera;
    private ProgressDialog progressDialog;

    //蜂鸣器,暂时写在这里，以后封装到WonderfulCamera中
    private BeepManager beepManager;


    private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
            EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
                    ResultMetadataType.SUGGESTED_PRICE,
                    ResultMetadataType.ERROR_CORRECTION_LEVEL,
                    ResultMetadataType.POSSIBLE_COUNTRY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wonderful_camere);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("图片处理中...");
        beepManager = new BeepManager(this);

        //testA();
        testB();
        //testC();
    }

    /**
     * 测试A，使用默认设置，最简单的使用
     */
    private void testA() {
        wonderfulCamera = findViewById(R.id.cameraView);
        //设置回调监听以便获取拍照数据
        wonderfulCamera.setCameraDataTransport(this);
        //设置扫码回调
        wonderfulCamera.setScanResultListener(this);

//        //设置前摄，默认后摄像
//        wonderfulCamera.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        //设置拍照质量0:高质量 1：低质量
//        wonderfulCamera.setPicLevel(1);

        //设置大小发生改变的监听，可获取控件的size，默认只要大小改变就会回调
        //一般情况下会回调两次，第二次才是准确的
        //通过设置wonderfulCamera.keepValueSize = false，可截断回调
        wonderfulCamera.setSizeChangedListener(new WonderfulCamera.SizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                Log.d(TAG, "width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this, "width: " + width + " - height: " + height, Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG, "onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this, "onLeftClick - back", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG, "onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this, "onRightClick - album", Toast.LENGTH_SHORT).show();
            }
        });

        ////相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraEventListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, CameraMode currentMode, String message) {
                Log.d(TAG, "onModeSelect -> mode: " + currentMode.getMode() + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "onModeSelect: " + currentMode.getMode() + " message: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "center -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "center -> position: " + currentMode + " buttonName: " + buttonName + " mode: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message, Toast.LENGTH_SHORT).show();
                //如果当前选中的是拍照模式
                if (currentMode == CameraMode.PICTURE) {
                    progressDialog.show();
                    wonderfulCamera.takePicture();
                }
                //TODO 这样也可以
//                if ("拍照".equals(message)) {
//                    progressDialog.show();
//                    wonderfulCamera.takePicture();
//                }
            }
        });

        //设置数据监听接口
        wonderfulCamera.setCameraDataTransport(this);

        //初始化
        wonderfulCamera.init();

    }


    //如默认设置无法满足需要可以修改默认熟悉
    //例如当前只支持拍照和扫一扫
    //需求1：将扫一扫修改为扫码，并替换图标
    //需求2：想多增加一个识别按钮
    private void testB() {
        wonderfulCamera = findViewById(R.id.cameraView);
        //设置回调监听以便获取拍照数据
        wonderfulCamera.setCameraDataTransport(this);
        //设置扫码回调
        wonderfulCamera.setScanResultListener(this);

        //设置大小发生改变的监听，可获取控件的size，默认只要大小改变就会回调
        //一般情况下会回调两次，第二次才是准确的
        //通过设置wonderfulCamera.keepValueSize = false，可截断回调
        wonderfulCamera.setSizeChangedListener(new WonderfulCamera.SizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                //截断测试
                wonderfulCamera.keepValueSize = false;
                Log.d(TAG, "width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this, "width: " + width + " - height: " + height, Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG, "onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this, "onLeftClick - back", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG, "onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this, "onRightClick - album", Toast.LENGTH_SHORT).show();
            }
        });

        ////相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraEventListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, CameraMode currentMode, String message) {
                Log.d(TAG, "onModeSelect -> mode: " + currentMode.getMode() + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "onModeSelect -> mode: " + currentMode.getMode() + " message: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "center -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "center -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message, Toast.LENGTH_SHORT).show();
                //如果当前选中的是拍照模式，则调用api进行拍照
                if (currentMode == CameraMode.PICTURE) {
                    wonderfulCamera.takePicture();
                }
                //TODO 也可以这样
//                if ("拍照".equals(message)) {
//                    wonderfulCamera.takePicture();
//                }
            }
        });

        /**
         * 修改默认设置
         */
        //获取元件仓库集合
        Map<String, WonderfulCamera.ComponentDepot> componentDepotList = wonderfulCamera.forceRequestComponentDepot();
        //获取扫一扫元件仓库
        WonderfulCamera.ComponentDepot scan = componentDepotList.get(Keys.SCAN);
        //获取导航按钮并修改为扫码，并替换图标
        TabView tabView = (TabView) scan.targetTypeView.view;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setTopTitle("扫码");
        tabView.setBottomTitle("扫码");
        tabView.init();

        /**
         * 添加自己的识别按钮
         * TODO 注意添加自定义按钮有些复杂，必须使用Keys中的值，并且需要在CameraMode中添加对于的枚举类型
         */
        //先构建一个元件仓库
        WonderfulCamera.ComponentDepot depot = wonderfulCamera.createComponentDepot();
        String key = Keys.RECOGNIZE;
        //添加自己的按钮属性
        //导航栏按钮
        tabView = (TabView) depot.targetTypeView.view;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("识别");
        tabView.setBottomTitle("识别");
        tabView.init();
        //中心shape
        tabView = (TabView) depot.centerShape.view;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.sel_nor));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.sel_nor));
        tabView.init();
        //中心按钮
        tabView = (TabView) depot.centerButton.view;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("打开手电");
        tabView.setBottomTitle("关闭手电");
        tabView.init();
        //底部按钮
        tabView = (TabView) depot.bottomButton.view;
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setTopTitle("点击识别");
        tabView.setBottomTitle("点击识别");
        tabView.init();

        //添加到集合中
        componentDepotList.put(key,depot);

        //可以指定选择导航按钮的位置，默认为bottom
        wonderfulCamera.setSnakePosition("top");

        //修改导航按钮的间隔
        wonderfulCamera.setSnakeHorizontalGap(10);

        //初始化
        wonderfulCamera.init();

        //设置当前选中的模式，默认为0
        wonderfulCamera.setCurrentMode(CameraMode.SCAN);
    }

    /**
     * 如果通过修改增加默认属性仍然无法满足需求，那么使用自定义View
     */
    private void testC() {
        wonderfulCamera = findViewById(R.id.cameraView);
        //设置回调监听以便获取拍照数据
        wonderfulCamera.setCameraDataTransport(this);

        //构建一个CoordinateView，用于设定自己的自定义view
        final ComponentView coordinateView = new ComponentView();

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
                coordinateView.y = wonderfulCamera.getMeasuredHeight() / 4 - coordinateView.view.getMeasuredHeight() / 2;
                Log.d(TAG, "width: " + width + " - height: " + height);
                Toast.makeText(WonderfulCameraActivity.this, "width: " + width + " - height: " + height, Toast.LENGTH_SHORT).show();
            }
        });

        //设置边缘事件监听，即相机默认的返回\相册按钮
        wonderfulCamera.setEdgeButtonListener(new WonderfulCamera.EdgeButtonListener() {
            @Override
            public void onLeftClick(View view) {
                Log.d(TAG, "onLeftClick - back");
                Toast.makeText(WonderfulCameraActivity.this, "onLeftClick - back", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Log.d(TAG, "onRightClick - album");
                Toast.makeText(WonderfulCameraActivity.this, "onRightClick - album", Toast.LENGTH_SHORT).show();
            }
        });

        //相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
        wonderfulCamera.setCameraEventListener(new WonderfulCamera.CameraEventListener() {
            @Override
            public void onModeSelect(View view, CameraMode currentMode, String message) {
                Log.d(TAG, "onModeSelect -> mode: " + currentMode.getMode() + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "onModeSelect -> mode: " + currentMode.getMode() + " message: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void centerOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "center -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "center -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bottomOnClick(CameraMode currentMode, String buttonName, String message) {
                Log.d(TAG, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message);
                Toast.makeText(WonderfulCameraActivity.this, "bottom -> mode: " + currentMode.getMode() + " buttonName: " + buttonName + " message: " + message, Toast.LENGTH_SHORT).show();
                //如果当前选中的是拍照模式
                if (currentMode == CameraMode.PICTURE) {
                    wonderfulCamera.takePicture();
                }
                //TODO 这样也可以
//                if ("拍照".equals(message)) {
//                    wonderfulCamera.takePicture();
//                }
            }
        });

        //强制获取一个自定义View集合
        Map<String,ComponentView> coordinateViews = wonderfulCamera.forceRequestCoordinateView();
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
                Toast.makeText(WonderfulCameraActivity.this, "我的按钮", Toast.LENGTH_SHORT).show();

                //TODO 作为一个测试，我们还能动态修改当前选中的模式
//                wonderfulCamera.setCurrentMode(1);

                //TODO 我们还可以随意切换摄像头
                wonderfulCamera.switchCamera();
            }
        });

        //TODO 这个集合包含了两个默认的左右按钮，根据需求可以删除
        coordinateViews.clear();
        //添加到集合中，
        String key = "my_button";
        coordinateViews.put(key,coordinateView);

        //TODO 高级使用-数据加工厂
        //TODO 如果相机默认数据加工不能满足需求，则可以设置自己的数据加工厂
        //TODO 90度旋转测试
        wonderfulCamera.addCameraDataFactory(new RotationFactory());

        //初始化
        wonderfulCamera.init();

    }

    private Drawable getDrawableFromSource(int sourceId) {
        return getResources().getDrawable(sourceId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beepManager.updatePrefs();
        if (wonderfulCamera != null){
            wonderfulCamera.scanReset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beepManager.close();
        wonderfulCamera.releaseCamera();
    }


    //拍照后的最终数据会运输到这里，这里的数据已经经过了加工处理，如水印等
    @Override
    public void picture(Bitmap bitmap, byte[] data) {
        progressDialog.dismiss();
        PictureActivity.bitmap = bitmap;
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    //扫码返回接口
    @Override
    public void onScanResult(Result result,byte[] scanBitmap) {

        beepManager.playBeepSoundAndVibrate();

        PictureActivity.bitmapArray = scanBitmap;
        Intent intent = new Intent(this, PictureActivity.class);
        ParsedResult parsedResult = ResultParser.parseResult(result);
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String date = formatter.format(result.getTimestamp());
        Map<ResultMetadataType,Object> metadata = result.getResultMetadata();
        StringBuilder metadataText = new StringBuilder(20);
        if (metadata != null) {
            for (Map.Entry<ResultMetadataType,Object> entry : metadata.entrySet()) {
                if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
                    metadataText.append(entry.getValue()).append('\n');
                }
            }
            if (metadataText.length() > 0) {
                metadataText.setLength(metadataText.length() - 1);
            }
        }
        String contents = parsedResult.getDisplayResult();
        contents =  contents.replace("\r", "");
        intent.putExtra("format",result.getBarcodeFormat().toString());
        intent.putExtra("type",parsedResult.getType().toString());
        intent.putExtra("time",date);
        intent.putExtra("metadata",metadataText.toString());
        intent.putExtra("content",contents);
        startActivity(intent);
    }
}
