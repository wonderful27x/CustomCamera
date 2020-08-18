package com.example.cameratest.core;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.cameratest.R;
import com.example.cameratest.annotation.CameraDataFactory;
import com.example.cameratest.annotation.CameraDataTransport;
import com.example.cameratest.annotation.CameraMode;
import com.example.cameratest.annotation.CoordinateKey;
import com.example.cameratest.annotation.ScanResultListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  @Author wonderful
 *  @Date 2020-5-21
 *  @Version 1.0
 *  @Description 自定义相机
 *  TODO 初始版本，性能有待提升,多线程，拦截器,UI和业务逻辑应该解藕
 */
public class WonderfulCamera extends RelativeLayout implements CameraDataTransport {

    //相机辅助类
    private CameraHelper cameraHelper;
    //相机预览surfaceView
    private SurfaceView surfaceView;
    //固定的相机元件仓库集合，每个item代表相机的一个功能，如拍照、扫码
    private LinkedHashMap<String,ComponentDepot> componentDepots;
    //用户自定义的View，如果固定的相机元素不能满足需求用户可添加自己的View，这个view的位置由用户通过坐标指定
    private LinkedHashMap<String, ComponentView> coordinateViews;
    //相机选择导航按钮，用于切换相机功能，如拍照、扫码
    private CustomSnakeBar<View> snakeBar;

    //相机预览宽高
    private int previewWidth;
    private int previewHeight;
    //相机拍照宽高
    private int picWidth;
    private int picHeight;
    //对焦模式
    private String focusMode;
    //相机Id
    private int cameraId;

    //当前控件的padding
    private int marginLeft;
    private int marginTop;
    private int marginRight;
    private int marginBottom;

    //snakeBar的padding
    private int snakePaddingLeft;
    private int snakePaddingTop;
    private int snakePaddingRight;
    private int snakePaddingBottom;

    //snakeBar的位置属性
    //只支持：top/bottom,默认bottom
    private String snakePosition = "bottom";

    //默认情况下的左边按钮
    private ComponentView leftDefault;
    //默认情况下右边按钮
    private ComponentView rightDefault;

    //控件大小回调标志，默认只要发生改变就回调
    //用户可手动截断
    public boolean keepValueSize = true;
    //当前选中的模式，如拍照或扫码
    private CameraMode currentMode;

    //拍照模式,默认为0
    //0：高质量-使用takePicture api
    //1：低质量-使用预览数据，这种模式下
    private int picLevel;

    //导航按钮（snakeBar中间按钮）的间隔
    private int snakeHorizontalGap;

    //扫码管理类
    private ScanManager scanManager;

    private Context context;

    //控件大小发生改变时的接口回调，这时就能够获取控件的大小信息了
    //在用户自定义控件任意指定控件的坐标时非常有用
    //默认情况下此接口会持续触发，用户可通过keepValueSize标志手动截断
    //TODO 测试发现一般会回调两次，第二次才是准确的
    private SizeChangedListener sizeChangedListener;

    //边缘按钮监听，即对应返回按钮和相册按钮
    private EdgeButtonListener edgeButtonListener;
    //相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
    private CameraEventListener cameraEventListener;

    //相机数据处理模块-相机数据工厂，对数据进行加工处理，如水印
    private List<CameraDataFactory> cameraDataFactory;
    //相机数据运输接口，将加工后的数据运输到外界
    private CameraDataTransport cameraDataTransport;
    //扫描结果返回接口
    private ScanResultListener scanResultListener;

    public WonderfulCamera(Context context) {
        this(context,null);
    }

