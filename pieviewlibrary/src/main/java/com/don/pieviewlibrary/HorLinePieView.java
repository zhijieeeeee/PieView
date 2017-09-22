package com.don.pieviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * <p>
 * Description：横线数据类型饼状统计图
 * </p>
 *
 * @author tangzhijie
 */
public class HorLinePieView extends View {

    /**
     * 横线长度
     */
    private static final int HOR_LINE_LENGTH = 100;

    /**
     * 横线上文字的横向偏移量
     */
    private static final int X_OFFSET = 10;

    /**
     * 中心坐标
     */
    private int centerX;
    private int centerY;

    /**
     * 半径
     */
    private float radius;

    /**
     * 弧形外接矩形
     */
    private RectF rectF;

    /**
     * 数据文本的大小
     */
    private Rect dataTextBound = new Rect();

    /**
     * 扇形画笔
     */
    private Paint mArcPaint;

    /**
     * 横线画笔
     */
    private Paint horLinePaint;

    /**
     * 数据画笔
     */
    private Paint dataPaint;

    /**
     * 数据源数字数组
     */
    private int[] numbers;

    /**
     * 数据源总和
     */
    private int sum;

    /**
     * 颜色数组
     */
    private int[] colors;

    private Random random = new Random();

    //自定义属性 Start

    /**
     * 数据字体大小
     */
    private float dataTextSize = 40;

    /**
     * 数据字体颜色
     */
    private int dataTextColor = Color.BLACK;

    /**
     * 横线颜色
     */
    private int horLineColor = Color.GRAY;

    /**
     * 圆圈的宽度
     */
    private float circleWidth = 50;

    //自定义属性 End

    public HorLinePieView(Context context) {
        super(context);
        init();
    }

