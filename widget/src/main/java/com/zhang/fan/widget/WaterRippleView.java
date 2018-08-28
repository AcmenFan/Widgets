package com.zhang.fan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * @author zhangfan
 * @desc 按钮水波纹效果
 */

public class WaterRippleView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Paint mRipplePaint;
    private int rippleCount = 3;
    private float currentRadius;
    /**
     * 中间圆圈的画笔
     */
    private Paint mCenterCirclePaint;
    /**
     * 画笔是否为stroke模式（即线条）
     */
    private boolean stroke = false;
    /**
     * 波纹颜色
     */
    private int mWaveColor;
    private boolean mIsRunning;
    private int mStopCircleColor;
    private int mCenterCircleRadius;

    public WaterRippleView(Context context) {
        this(context, null);
    }

    public WaterRippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterRippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readXML(attrs, defStyleAttr);
        initRipplePaint();
        initCenterCirclePaint();
        initHolder();
    }

    /**
     * 读取xml属性
     *
     * @param attrs
     * @param defStyleAttr
     */
    private void readXML(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.WaterRippleView, 0, defStyleAttr);
        mWaveColor = typedArray.getColor(R.styleable.WaterRippleView_rippleColor, Color.RED);
        mStopCircleColor = typedArray.getColor(R.styleable.WaterRippleView_stopColor, Color.RED);
        stroke = typedArray.getBoolean(R.styleable.WaterRippleView_stroke, false);
        typedArray.recycle();
    }

    /**
     * 初始化中间圆的画笔
     */
    private void initCenterCirclePaint() {
        mCenterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterCirclePaint.setStyle(Paint.Style.FILL);
        mCenterCirclePaint.setColor(mStopCircleColor);
    }

    /**
     * 初始化波纹画笔
     */
    private void initRipplePaint() {
        mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRipplePaint.setStrokeWidth(3);
        mRipplePaint.setDither(true);
        if (stroke) {
            mRipplePaint.setStyle(Paint.Style.STROKE);
        } else {
            mRipplePaint.setStyle(Paint.Style.FILL);
        }
    }

    /**
     * 设置surfaceHolder的属性
     */
    private void initHolder() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setZOrderOnTop(true);//在顶部显示
        holder.setFormat(PixelFormat.TRANSPARENT);//设置surface为透明
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        mCenterCircleRadius = (int) (size / 2 * 0.8);
    }

    private Thread mDrawThread;

    /**
     * 开启波纹效果
     */
    public void start() {
        if (mIsRunning) {
            return;
        }
        mIsRunning = true;
        mDrawThread = new Thread(WaterRippleView.this);
        mDrawThread.start();
    }

    /**
     * 开关
     */
    public void toggle() {
        if (mIsRunning) {
            stop();
        } else {
            start();
        }
    }

    /**
     * 重置
     */
    public void reset() {
        mIsRunning = false;
        currentRadius = 0;
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清除掉上一次的画框。
        int measuredWidth = getMeasuredWidth();
        int cX = measuredWidth / 2;
        int cY = measuredWidth / 2;
        canvas.drawCircle(cX, cY, mCenterCircleRadius, mCenterCirclePaint);
        getHolder().unlockCanvasAndPost(canvas);
    }

    /**
     * 停止
     */
    public void stop() {
        reset();
        mDrawThread.interrupt();
    }

    /**
     * 是否正在执行
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * 暂停波纹效果
     */
    public void pause() {
        mIsRunning = false;
    }

    //设置水波纹颜色
    public void setColor(int color) {
        mWaveColor = color;
    }

    //是否画笔stroke
    public void setStroke(boolean stroke) {
        this.stroke = stroke;
    }

    public boolean isStroke() {
        return stroke;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        reset();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 获取当前波纹半径覆盖的波纹个数
     *
     * @param spaceRadius 波纹间隔
     * @return
     */
    private int getRippleLevel(int spaceRadius) {
        float index = (currentRadius - mCenterCircleRadius) / spaceRadius;
        return (int) index;//向下取整
    }

    /**
     * 根据半径得到alpha值
     *
     * @param radius
     * @return
     */
    private int getAlphaByRadius(float radius) {
        int alpha = (int) ((int) (255 * (1 - (radius - mCenterCircleRadius) / (float) (getMeasuredWidth() / 2 - mCenterCircleRadius))) * 0.6f);//走的百分比
        return alpha;
    }

    @Override
    public void run() {
        onStart();
        while (mIsRunning) {
            try {
                SurfaceHolder holder = getHolder();
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清除掉上一次的画框。
                int measuredWidth = getMeasuredWidth();
                int cX = measuredWidth / 2;
                int cY = measuredWidth / 2;
                int spaceRadius = (getMeasuredWidth() / 2 - mCenterCircleRadius) / rippleCount;
                int rippleLevel = getRippleLevel(spaceRadius);//当前半径处于哪个临界线内
                for (int i = 0; i <= rippleLevel; i++) {
                    float radius = currentRadius - spaceRadius * i;
                    mRipplePaint.setAlpha(getAlphaByRadius(radius));
                    canvas.drawCircle(cX, cY, radius, mRipplePaint);
                }
                canvas.drawCircle(cX, cY, mCenterCircleRadius, mCenterCirclePaint);
                getHolder().unlockCanvasAndPost(canvas);
                spread();
            } catch (Exception e) {
                break;
            }
        }
        onPause();
    }

    private void onStart() {
        mRipplePaint.setColor(mWaveColor);
        mCenterCirclePaint.setColor(mWaveColor);
    }

    private void onPause() {
        mRipplePaint.setColor(mStopCircleColor);
        mCenterCirclePaint.setColor(mStopCircleColor);
    }

    private void spread() {
        if (currentRadius == 0) {
            currentRadius = mCenterCircleRadius;
        }
        int measuredWidth = getMeasuredWidth();
        if (currentRadius < measuredWidth / 2) {//未触及边缘的时候，递加
            currentRadius += 0.6f;
        } else {//触及边缘则等于最近的那个波纹的值
            currentRadius = mCenterCircleRadius + (rippleCount - 1) * (getMeasuredWidth() / 2 - mCenterCircleRadius) / rippleCount;
        }
    }

}
