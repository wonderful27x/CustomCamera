package com.example.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;

/**
 *  @Author wonderful
 *  @Date 2020-5-21
 *  @Version 1.0
 *  @Description 自定义相机
 */
public class WonderfulCamera extends RelativeLayout {

    //相机辅助类
    private CameraHelper cameraHelper;
    //相机预览surfaceView
    private SurfaceView surfaceView;
    //固定的相机元件仓库集合，每个item代表相机的一个功能，如拍照、扫码
    private List<ComponentDepot> componentDepots;
    //用户自定义的View，如果固定的相机元素不能满足需求用户可添加自己的View，这个view的位置由用户通过坐标指定
    private List<CoordinateView> coordinateViews;
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
    private CoordinateView leftDefault;
    //默认情况下右边按钮
    private CoordinateView rightDefault;

    private Context context;

    public boolean sizeValued = false;

    //控件大小发生改变时的接口回调，这时就能够获取控件的大小信息了
    //在用户自定义控件任意指定控件的坐标时非常有用
    //默认情况下此接口只回调一次，如需多次回调则需手动将sizeValued设置为false
    private SizeChangedListener sizeChangedListener;

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
            if (!sizeValued){
                sizeValued = true;
                sizeChangedListener.onSizeChanged(getMeasuredWidth(),getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int left;
        int top ;
        int right;
        int bottom;

        //布局surface，充满屏幕
        surfaceView.layout(0,0,width,height);

        //布局snakeBar
        if ("bottom".equals(snakePosition)){
            left = marginLeft;
            top = height - snakeBar.getMeasuredHeight() - marginBottom;
            right = width - marginRight;
            bottom = height - marginBottom;

        }else {
            left = marginLeft;
            top = marginTop;
            right = width - marginRight;
            bottom = snakeBar.getMeasuredHeight() + marginTop;
        }
        snakeBar.layout(left,top,right,bottom);

        //布局固定控件
        if (componentDepots != null){
            for (ComponentDepot depot:componentDepots){
                int centerX = width / 2;
                int centerY = height / 2;
                //centerShape
                left = centerX - depot.centerShape.getMeasuredWidth() / 2;
                top = centerY - depot.centerShape.getMeasuredHeight() / 2;
                right = left + depot.centerShape.getMeasuredWidth();
                bottom = top + depot.centerShape.getMeasuredHeight();
                depot.centerShape.layout(left,top,right,bottom);
                //centerButton
                left = centerX - depot.centerButton.getMeasuredWidth() / 2;
                top = centerY - depot.centerButton.getMeasuredHeight() / 2;
                right = left + depot.centerButton.getMeasuredWidth();
                bottom = top + depot.centerButton.getMeasuredHeight();
                depot.centerButton.layout(left,top,right,bottom);
                //bottomButton
                if ("bottom".equals(snakePosition)){
                    left = centerX - depot.bottomButton.getMeasuredWidth() / 2;
                    top = height - depot.bottomButton.getMeasuredHeight() - snakeBar.getMeasuredHeight() - marginBottom;
                    right = left + depot.bottomButton.getMeasuredWidth();
                    bottom = top + depot.bottomButton.getMeasuredHeight() - marginBottom;
                }else {
                    left = centerX - depot.bottomButton.getMeasuredWidth() / 2;
                    top = marginTop;
                    right = left + depot.bottomButton.getMeasuredWidth();
                    bottom = top + depot.bottomButton.getMeasuredHeight() + marginTop;
                }
                depot.bottomButton.layout(left,top,right,bottom);
            }
        }

        //用户自定义buj
        if(coordinateViews != null){
            for (CoordinateView view:coordinateViews){
                view.view.layout(view.x,view.y,view.x + view.view.getMeasuredWidth(),view.y + view.view.getMeasuredHeight());
            }
        }
    }

    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.wonderfulCameraStyle);
        snakePosition = typedArray.getString(R.styleable.wonderfulCameraStyle_snakePosition);
        typedArray.recycle();
        if (snakePosition == null){
            snakePosition = "bottom";
        }
        initDefault();
    }

    private void initDefault(){
        this.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;             //默认启用后摄
        this.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE; //默认持续对焦
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

        TabView tabView;

        //固定控件
        componentDepots = new ArrayList<>();
        //拍照
        ComponentDepot componentPic = new ComponentDepot();
        //centerShape
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.init();
        componentPic.centerShape = tabView;

        //centerButton
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("点亮");
        tabView.setBottomTitle("点亮");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.init();
        componentPic.centerButton = tabView;

        //bottomButton
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.init();
        componentPic.bottomButton = tabView;

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
        componentPic.targetTypeView = tabView;

        //扫码
        ComponentDepot componentScanCode = new ComponentDepot();
        //centerShape
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.com_input_box_clear));
        tabView.init();
        componentScanCode.centerShape = tabView;

        //centerButton
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("点亮");
        tabView.setBottomTitle("点亮");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.init();
        componentScanCode.centerButton = tabView;

        //bottomButton
        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.icon_toggle_white_circle));
        tabView.init();
        componentScanCode.bottomButton = tabView;

        tabView = new TabView(context);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.load_image3));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.load_image2));
        tabView.setTopTitle("扫一扫");
        tabView.setBottomTitle("扫一扫");
        tabView.setTopTitleColor(Color.parseColor("#333333"));
        tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
        tabView.setBackgroundColor(Color.GREEN);
        tabView.init();
        componentScanCode.targetTypeView = tabView;

        componentDepots.add(componentPic);
        componentDepots.add(componentScanCode);


        //用户自定义控件
        coordinateViews = new ArrayList<>();
        //返回按钮
        leftDefault = new CoordinateView();
        tabView = new TabView(context);
        tabView.setResponseMode(0);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.bar_black_back));
        tabView.setClickable(true);
        tabView.init();
        leftDefault.view = tabView;

        //相册按钮
        rightDefault = new CoordinateView();
        tabView = new TabView(context);
        tabView.setResponseMode(0);
        tabView.setTopDrawable(getDrawableFromSource(R.drawable.album));
        tabView.setBottomDrawable(getDrawableFromSource(R.drawable.album));
        tabView.setClickable(true);
        tabView.init();
        rightDefault.view = tabView;

        coordinateViews.add(leftDefault);
        coordinateViews.add(rightDefault);

    }

    public void setComponentDepots(List<ComponentDepot> componentDepots) {
        this.componentDepots = componentDepots;
    }

    public void setCoordinateViews(List<CoordinateView> coordinateViews) {
        this.coordinateViews = coordinateViews;
    }

    public String getSnakePosition() {
        return snakePosition;
    }

    public void setSnakePosition(String snakePosition) {
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
        cameraHelper.setFocusMode(focusMode);
    }

    //初始化控件
    private void initViews(){
        //初始化surfaceView
        surfaceView = new SurfaceView(context);
        //初始化相机选择导航按钮snakeBar
        snakeBar = new CustomSnakeBar<>(context);
        snakeBar.setMidPaddingLeft(snakePaddingLeft);
        snakeBar.setMidPaddingTop(snakePaddingTop);
        snakeBar.setMidPaddingRight(snakePaddingRight);
        snakeBar.setMidPaddingBottom(snakePaddingBottom);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snakeBar.setLayoutParams(params);
        snakeBar.setGapHorizontal(50);
        List<View> views = new ArrayList<>();
        if (componentDepots != null){
            for (ComponentDepot depot:componentDepots){
                views.add(depot.targetTypeView);
            }
        }
        snakeBar.addChildren(views);
        snakeBar.setBackgroundColor(Color.YELLOW);
        snakeBar.init();
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
            for (ComponentDepot depot:componentDepots){
                this.addView(depot.centerShape);
                this.addView(depot.centerButton);
                this.addView(depot.bottomButton);
            }
        }
        //添加用户自定view
        if (coordinateViews != null){
            for (CoordinateView view:coordinateViews){
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

    /**
     * 带坐标的View，坐标用于指定View的摆放位置
     */
    public static class CoordinateView{
        public int x;
        public int y;
        public View view;
    }

    /**
     * 固定的相机元件仓库
     * 每个仓库对于了相机的一个功能，如拍照、扫码
     * 他是和相机选择导航按钮snakeBar相关联的
     */
    public static class ComponentDepot{
        public View centerShape;     //相机中间的一个形状，如扫码框
        public View centerButton;    //相机中间的一个Button,覆盖在centerShape的上层
        public View bottomButton;    //相机底部按钮，如拍照的按钮
        public View targetTypeView;  //与snakeBar对应的按钮
    }

    public SizeChangedListener getSizeChangedListener() {
        return sizeChangedListener;
    }

    public interface SizeChangedListener{
        public void onSizeChanged(int width,int height);
    }
}