    public WonderfulCamera(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WonderfulCamera(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        rightDefault.x = getMeasuredWidth() - rightDefault.view.getMeasuredWidth() - marginRight;
        rightDefault.y = marginTop;
        leftDefault.x = marginLeft;
        leftDefault.y = marginTop;
        if (sizeChangedListener != null){
            if (keepValueSize){
                sizeChangedListener.onSizeChanged(getMeasuredWidth(),getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        //布局surface，充满屏幕
        surfaceView.layout(0,0,width,height);

        //布局snakeBar
        if ("bottom".equals(snakePosition)){
            left = marginLeft;
            top = height - snakeBar.getMeasuredHeight() - marginBottom;
            right = width - marginRight;
            bottom = top + snakeBar.getMeasuredHeight();

        }else if("top".equals(snakePosition)){
            left = marginLeft;
            top = marginTop;
            right = width - marginRight;
            bottom = top + snakeBar.getMeasuredHeight();
        }
        snakeBar.layout(left,top,right,bottom);

        //布局固定控件
        if (componentDepots != null){
            for (ComponentDepot depot:componentDepots.values()){
                int centerX = width / 2;
                int centerY = height / 2;
                //centerShape
                left = centerX - depot.centerShape.view.getMeasuredWidth() / 2;
                top = centerY - depot.centerShape.view.getMeasuredHeight() / 2;
                right = left + depot.centerShape.view.getMeasuredWidth();
                bottom = top + depot.centerShape.view.getMeasuredHeight();
                depot.centerShape.view.layout(left,top,right,bottom);
                //centerButton
                left = centerX - depot.centerButton.view.getMeasuredWidth() / 2;
                top = centerY - depot.centerButton.view.getMeasuredHeight() / 2;
                right = left + depot.centerButton.view.getMeasuredWidth();
                bottom = top + depot.centerButton.view.getMeasuredHeight();
                depot.centerButton.view.layout(left,top,right,bottom);
                //bottomButton
                if ("bottom".equals(snakePosition)){
                    left = centerX - depot.bottomButton.view.getMeasuredWidth() / 2;
                    top = height - depot.bottomButton.view.getMeasuredHeight() - snakeBar.getMeasuredHeight() - marginBottom;
                    right = left + depot.bottomButton.view.getMeasuredWidth();
                    bottom = top + depot.bottomButton.view.getMeasuredHeight();
                }else if("top".equals(snakePosition)){
                    left = centerX - depot.bottomButton.view.getMeasuredWidth() / 2;
                    top = height - depot.bottomButton.view.getMeasuredHeight() - marginBottom;
                    right = left + depot.bottomButton.view.getMeasuredWidth();
                    bottom = top + depot.bottomButton.view.getMeasuredHeight();
                }
                depot.bottomButton.view.layout(left,top,right,bottom);
            }
        }

        //用户自定义控件
        if(coordinateViews != null){
            for (ComponentView view:coordinateViews.values()){
                view.view.layout(view.x,view.y,view.x + view.view.getMeasuredWidth(),view.y + view.view.getMeasuredHeight());
            }
        }
    }

    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulCameraStyle);
        snakePosition = typedArray.getString(R.styleable.wonderfulCameraStyle_snakePosition);
        typedArray.recycle();
        if (snakePosition == null){
            snakePosition = "bottom";
        }
        initDefault();
    }

    private void initDefault(){
        this.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;             //默认启用后摄
        this.focusMode = null;                                            //使用默认对焦模式
        this.previewWidth = -1;
        this.previewHeight = -1;
        this.picWidth = -1;
        this.picHeight = -1;

        marginLeft = 10;
        marginTop = 10;
        marginRight = 10;
        marginBottom = 10;

        snakePaddingLeft = 5;
        snakePaddingTop = 5;
        snakePaddingRight = 5;
        snakePaddingBottom = 5;

        currentMode = CameraMode.PICTURE;

        picLevel = 0;

        snakeHorizontalGap = 50;

        TabView tabView;
        RelativeLayout.LayoutParams params;

        //数据加工厂
        cameraDataFactory = new ArrayList<>();

        //固定控件
        componentDepots = new LinkedHashMap<>();
        //拍照
        ComponentDepot componentPic = new ComponentDepot();
        //centerShape
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.video_show_play));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.video_show_play));
        tabView.init();
        componentPic.centerShape = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //centerButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("拍点亮");
        tabView.setBottomTitle("拍点亮");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.init();
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                view.setChecked(!view.isChecked());
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.centerOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentPic.centerButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //bottomButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.init();
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.bottomOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentPic.bottomButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //snakeBar对应的按钮
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("拍照");
        tabView.setBottomTitle("拍照");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.setBackgroundColor(Color.GREEN);
        tabView.init();
        componentPic.targetTypeView = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //扫码
        ComponentDepot componentScanCode = new ComponentDepot();
        //centerShape
        ImageView imageView = new ImageView(context);
        params = new RelativeLayout.LayoutParams(500, 500);
        imageView.setLayoutParams(params);
        imageView.setBackground(getDrawableFromSource(R.drawable.xj_photo_up));
        componentScanCode.centerShape = createComponentView(-1,-1,500,500,imageView);

        //centerButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("扫点亮");
        tabView.setBottomTitle("扫点亮");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.init();
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                view.setChecked(!view.isChecked());
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.centerOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentScanCode.centerButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //bottomButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.init();
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.bottomOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentScanCode.bottomButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //snakeBar对应的按钮
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("扫一扫abcde");
        tabView.setBottomTitle("扫一扫abcde");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.setBackgroundColor(Color.GREEN);
        tabView.init();
        componentScanCode.targetTypeView = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        componentDepots.put(CameraMode.PICTURE.getMode(),componentPic);
        componentDepots.put(CameraMode.SCAN.getMode(),componentScanCode);

        //用户自定义控件
        coordinateViews = new LinkedHashMap<>();
        //返回按钮

        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setResponseMode(0);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setClickable(true);
        tabView.init();
        leftDefault = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //相册按钮
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setResponseMode(0);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.album));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.album));
        tabView.setClickable(true);
        tabView.init();
        rightDefault = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        coordinateViews.put(CoordinateKey.BACK_BTN.getKey(),leftDefault);
        coordinateViews.put(CoordinateKey.ALBUM_BTN.getKey(),rightDefault);
    }

    public void setComponentDepots(LinkedHashMap<String,ComponentDepot> componentDepots) {
        this.componentDepots = componentDepots;
    }

    public void setCoordinateViews(LinkedHashMap<String,ComponentView> coordinateViews) {
        this.coordinateViews = coordinateViews;
    }

    public String getSnakePosition() {
        return snakePosition;
    }

    public void setSnakePosition(String snakePosition) {
        if (!"top".equals(snakePosition) && !"bottom".equals(snakePosition)){
            throw new IllegalArgumentException(getClass().getSimpleName() + ".setSnakePosition: 此方法只接受 top/bottom 两种类型!");
        }
        this.snakePosition = snakePosition;
    }

    //外界调用初始化
    public void init(){
        initCamera();
        initViews();
        addToWindow();
    }

    //初始化相机
    private void initCamera(){
        cameraHelper = new CameraHelper((Activity) context);
        cameraHelper.setCameraId(cameraId);
        cameraHelper.setExpectPreviewSize(previewWidth,previewHeight);
        cameraHelper.setExpectPicSize(picWidth,picHeight);
        cameraHelper.setPicLevel(picLevel);

        //如果用户没有设置对焦模式
        if (focusMode == null){
            //如果当前是拍照模式，则默认使用PICTURE对焦模式
            //TODO 但是请注意CameraMode.PICTURE默认认为0是拍照，1是扫码，
            //TODO 如果用户自定义了相机选择模式则应该自己根据需求设置对焦模式
            if(currentMode == CameraMode.PICTURE){
                cameraHelper.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            //否则使用VIDEO对焦模式
            else {
                cameraHelper.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }else {
            cameraHelper.setFocusMode(focusMode);
        }

        cameraHelper.setCameraDataTransport(this);
    }

    //初始化控件
    private void initViews(){
        //初始化surfaceView
        surfaceView = new SurfaceView(context);
        //初始化相机选择导航按钮snakeBar
        snakeBar = new CustomSnakeBar<>(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snakeBar.setLayoutParams(params);
        //设置padding
        snakeBar.setMidPaddingLeft(snakePaddingLeft);
        snakeBar.setMidPaddingTop(snakePaddingTop);
        snakeBar.setMidPaddingRight(snakePaddingRight);
        snakeBar.setMidPaddingBottom(snakePaddingBottom);
        //横向均据摆放间隔
        snakeBar.setGapHorizontal(snakeHorizontalGap);
        //设置snakeBar的中间按钮
        List<View> views = new ArrayList<>();
        if (componentDepots != null){
            for (ComponentDepot depot:componentDepots.values()){
                views.add(depot.targetTypeView.view);
            }
        }
        snakeBar.addChildren(views);
        //TODO test
        snakeBar.setBackgroundColor(Color.YELLOW);

        //如果snakeBar在上方则启用snakeBar的左右按钮并隐藏默认的左右按钮
        //否则说明snakeBar在下方，使用默认左右按钮
        if ("top".equals(snakePosition)){
            snakeBar.enableLeftButton(null);
            snakeBar.enableRightButton(null);
            leftDefault.view.setVisibility(GONE);
            rightDefault.view.setVisibility(GONE);
            //设置左右按钮的点击事件监听
            snakeBar.setLeftClickListener(new CustomSnakeBar.LeftClickListener() {
                @Override
                public void onLeftClick(View view) {
                    if(edgeButtonListener != null){
                        edgeButtonListener.onLeftClick(view);
                    }
                }
            });
            snakeBar.setRightClickListener(new CustomSnakeBar.RightClickListener() {
                @Override
                public void onRightClick(View view) {
                    if(edgeButtonListener != null){
                        edgeButtonListener.onRightClick(view);
                    }
                }
            });
        }else if("bottom".equals(snakePosition)){
            //设置左右按钮的点击事件监听
            leftDefault.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edgeButtonListener != null){
                        edgeButtonListener.onLeftClick(v);
                    }
                }
            });
            rightDefault.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edgeButtonListener != null){
                        edgeButtonListener.onRightClick(v);
                    }
                }
            });
        }

        //设置中间按钮监听
        snakeBar.setOnItemClickListener(new CustomSnakeBar.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CameraMode lastMode = currentMode;
                String key = getKeyFromPosition(position);
                CameraMode mode = CameraMode.cameraMode(key);
                setCurrentMode(mode,false);
                if (cameraEventListener != null){
                    TabView tabView = (TabView) view;
                    cameraEventListener.onModeSelect(view,mode,tabView.getTopTitle());
                }
                //如果模式发生了改变则重新设置对焦模式并重新预览
                if (lastMode != mode){
                    rePreviewCamera();
                }
            }
        });

        //初始化snake
        snakeBar.init();

        //默认选择位置
        setCurrentMode(CameraMode.PICTURE,true);
    }

    //通过位置position，从LinkedHashMap中获取key
    private String getKeyFromPosition(int position){
        List<Map.Entry<String, ComponentDepot>> entityList = new ArrayList<>(componentDepots.entrySet());
        Map.Entry<String, ComponentDepot> entry = entityList.get(position);
        if (entry != null){
            return entry.getKey();
        }
        return null;
    }

    //通过key，从LinkedHashMap中获取位置position
    private int getPositionFromKey(String key){
        List<Map.Entry<String, ComponentDepot>> entityList = new ArrayList<>(componentDepots.entrySet());
        for (int i=0; i<entityList.size(); i++){
            if (key.equals(entityList.get(i).getKey())){
                return i;
            }
        }
        return -1;
    }

    //重新设置对焦模式并重新预览
    private void rePreviewCamera(){
        if (focusMode == null){
            if(currentMode == CameraMode.PICTURE){
                cameraHelper.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            else {
                cameraHelper.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }else {
            cameraHelper.setFocusMode(focusMode);
        }
        cameraHelper.rePreview();
        //二维码扫描初始化
        initScan();
    }

    //TODO 二维码扫描初始化
    private void initScan(){
        if (currentMode == CameraMode.SCAN){
            Point scan = getComponentSize(componentDepots.get(CameraMode.SCAN.getMode()).centerShape);
            Point preview = new Point(cameraHelper.getPreviewWidth(),cameraHelper.getPreviewHeight());
            Point screen = getScreenSize();
            scanManager = new ScanManager(scan,preview,screen);
            scanManager.setScanResultListener(scanResultListener);
            cameraHelper.setPreviewCallback(scanManager);
        }else {
            if (scanManager != null){
                scanManager.release();
            }
        }
    }

    //添加到窗口中
    private void addToWindow(){
        //先清理
        removeAllViews();
        //添加surfaceView
        this.addView(surfaceView);
        //添加snakeBar
        this.addView(snakeBar);
        //添加固定元素
        if (componentDepots != null){
            for (ComponentDepot depot:componentDepots.values()){
                this.addView(depot.centerShape.view);
                this.addView(depot.centerButton.view);
                this.addView(depot.bottomButton.view);
            }
        }
        //添加用户自定view
        if (coordinateViews != null){
            for (ComponentView view:coordinateViews.values()){
                this.addView(view.view);
            }
        }

        cameraHelper.setHolder(surfaceView.getHolder());
    }

    /**
     * 设置预览尺寸
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public void setExpectPreviewSize(int expectWidth, int expectHeight){
        this.previewWidth = expectWidth;
        this.previewHeight = expectHeight;
    }

    /**
     * 设置拍照尺寸
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public void setExpectPicSize(int expectWidth, int expectHeight){
        this.picWidth = expectWidth;
        this.picHeight = expectHeight;
    }

    public String getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(String focusMode) {
        this.focusMode = focusMode;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getPreviewWidth() {
        return cameraHelper.getPreviewWidth();
    }

    public int getPreviewHeight() {
        return cameraHelper.getPreviewHeight();
    }

    public int getPicWidth() {
        return cameraHelper.getPicWidth();
    }

    public int getPicHeight() {
        return cameraHelper.getPicHeight();
    }

    private Drawable getDrawableFromSource(int sourceId){
        return getResources().getDrawable(sourceId);
    }

    public CameraMode getCurrentMode() {
        return currentMode;
    }

    //设置当前选中模式,外界调用
    public void setCurrentMode(CameraMode currentMode) {
        CameraMode lastMode = this.currentMode;
        //控制各控件的显示与隐藏
        setCurrentMode(currentMode,true);
        //重新预览，因为不同的模式下对焦模式也不同
        if(lastMode != currentMode){
            rePreviewCamera();
        }
    }

    //设置当前选中模式内部调用
    private void setCurrentMode(CameraMode currentMode,boolean snake) {
        this.currentMode = currentMode;
        if (componentDepots != null){
            for (Map.Entry<String,ComponentDepot> entry:componentDepots.entrySet()){
                //先隐藏非选中控件
                if (!entry.getKey().equals(currentMode.getMode())){
                    entry.getValue().centerShape.view.setVisibility(INVISIBLE);
                    entry.getValue().centerButton.view.setVisibility(INVISIBLE);
                    entry.getValue().bottomButton.view.setVisibility(INVISIBLE);
                }
                //显示选中控件
                else {
                    entry.getValue().centerShape.view.setVisibility(VISIBLE);
                    entry.getValue().centerButton.view.setVisibility(VISIBLE);
                    entry.getValue().bottomButton.view.setVisibility(VISIBLE);
                }
            }
        }
        //设置snakeBar的选中项
        if (snake){
            int position = getPositionFromKey(currentMode.getMode());
            if (position != -1){
                snakeBar.setChoosePosition(position);
            }
        }
    }

    public int getSnakeHorizontalGap() {
        return snakeHorizontalGap;
    }

    public void setSnakeHorizontalGap(int snakeHorizontalGap) {
        this.snakeHorizontalGap = snakeHorizontalGap;
    }

    public void releaseCamera(){
        if (cameraHelper != null){
            cameraHelper.release();
        }
        if (scanManager != null){
            scanManager.release();
        }
    }

    /**
     * 如果默认的功能无法满足需求，调用此方法强制返回一个相机管理类
     * @return
     */
    public CameraHelper forceRequestCamera(){
        return cameraHelper;
    }

    /**
     * 如果默认的功能无法满足需求，调用此方法强制返回一个元件仓库集合
     * 每个元件仓库代表相机的一种操作类型，如拍照或扫码，这个元件仓库主要包含4个元素
     * 1：shape，可用于扫码框的实现
     * 2：centerButton,中心按钮，可用于手电筒的开关
     * 3：bottomButton,底部按钮，可用于点击拍照
     * 4：当前模式下的导航栏按钮
     * 可以修改、增加、删除元件来达到自己的目的
     * @return
     */
    public Map<String,ComponentDepot> forceRequestComponentDepot(){
        return componentDepots;
    }

    /**
     * 如果通过修改增加默认属性仍然无法满足需求，调用此方法强制返回一个自定义View集合
     * @return
     */
    public Map<String,ComponentView> forceRequestCoordinateView(){
        return coordinateViews;
    }

    /**
     * 固定的相机元件仓库
     * 每个仓库对于了相机的一个功能，如拍照、扫码
     * 他是和相机选择导航按钮snakeBar相关联的
     */
    public static class ComponentDepot{
        public ComponentView centerShape;     //相机中间的一个形状，如扫码框
        public ComponentView centerButton;    //相机中间的一个Button,覆盖在centerShape的上层
        public ComponentView bottomButton;    //相机底部按钮，如拍照的按钮
        public ComponentView targetTypeView;  //与snakeBar对应的按钮
    }

    /**
     * 构建一个元件仓库方便外界使用，自己new容易出现空指针
     * @return
     */
    public ComponentDepot createComponentDepot(){
        ComponentDepot componentDepot = new ComponentDepot();
        TabView tabView;
        RelativeLayout.LayoutParams params;
        //centerShape
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        componentDepot.centerShape = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //centerButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                view.setChecked(!view.isChecked());
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.centerOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentDepot.centerButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //bottomButton
        tabView = new TabView(context);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(params);
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TabView view = (TabView) v;
                if(cameraEventListener != null){
                    TabView targetView = (TabView) componentDepots.get(currentMode.getMode()).targetTypeView.view;
                    cameraEventListener.bottomOnClick(currentMode,view.getTopTitle(),targetView.getTopTitle());
                }
            }
        });
        componentDepot.bottomButton = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        //snakeBar对应的按钮
        tabView = new TabView(context);
        componentDepot.targetTypeView = createComponentView(-1,-1,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,tabView);

        return componentDepot;
    }

    public void setPicLevel(int picLevel) {
        this.picLevel = picLevel;
    }

    /**
     * 切换摄像头
     */
    public void switchCamera(){
        cameraHelper.switchCamera();
    }

    /**
     * 获取元件的宽高
     * @param componentView
     * @return
     */
    private Point getComponentSize(ComponentView componentView){
        return new Point(componentView.width,componentView.height);
    }

    private Point getScreenSize(){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    public void addCameraDataFactory(CameraDataFactory cameraDataFactory) {
        this.cameraDataFactory.add(cameraDataFactory);
    }

    public void setCameraDataTransport(CameraDataTransport cameraDataTransport) {
        this.cameraDataTransport = cameraDataTransport;
    }



    //构建ComponentView
    public ComponentView createComponentView(int x,int y,int width,int height,View view){
        ComponentView componentView = new ComponentView();
        componentView.x = x;
        componentView.y = y;
        componentView.width = width;
        componentView.height = height;
        componentView.view = view;
        return componentView;
    }

    public void setSizeChangedListener(SizeChangedListener sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }

    public void setEdgeButtonListener(EdgeButtonListener edgeButtonListener) {
        this.edgeButtonListener = edgeButtonListener;
    }

    public void setCameraEventListener(CameraEventListener cameraEventListener) {
        this.cameraEventListener = cameraEventListener;
    }

    public void setScanResultListener(ScanResultListener scanResultListener) {
        this.scanResultListener = scanResultListener;
    }

    //控件大小发生改变的监听
    public interface SizeChangedListener{
        public void onSizeChanged(int width,int height);
    }

    //左右按钮的监听，即对应返回按钮和相册按钮
    public interface EdgeButtonListener{
        public void onLeftClick(View view);
        public void onRightClick(View view);
    }

    //相机事件监听，注意这里包括三个事件，1：相机模式选择，2：当前模式下的中间按钮点击事件，3：当前模式下底部按钮点击事件
    public interface CameraEventListener{
        //1 -> view:选中的View，position：选中的位置，代表了相机的模式，message：选中模式的字符串描述，和按钮的名字一致
        public void onModeSelect(View view, CameraMode currentMode,String message);
        //2 -> position：当前选中的位置，代表了相机的模式，buttonName:当前按钮名字，message：选中模式的字符串描述
        public void centerOnClick(CameraMode currentMode,String buttonName,String message);
        //3 -> position：当前选中的位置，代表了相机的模式，buttonName:当前按钮名字，message：选中模式的字符串描述
        public void bottomOnClick(CameraMode currentMode,String buttonName,String message);
    }

    //拍照数据运输接口，当拍照成功后会将拍照数据运输到这里
    //然后调用相机数据工厂进行加工，最后再将加工后的数据继续运输到外界
    @Override
    public void picture(Bitmap bitmap, byte[] data) {
        Bitmap picBitmap = bitmap;
        for (CameraDataFactory factory:cameraDataFactory){
            picBitmap = factory.working(picBitmap,data);
        }
        if (cameraDataTransport != null){
            cameraDataTransport.picture(picBitmap,data);
        }
    }

    /**
     * 拍照
     */
    public void takePicture(){
        if (cameraHelper != null){
            cameraHelper.takePicture();
        }
    }

    //重置扫码，让其继续工作，因为扫码成功后就暂停了
    public void scanReset(){
        if (scanManager != null){
            scanManager.reset();
        }
    }

}
