package com.example.cameratest.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;

import com.example.cameratest.R;

/**
 *  @Author wonderful
 *  @Date 2019-8-30
 *  update to 1.1 2020-5-14
 *  update to 1.2 2020-5-16
 *  @Version 1.2
 *  @Description 自定义view,图标 + 文字的组合view，目前只支持上图片/下文字的模式
 *  可设置选中状态，实现选择器效果
 *
 *  TODO 这个控件使用起来仍然有些不方便，这是由于图片大小和父控件的宽高不一致导致的，
 *   当图片大于父控件宽高时往往效果很差
 *   在后面我们将使用图片缩放技术自动调整图片大小来进行适配
 */

public class TabView extends RelativeLayout {

    private static final String TAG = "TabView";

    private Context context;

    private boolean isChecked;

    /**
     * 响应模式 0：选择模式 ~0：点击模式，默认为选择模式
     */
    private int responseMode;

    /**底部标题**/
    private TextView bottomTextView;
    private int bottomTitleSize;
    private int bottomTitleColor;
    private String bottomTitle;
    private static final int bottomTextViewId = 10000;

    /**顶部标题**/
    private TextView topTextView;
    private int topTitleSize;
    private int topTitleColor;
    private String topTitle;
    private static final int topTextViewId = 10001;

    /**底部图标**/
    private ImageView bottomImage;
    private Drawable bottomDrawable;
    private static final int bottomImageId = 10010;

    /**顶部图标**/
    private ImageView topImage;
    private Drawable topDrawable;
    private static final int topImageId = 10011;

    /**
     * 在处理子控件的摆放时需要知道布局参数的宽高信息，
     * 但是布局参数不能马上get到，我们有两种策略获取，
     * 外接提供和内部在measure的时候获取，这是一个标记
     */
    private boolean isSizeModeInit = false;

    public TabView(Context context) {
        super(context);
        init(context,null);
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        initAttrs(context,attrs);
        initView();
        initData();
    }

    private void initAttrs(Context context,AttributeSet attrs){
        this.context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulTabStyle);

        isChecked = typedArray.getBoolean(R.styleable.wonderfulTabStyle_isChecked,false);

        bottomTitleSize = typedArray.getDimensionPixelSize(R.styleable.wonderfulTabStyle_bottomTitleSize, 14);
        bottomTitleColor = typedArray.getColor(R.styleable.wonderfulTabStyle_bottomTitleColor,Color.GRAY);
        bottomTitle = typedArray.getString(R.styleable.wonderfulTabStyle_bottomTitle);

        topTitleSize = typedArray.getDimensionPixelSize(R.styleable.wonderfulTabStyle_topTitleSize, 14);
        topTitleColor = typedArray.getColor(R.styleable.wonderfulTabStyle_topTitleColor,Color.BLACK);
        topTitle = typedArray.getString(R.styleable.wonderfulTabStyle_topTitle);

        bottomDrawable = typedArray.getDrawable(R.styleable.wonderfulTabStyle_bottomDrawable);
        topDrawable = typedArray.getDrawable(R.styleable.wonderfulTabStyle_topDrawable);

