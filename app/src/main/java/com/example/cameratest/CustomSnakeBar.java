package com.example.cameratest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import java.util.List;

/**
 *  @Author wonderful
 *  @Date 2020-5-14
 *  @Version 1.0
 *  @Description 自定义SnakeBar，类似导航栏的功能,结合TabView可实现强大的功能
 */
public class CustomSnakeBar<T> extends LinearLayout {

    private static final String TAG = "CustomSnakeBar";

    private boolean enableLeftButton; //左按钮开关
    private boolean enableRightButton;//右边按钮开关
    private int gapHorizontal;        //中间按钮间隔

    private int midItemWidth;         //单个中间按钮的宽

    private int leftButtonPaddingLeft;
    private int leftButtonPaddingRight;
    private int leftButtonPaddingTop;
    private int leftButtonPaddingBottom;
    private int leftButtonPadding;

    private int rightButtonPaddingLeft;
    private int rightButtonPaddingRight;
    private int rightButtonPaddingTop;
    private int rightButtonPaddingBottom;
    private int rightButtonPadding;

    private int midPaddingLeft;
    private int midPaddingRight;
    private int midPaddingTop;
    private int midPaddingBottom;
    private int midPadding;

    private Object leftButton;
    private Object rightButton;
    private List<T> viewList;

    private Context context;

    private OnItemClickListener onItemClickListener;
    private LeftClickListener leftClickListener;
    private RightClickListener rightClickListener;
    private int choosePosition = -1;

    /**
     * 响应模式 0：选择模式 ~0：点击模式，默认为点击模式
     * LeftButton/RightButton
     */
    private int responseMode;

    /**
     * 响应模式 0：选择模式 ~0：点击模式，默认为选择模式
     * midButton
     */
    private int responseModeMid;

    public CustomSnakeBar(Context context) {
        this(context,null);
    }