    public HorLinePieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorLinePieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView);
        dataTextSize = typedArray.getDimension(R.styleable.PieView_dataTextSize, dataTextSize);
        circleWidth = typedArray.getDimension(R.styleable.PieView_circleWidth, circleWidth);
        dataTextColor = typedArray.getColor(R.styleable.PieView_dataTextColor, dataTextColor);
        typedArray.recycle();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setStrokeWidth(circleWidth);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);

        horLinePaint = new Paint();
        horLinePaint.setStrokeWidth(3);
        horLinePaint.setAntiAlias(true);
        horLinePaint.setColor(horLineColor);

        dataPaint = new Paint();
        dataPaint.setTextSize(dataTextSize);
        dataPaint.setAntiAlias(true);
        dataPaint.setColor(dataTextColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (measureWidthMode == MeasureSpec.AT_MOST
                && measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Constant.DEFAULT_WIDTH, Constant.DEFAULT_HEIGHT);
        } else if (measureWidthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Constant.DEFAULT_WIDTH, measureHeightSize);
        } else if (measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidthSize, Constant.DEFAULT_HEIGHT);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        //设置半径为宽高最小值的1/4
        radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 4;
        //设置扇形外接矩形
        rectF = new RectF(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateAndDraw(canvas);
    }

    /**
     * 计算比例并且绘制扇形和数据
     */
    private void calculateAndDraw(Canvas canvas) {
        if (numbers == null || numbers.length == 0) {
            return;
        }
        //扇形开始度数
        int startAngle = 0;
        //所占百分比
        float percent;
        //所占度数
        float angle;
        for (int i = 0; i < numbers.length; i++) {
            percent = numbers[i] / (float) sum;
            //获取百分比在360中所占度数
            if (i == numbers.length - 1) {//保证所有度数加起来等于360
                angle = 360 - startAngle;
            } else {
                angle = (float) Math.ceil(percent * 360);
            }
            //绘制第i段扇形
            drawArc(canvas, startAngle, angle, colors[i]);
            startAngle += angle;

            //绘制数据
            if (numbers[i] <= 0) {
                continue;
            }
            //当前扇形弧线相对于纵轴的中心点度数,由于扇形的绘制是从三点钟方向开始，所以加90
            float arcCenterDegree = 90 + startAngle - angle / 2;
            drawData(canvas, arcCenterDegree, i, percent);
        }
        //绘制中心数字总和
//        canvas.drawText(sum + "", centerX - centerTextBound.width() / 2, centerY + centerTextBound.height() / 2, centerTextPaint);
    }

    /**
     * 计算每段弧度的中心坐标
     *
     * @param degree 当前扇形中心度数
     */
    private float[] calculatePosition(float degree) {
        //由于Math.sin(double a)中参数a不是度数而是弧度，所以需要将度数转化为弧度
        //而Math.toRadians(degree)的作用就是将度数转化为弧度
        //sin 一二正，三四负 sin（180-a）=sin(a)
        //扇形弧线中心点距离圆心的x坐标
        float x = (float) (Math.sin(Math.toRadians(degree)) * radius);
        //cos 一四正，二三负
        //扇形弧线中心点距离圆心的y坐标
        float y = (float) (Math.cos(Math.toRadians(degree)) * radius);

        //每段弧度的中心坐标(扇形弧线中心点相对于view的坐标)
        float startX = centerX + x;
        float startY = centerY - y;

        float[] position = new float[2];
        position[0] = startX;
        position[1] = startY;
        return position;
    }

    /**
     * 绘制数据
     *
     * @param canvas  画布
     * @param degree  第i段弧线中心点相对于纵轴的夹角度数
     * @param i       第i段弧线
     * @param percent 数据百分比
     */
    private void drawData(Canvas canvas, float degree, int i, float percent) {
        //弧度中心坐标
        float startX = calculatePosition(degree)[0];
        float startY = calculatePosition(degree)[1];
        //横线结束坐标
        float horEndX = 0;
        float horEndY = 0;
        //数字开始坐标
        float numberStartX = 0;
        float numberStartY = 0;

        //拼接百分比并获取文本大小
        DecimalFormat df = new DecimalFormat("0.0");
        String percentString = df.format(percent * 100) + "%";
        dataPaint.getTextBounds(percentString, 0, percentString.length(), dataTextBound);

        //根据角度判断象限，并且计算各个坐标点
        if ((degree > 90 && degree <= 180) || degree > 360) {//一，二象限
            horEndX = startX + HOR_LINE_LENGTH;
            horEndY = startY;

            numberStartX = horEndX + X_OFFSET;
            numberStartY = horEndY + dataTextBound.height() / 2;

        } else if (degree > 180 && degree <= 360) {//三，四象限
            horEndX = startX - HOR_LINE_LENGTH;
            horEndY = startY;

            numberStartX = horEndX - X_OFFSET - dataTextBound.width();
            numberStartY = horEndY + dataTextBound.height() / 2;

        }
        //绘制横线
        canvas.drawLine(startX, startY, horEndX, horEndY, horLinePaint);
        //绘制数字
        canvas.drawText(percentString, numberStartX, numberStartY, dataPaint);
    }

    /**
     * 绘制扇形
     *
     * @param canvas     画布
     * @param startAngle 开始度数
     * @param angle      扇形的度数
     * @param color      颜色
     */
    private void drawArc(Canvas canvas, float startAngle, float angle, int color) {
        mArcPaint.setColor(color);
        //+0.5是为了让每个扇形之间没有间隙
        if (angle != 0) {
            angle += 0.5f;
        }
        canvas.drawArc(rectF, startAngle, angle, false, mArcPaint);
    }

    /**
     * 生成随机颜色
     */
    private int randomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    /**
     * 设置数据(使用随机颜色)
     *
     * @param numbers 数字数组
     */
    public void setData(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return;
        }
        this.numbers = numbers;
        colors = new int[numbers.length];
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
            //随机颜色
            colors[i] = randomColor();
        }
        invalidate();
    }

    /**
     * 设置数据(自定义颜色)
     *
     * @param numbers 数字数组
     * @param colors  颜色数组
     */
    public void setData(int[] numbers, int[] colors) {
        if (numbers == null || numbers.length == 0
                || colors == null || colors.length == 0) {
            return;
        }
        if (numbers.length != colors.length) {
            return;
        }
        this.numbers = numbers;
        this.colors = colors;
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
        }
        invalidate();
    }
}