        responseMode = typedArray.getInt(R.styleable.wonderfulTabStyle_responseMode,0);
        typedArray.recycle();
    }

    /**
     * 发现组合的时候WRAP_CONTENT有些bug，需要自己处理一下测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //如果之前没有获取到布局参数的宽高信息，在这里获取，重新初始化
        if (!isSizeModeInit){
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            init(layoutParams.width,layoutParams.height);
        }
    }

    //获取图片和文字的总高
    private int getHeightAT_MOST(){
        int imageHeight = 0;
        int textHeight = 0;
        //获取图片高
        if (bottomDrawable != null){
            imageHeight = bottomDrawable.getIntrinsicHeight();
        }
        if (topDrawable != null){
            int height = topDrawable.getIntrinsicHeight();
            imageHeight = Math.max(imageHeight,height);
        }

        //获取文字高
        Paint paint = new Paint();
        Rect rect = new Rect();
        if(bottomTitle != null){
            paint.getTextBounds(bottomTitle,0,bottomTitle.length(),rect);
            textHeight = rect.height();
        }
        if(topTitle != null){
            paint.getTextBounds(topTitle,0,topTitle.length(),rect);
            int height = rect.height();
            textHeight = Math.max(textHeight,height);
        }

        return textHeight + imageHeight;
    }

    //获取图片和文字的最大宽度
    private int getWidthAT_MOST(){
        int imageWidth = 0;
        int textWidth = 0;
        //获取图片宽
        if (bottomDrawable != null){
            imageWidth = bottomDrawable.getIntrinsicWidth();
        }
        if (topDrawable != null){
            int width = topDrawable.getIntrinsicWidth();
            imageWidth = Math.max(imageWidth,width);
        }

        //获取文字高
        Paint paint = new Paint();
        Rect rect = new Rect();
        if(bottomTitle != null){
            paint.getTextBounds(bottomTitle,0,bottomTitle.length(),rect);
            textWidth = rect.width();
        }
        if(topTitle != null){
            paint.getTextBounds(topTitle,0,topTitle.length(),rect);
            int width = rect.width();
            textWidth = Math.max(textWidth,width);
        }

        return Math.max(textWidth,imageWidth);
    }

    //创建图片容器和文字容器，并初始化他们的位置
    private void initView(int width,int height){

        isSizeModeInit = true;

        removeAllViews();

        LayoutParams params;
        int centerId = -1;

        /**
         * 先创建并添加容器
         */
        initView();
        /**
         * 再指定容器的位置
         */
        //指定控件居中
        setGravity(Gravity.CENTER);
        //并且文字和图片内容都不为空，将文字容器置于下方，将图片容器置于上方
        if((bottomTitle != null || topTitle != null) && (bottomDrawable !=null || topDrawable != null)){
            if(bottomDrawable != null){
                centerId = bottomImageId;
            }else {
                centerId = topImageId;
            }

            //先设置图片容器的位置，WRAP_CONTENT需要区别处理
            //TODO 顺序不要颠倒，否则会出bug，可能是由于imageView的特性导致的
            if (height == LayoutParams.WRAP_CONTENT){
                params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                bottomImage.setLayoutParams(params);

                params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                topImage.setLayoutParams(params);
            }else {
                params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                bottomImage.setLayoutParams(params);

                params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                topImage.setLayoutParams(params);
            }

            //设置文字容器的位置
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW,centerId);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            bottomTextView.setLayoutParams(params);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW,centerId);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            topTextView.setLayoutParams(params);
        }
        //否则全部置中
        else {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            bottomTextView.setLayoutParams(params);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            topTextView.setLayoutParams(params);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            bottomImage.setLayoutParams(params);

            params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            topImage.setLayoutParams(params);
        }
    }

    //创建图片容器和文字容器
    private void initView(){
        /**
         * 先创建并添加容器
         */
        //创建并添加底部文字容器
        bottomTextView = new TextView(context);
        bottomTextView.setId(bottomTextViewId);
        //TODO 扩展
        bottomTextView.setPadding(0,0,0,0);
        bottomTextView.setGravity(Gravity.CENTER);
        this.addView(bottomTextView);

        //创建并添加顶部文字容器
        topTextView = new TextView(context);
        topTextView.setId(topTextViewId);
        //TODO 扩展
        topTextView.setPadding(0,0,0,0);
        bottomTextView.setGravity(Gravity.CENTER);
        this.addView(topTextView);

        //创建并添加底部图片容器
        bottomImage = new ImageView(context);
        bottomImage.setId(bottomImageId);
        //TODO 扩展
        bottomImage.setPadding(0,0,0,0);
        bottomTextView.setGravity(Gravity.CENTER);
        this.addView(bottomImage);

        //创建并添加顶部图片容器
        topImage = new ImageView(context);
        topImage.setId(topImageId);
        //TODO 扩展
        topImage.setPadding(0,0,0,0);
        bottomTextView.setGravity(Gravity.CENTER);
        this.addView(topImage);
    }

    private void initData(){
        if (bottomTextView != null){
            bottomTextView.setTextColor(bottomTitleColor);
            bottomTextView.setTextSize(bottomTitleSize);
            bottomTextView.setText(bottomTitle);
        }

        if(topTextView != null){
            topTextView.setTextColor(topTitleColor);
            topTextView.setTextSize(topTitleSize);
            topTextView.setText(topTitle);
        }

        if(bottomImage != null){
            bottomImage.setImageDrawable(bottomDrawable);
        }

        if(topImage != null){
            topImage.setImageDrawable(topDrawable);
        }

        if(isChecked){
            alphaChange(1.0f);
        }else {
            alphaChange(0.0f);
        }
    }

    public void alphaChange(float alpha){
        topTextView.setAlpha(alpha);
        topImage.setAlpha(alpha);
        bottomTextView.setAlpha(1-alpha);
        bottomImage.setAlpha(1-alpha);
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
        if(isChecked){
            alphaChange(1.0f);
        }else {
            alphaChange(0.0f);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (responseMode == 0)return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setChecked(true);
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            setChecked(false);
        }
        return super.onTouchEvent(event);
    }

    public boolean isChecked(){
        return isChecked;
    }

    public int getResponseMode() {
        return responseMode;
    }

    public void setResponseMode(int responseMode) {
        this.responseMode = responseMode;
    }

    public TextView getBottomTextView() {
        return bottomTextView;
    }

    public void setBottomTextView(TextView bottomTextView) {
        this.bottomTextView = bottomTextView;
    }

    public int getBottomTitleSize() {
        return bottomTitleSize;
    }

    public void setBottomTitleSize(int bottomTitleSize) {
        this.bottomTitleSize = bottomTitleSize;
    }

    public int getBottomTitleColor() {
        return bottomTitleColor;
    }

    public void setBottomTitleColor(int bottomTitleColor) {
        this.bottomTitleColor = bottomTitleColor;
    }

    public String getBottomTitle() {
        return bottomTitle;
    }

    public void setBottomTitle(String bottomTitle) {
        this.bottomTitle = bottomTitle;
    }

    public static int getBottomTextViewId() {
        return bottomTextViewId;
    }

    public TextView getTopTextView() {
        return topTextView;
    }

    public void setTopTextView(TextView topTextView) {
        this.topTextView = topTextView;
    }

    public int getTopTitleSize() {
        return topTitleSize;
    }

    public void setTopTitleSize(int topTitleSize) {
        this.topTitleSize = topTitleSize;
    }

    public int getTopTitleColor() {
        return topTitleColor;
    }

    public void setTopTitleColor(int topTitleColor) {
        this.topTitleColor = topTitleColor;
    }

    public String getTopTitle() {
        return topTitle;
    }

    public void setTopTitle(String topTitle) {
        this.topTitle = topTitle;
    }

    public Drawable getBottomDrawable() {
        return bottomDrawable;
    }

    public void setBottomDrawable(Drawable bottomDrawable) {
        this.bottomDrawable = bottomDrawable;
    }

    public Drawable getTopDrawable() {
        return topDrawable;
    }

    public void setTopDrawable(Drawable topDrawable) {
        this.topDrawable = topDrawable;
    }

    /**
     * 初始化，动态调用
     */
    public void init(){
        initView();
    }

    /**
     * 初始化，动态调用，这个方法可以接收外界的参数，
     * 但是请注意，这里的宽高仅仅是想知道外界设置了什么样的布局参数
     */
    public void init(int width,int height){
        initView(width,height);
        initData();
    }

}