    public CustomSnakeBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomSnakeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.wonderfulSnakeStyle);
        enableLeftButton = typedArray.getBoolean(R.styleable.wonderfulSnakeStyle_enableLeftButton,false);
        enableRightButton = typedArray.getBoolean(R.styleable.wonderfulSnakeStyle_enableRightButton,false);
        gapHorizontal = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_gapHorizontal,-1);

        midItemWidth = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_midItemWidth,100);

        leftButtonPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,10);
        leftButtonPaddingRight = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,10);
        leftButtonPaddingTop = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        leftButtonPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        leftButtonPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,-1);

        rightButtonPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,10);
        rightButtonPaddingRight = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,10);
        rightButtonPaddingTop = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        rightButtonPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        rightButtonPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,-1);

        midPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        midPaddingRight = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        midPaddingTop = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        midPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,0);
        midPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulSnakeStyle_leftButtonPaddingLeft,-1);

        responseMode = typedArray.getInt(R.styleable.wonderfulSnakeStyle_responseModeEdge,1);

        responseModeMid = typedArray.getInt(R.styleable.wonderfulSnakeStyle_responseModeMid,0);

        typedArray.recycle();
    }


    /**
     * 添加子控件，此方法默认不开启左右按钮
     * @param viewList 按钮控件
     */
    public void addChildren(List<T> viewList){
        this.viewList = viewList;
    }

    /**
     * 添加子控件,此方法会默认开启左右按钮，
     * @param leftButton 左按钮，如果为null，则使用默认按钮
     * @param rightButton 右按钮，如果为null，则使用默认按钮
     * @param viewList 中间按钮
     */
    public void addChildren(Object leftButton,Object rightButton,List<T> viewList){
        enableLeftButton = enableRightButton = true;
        this.leftButton = leftButton;
        this.rightButton = rightButton;
        this.viewList = viewList;
    }

    //启用左按钮，如果为null，则使用默认按钮
    public void enableLeftButton(Object leftButton){
        enableLeftButton = true;
        this.leftButton = leftButton;
    }

    //启用右按钮，如果为null，则使用默认按钮
    public void enableRightButton(Object rightButton){
        enableRightButton = true;
        this.rightButton = rightButton;
    }

    //初始化
    public void init(){
        //居中显示
        setGravity(Gravity.CENTER);
        //校验
        typeCheck(leftButton);
        typeCheck(rightButton);
        typeCheck(viewList.get(0));

        removeAllViews();

        //添加左按钮
        if(enableLeftButton){
            View left = createLeftButton();
            this.addView(left);
        }

        //添加中间按钮
        View mid = createCustomButtonLayout();
        this.addView(mid);

        //添加右边按钮
        if (enableRightButton){
            View right = createRightButton();
            this.addView(right);
        }
    }

    //校验，泛型只支持String和View类型
    private void typeCheck(Object object){
        if (object == null)return;
        if (!(object instanceof String) && !(object instanceof View)){
            throw new IllegalArgumentException("泛型的类型必须是String或View！");
        }
    }

    //构建左按钮
    private View createLeftButton(){
        View view = null;
        if (leftButton == null){
            TabView tabView = new TabView(context);
            tabView.setResponseMode(responseMode);
            tabView.setTopDrawable(getDrawable(R.drawable.bar_black_back));
            tabView.setBottomDrawable(getDrawable(R.drawable.bar_black_back));
            tabView.setClickable(true);
            tabView.init();
            view = tabView;
        }else if(leftButton instanceof String){
            TabView tabView = new TabView(context);
            tabView.setResponseMode(responseMode);
            tabView.setTopTitle((String) leftButton);
            tabView.setBottomTitle((String) leftButton);
            tabView.setTopTitleColor(Color.parseColor("#aaaaaa"));
            tabView.setBottomTitleColor(Color.parseColor("#333333"));
            tabView.setClickable(true);
            tabView.init();
            view = tabView;
        }else{
            view = (View) leftButton;
        }

        if (leftButtonPadding != -1){
            view.setPadding(leftButtonPadding,leftButtonPadding,leftButtonPadding,leftButtonPadding);
        }else {
            view.setPadding(leftButtonPaddingLeft,leftButtonPaddingTop,leftButtonPaddingRight,leftButtonPaddingBottom);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftClickListener != null){
                    leftClickListener.onLeftClick(v);
                }
            }
        });

        return view;
    }

    //构建右按钮
    private View createRightButton(){
        View view = null;
        if (rightButton == null){
            TabView tabView = new TabView(context);
            tabView.setResponseMode(responseMode);
            tabView.setTopDrawable(getDrawable(R.drawable.album));
            tabView.setBottomDrawable(getDrawable(R.drawable.album));
            tabView.setClickable(true);
            tabView.init();
            view = tabView;
        }else if(rightButton instanceof String){
            TabView tabView = new TabView(context);
            tabView.setResponseMode(responseMode);
            tabView.setTopTitle((String) rightButton);
            tabView.setBottomTitle((String) rightButton);
            tabView.setTopTitleColor(Color.parseColor("#aaaaaa"));
            tabView.setBottomTitleColor(Color.parseColor("#333333"));
            tabView.setClickable(true);
            tabView.init();
            view = tabView;
        }else{
            view = (View) rightButton;
        }

        if (rightButtonPadding != -1){
            view.setPadding(rightButtonPadding,rightButtonPadding,rightButtonPadding,rightButtonPadding);
        }else {
            view.setPadding(rightButtonPaddingLeft,rightButtonPaddingTop,rightButtonPaddingRight,rightButtonPaddingBottom);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightClickListener != null){
                    rightClickListener.onRightClick(v);
                }
            }
        });

        return view;
    }

    //构建中间按钮
    private CustomButtonLayout createCustomButtonLayout(){
        final CustomButtonLayout customButtonLayout = new CustomButtonLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        customButtonLayout.setLayoutParams(layoutParams);
        customButtonLayout.setGapHorizontal(gapHorizontal);
        if (midPadding != -1){
            customButtonLayout.setPadding(midPadding,midPadding,midPadding,midPadding);
        }else {
            customButtonLayout.setPadding(midPaddingLeft,midPaddingTop,midPaddingRight,midPaddingBottom);
        }
        customButtonLayout.removeAllViews();
        for (int i=0; i<viewList.size(); i++){
            Object object = viewList.get(i);
            final int finalI = i;
            //如果是String类型，则构建默认TabView
            if (object instanceof String){
                TabView tabView = new TabView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(midItemWidth,  ViewGroup.LayoutParams.WRAP_CONTENT);
                tabView.setLayoutParams(params);
                //设置模式-选择模式
                tabView.setResponseMode(responseModeMid);
                //设置文字
                tabView.setTopTitle((String) object);
                tabView.setBottomTitle((String) object);
                tabView.setTopTitleColor(Color.parseColor("#333333"));
                tabView.setBottomTitleColor(Color.parseColor("#aaaaaa"));
                //初始化
                tabView.setClickable(true);
                tabView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //如果是选择模式
                        if (responseModeMid == 0){
                            TabView view;
                            //先清除上次选中的
                            if (choosePosition != -1){
                                view = (TabView) customButtonLayout.getChildAt(choosePosition);
                                view.setChecked(false);
                            }
                            //设置当前选中的
                            view = (TabView) v;
                            view.setChecked(true);
                        }

                        choosePosition = finalI;
                        if (onItemClickListener != null){
                            onItemClickListener.onItemClick(v,finalI);
                        }
                    }
                });
                tabView.init();
                customButtonLayout.addView(tabView);
            }
            //如果是TabView，只设置监听事件
            else if(object instanceof TabView){
                TabView tabView = (TabView) object;
                tabView.setResponseMode(responseModeMid);
                tabView.setClickable(true);
                tabView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //如果是选择模式
                        if (responseModeMid == 0){
                            TabView view;
                            //先清除上次选中的
                            if (choosePosition != -1){
                                view = (TabView) customButtonLayout.getChildAt(choosePosition);
                                view.setChecked(false);
                            }
                            //设置当前选中的
                            view = (TabView) v;
                            view.setChecked(true);
                        }

                        choosePosition = finalI;
                        if (onItemClickListener != null){
                            onItemClickListener.onItemClick(v,finalI);
                        }
                    }
                });
                tabView.init();
                customButtonLayout.addView(tabView);
            }
            //如果是其他View
            else{
                View customView = (View) object;
                customView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePosition = finalI;
                        if (onItemClickListener != null){
                            onItemClickListener.onItemClick(v,finalI);
                        }
                    }
                });
                customButtonLayout.addView(customView);
            }
        }
        return customButtonLayout;
    }

    private Drawable getDrawable(int sourceId){
        return context.getResources().getDrawable(sourceId);
    }

    /**
     * 自定义按钮布局，为功能扩展而设计
     */
    private static final class CustomButtonLayout extends LinearLayout{

        private int gapHorizontal;//控件水平方向的间隔

        public CustomButtonLayout(Context context) {
            super(context);
            init();
        }

        public CustomButtonLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CustomButtonLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        //重新摆放子控件
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (gapHorizontal == -1){
                layoutAuto();
            }else {
                layoutWithCustomGap();
            }
        }

        /**
         * 在指定了控件间隔的情况下摆放控件
         * 按照指定的间隔，从中间向两边摆放
         */
        private void layoutWithCustomGap(){
            int childCount = getChildCount();
            if (childCount <= 0)return;

            View child;
            int left;
            int right;
            int top;
            int bottom;

            //只有一个控件，将其摆放在中间
            if (childCount == 1){
                child = getChildAt(0);
                left = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2;
                right = left + child.getMeasuredWidth();
                top = getPaddingTop();
                bottom = top + child.getMeasuredHeight();
                child.layout(left,top,right,bottom);
                return;
            }

            top = getPaddingTop();
            //计算最左边控件的位置
            int leftNum = childCount / 2;
            int childWidth = getChildAt(0).getMeasuredWidth();

            //偶数
            if (childCount % 2 ==0){
                left = getMeasuredWidth() / 2 - gapHorizontal / 2 - childWidth - (childWidth + gapHorizontal) * (leftNum - 1);
            }
            //基数
            else {
                left = getMeasuredWidth() / 2 - childWidth / 2 - (childWidth + gapHorizontal) * leftNum;
            }

            //从左往右摆放
            for (int i=0; i<childCount; i++){
                child = getChildAt(i);
                right = left + childWidth;
                bottom = top + child.getMeasuredHeight();
                child.layout(left,top,right,bottom);
                left += childWidth + gapHorizontal;
            }
        }

        /**
         * gapHorizontal = -1的情况下，自动处理控件间的间隔，
         */
        private void layoutAuto(){
            //如果没有任何子控件直接返回
            int childrenCount = getChildCount();
            if (childrenCount <=0)return;
            //获取布局的宽
            int parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            //获取所有子控件的总宽
            int childrenWidth = getChildAt(0).getMeasuredWidth() * childrenCount;

            View child;
            int left;
            int right;
            int top;
            int bottom;

            //如果只有一个控件，将其摆放在中间
            if (childrenCount == 1){
                child = getChildAt(0);
                left = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2;
                right = left + child.getMeasuredWidth();
                top = getPaddingTop();
                bottom = top + child.getMeasuredHeight();
                child.layout(left,top,right,bottom);
                return;
            }

            left = getPaddingLeft();
            top = getPaddingTop();
            //布局宽大于子控件总宽,自动计算控件间隔，均距摆放
            if (parentWidth >= childrenWidth){
                int gap = (parentWidth - childrenWidth) / (childrenCount - 1);
                for (int i=0; i<childrenCount; i++){
                    child = getChildAt(i);
                    right = left + child.getMeasuredWidth();
                    bottom = top + child.getMeasuredHeight();
                    child.layout(left,top,right,bottom);
                    left += child.getMeasuredWidth() + gap;
                }
            }
            //布局宽小于子控件总宽,获取能容纳（可见）的最大子控件数量并自动计算控件间隔，均距摆放
            else {
                //获取可见的子控件数量
                int maxNum = getMaxNum();
                if (maxNum == 0){
                    throw new IllegalArgumentException("单个子控件的宽超过了父控件的宽： " + getMeasuredWidth());
                }
                int gap;
                //只能显示一个控件
                if (maxNum == 1){
                    //计算间距
                    gap = (parentWidth - getChildAt(0).getMeasuredWidth()) / 2;
                    //计算左边不可见控件的数量
                    int leftNum = (childrenCount - maxNum)/ 2;
                    //计算最左边控件的起始位置
                    int childWidth = getChildAt(0).getMeasuredWidth();
                    left -= (getMeasuredWidth() / 2 - childWidth / 2) - (childWidth + gap) * leftNum;
                }else {
                    gap = (parentWidth - getChildAt(0).getMeasuredWidth() * maxNum) / (maxNum - 1);
                    int leftNum = (childrenCount - maxNum)/ 2;
                    left -= (getChildAt(0).getMeasuredWidth() + gap) * leftNum;
                }
                //从左往右摆放
                for (int i=0; i<childrenCount; i++){
                    child = getChildAt(i);
                    right = left + child.getMeasuredWidth();
                    bottom = top + child.getMeasuredHeight();
                    child.layout(left,top,right,bottom);
                    left += child.getMeasuredWidth() + gap;
                }
            }

        }

        //获取能容纳（可显示）的最大子控件数量
        private int getMaxNum(){
            int width = 0;
            int maxNum = 0;
            for (int i=0; i<getChildCount(); i++){
                maxNum ++;
                width += getChildAt(i).getMeasuredWidth();
                if (width > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()){
                    return maxNum - 1;
                }
            }
            return maxNum;
        }

        private void init(){
            gapHorizontal = -1;//默认为-1,即不开启
        }

        public void setGapHorizontal(int gapHorizontal) {
            this.gapHorizontal = gapHorizontal;
        }

        //TODO 处理滑动事件
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return super.onTouchEvent(event);
        }

        //TODO 处理拦截事件
        @Override
        public boolean onInterceptHoverEvent(MotionEvent event) {
            return super.onInterceptHoverEvent(event);
        }
    }

    public int getMidItemWidth() {
        return midItemWidth;
    }

    public void setMidItemWidth(int midItemWidth) {
        this.midItemWidth = midItemWidth;
    }

    public interface OnItemClickListener{
        public void onItemClick(View view,int position);
    }

    public interface LeftClickListener{
        public void onLeftClick(View view);
    }

    public interface RightClickListener{
        public void onRightClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setLeftClickListener(LeftClickListener leftClickListener) {
        this.leftClickListener = leftClickListener;
    }

    public void setRightClickListener(RightClickListener rightClickListener) {
        this.rightClickListener = rightClickListener;
    }
}
