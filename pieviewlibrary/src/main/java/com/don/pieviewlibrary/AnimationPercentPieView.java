package com.don.pieviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * Description：带动画的百分比数据类型饼状统计图
 * </p>
 *
 * @author tangzhijie
 */
public class AnimationPercentPieView extends View {

    /**
     * 绘制扇形的画布
     */
    private Canvas mCanvas;
    private Bitmap mBitmap;

    /**
     * 扇形位置等数据集合
     */
    private List<ArcInfo> positionList;

    /**
     * 动画绘制扇形时的当前度数进度
     */
    private int currentAngle = 0;

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
     * 中间文本的大小
     */
    private Rect centerTextBound = new Rect();

    /**
     * 数据文本的大小
     */
    private Rect dataTextBound = new Rect();

    /**
     * 扇形画笔
     */
    private Paint mArcPaint;

    /**
     * 中心文本画笔
     */
    private Paint centerTextPaint;

    /**
     * 数据画笔
     */
    private Paint dataPaint;

    /**
     * 数据源数字数组
     */
    private int[] numbers;

    /**
     * 数据源名称数组
     */
    private String[] names;

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
     * 中间字体大小
     */
    private float centerTextSize = 80;

    /**
     * 数据字体大小
     */
    private float dataTextSize = 30;

    /**
     * 中间字体颜色
     */
    private int centerTextColor = Color.BLACK;

    /**
     * 数据字体颜色
     */
    private int dataTextColor = Color.BLACK;

    /**
     * 圆圈的宽度
     */
    private float circleWidth = 100;

    //自定义属性 End

    public AnimationPercentPieView(Context context) {
        super(context);
        init();
    }

    public AnimationPercentPieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationPercentPieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView);
        centerTextSize = typedArray.getDimension(R.styleable.PieView_centerTextSize, centerTextSize);
        dataTextSize = typedArray.getDimension(R.styleable.PieView_dataTextSize, dataTextSize);
        circleWidth = typedArray.getDimension(R.styleable.PieView_circleWidth, circleWidth);
        centerTextColor = typedArray.getColor(R.styleable.PieView_centerTextColor, centerTextColor);
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

        centerTextPaint = new Paint();
        centerTextPaint.setTextSize(centerTextSize);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setColor(centerTextColor);

        dataPaint = new Paint();
        dataPaint.setStrokeWidth(2);
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
        //创建绘制使用的画布
        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (positionList != null && positionList.size() > 0) {
            canvas.drawBitmap(mBitmap, 0, 0, null);

            for (int i = 0; i < positionList.size(); i++) {
                ArcInfo arcInfo = positionList.get(i);
                //判断当前进度所属的扇形段，并根据相关颜色绘制
                if (currentAngle >= arcInfo.getStartAngle() && currentAngle <= arcInfo.getStartAngle() + arcInfo.getAngle()) {
                    drawArc(mCanvas, currentAngle, 1, arcInfo.getColor());
                    break;
                }
            }

            currentAngle++;
            if (currentAngle < 360) {
                invalidate();
            } else {//绘制扇形完毕，绘制数据
                for (int i = 0; i < positionList.size(); i++) {
                    ArcInfo arcInfo = positionList.get(i);
                    //绘制第i段扇形对应的数据
                    drawData(mCanvas, arcInfo.getCenterAngle(), i, arcInfo.getPercent());
                    //绘制中心数字总和
                    mCanvas.drawText(sum + "", centerX - centerTextBound.width() / 2, centerY + centerTextBound.height() / 2, centerTextPaint);
                }
            }
        }
    }

    /**
     * 计算每段扇形角度，中心点，百分比
     */
    private void calculateArc() {
        if (numbers == null || numbers.length == 0) {
            return;
        }
        //每段扇形开始度数
        int startAngle = 0;
        //每段扇形所占度数
        float angle;
        //每个数据所占百分比
        float percent;
        positionList = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++) {
            percent = numbers[i] / (float) sum;
            //获取百分比在360中所占度数
            if (i == numbers.length - 1) {//保证所有度数加起来等于360
                angle = 360 - startAngle;
            } else {
                angle = (float) Math.ceil(percent * 360);
            }
            ArcInfo arcInfo = new ArcInfo();
            arcInfo.setStartAngle(startAngle);
            arcInfo.setAngle(angle);
            arcInfo.setPercent(percent);
            arcInfo.setColor(colors[i]);
            //下一段扇形开始的度数为之前扇形所占度数总和
            startAngle += angle;
            //当前弧线中心点相对于纵轴的夹角度数,由于扇形的绘制是从三点钟方向开始，所以加90
            float arcCenterDegree = 90 + startAngle - angle / 2;
            arcInfo.setCenterAngle(arcCenterDegree);
            positionList.add(arcInfo);
        }
    }

    /**
     * 计算每段弧度的中心坐标
     *
     * @param degree 当前扇形中心度数
     */
    private float[] calculateCenterByDegree(float degree) {
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
        float startX = calculateCenterByDegree(degree)[0];
        float startY = calculateCenterByDegree(degree)[1];

        //获取名称文本大小
        dataPaint.getTextBounds(names[i], 0, names[i].length(), dataTextBound);
        //绘制名称数据，20为纵坐标偏移量
        canvas.drawText(names[i],
                startX - dataTextBound.width() / 2,
                startY + dataTextBound.height() / 2 - 20,
                dataPaint);

        //拼接百分比并获取文本大小
        DecimalFormat df = new DecimalFormat("0.0");
        String percentString = df.format(percent * 100) + "%";
        dataPaint.getTextBounds(percentString, 0, percentString.length(), dataTextBound);

        //绘制百分比数据，20为纵坐标偏移量
        canvas.drawText(percentString,
                startX - dataTextBound.width() / 2,
                startY + dataTextBound.height() * 2 - 20,
                dataPaint);
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
        //-0.5和+0.5是为了让每个扇形之间没有间隙
        canvas.drawArc(rectF, startAngle - 0.5f, angle + 0.5f, false, mArcPaint);
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
     * @param names   名称数组
     */
    public void setData(int[] numbers, String[] names) {
        if (numbers == null || numbers.length == 0 || names == null || names.length == 0) {
            return;
        }
        if (numbers.length != names.length) {
            return;
        }
        this.numbers = numbers;
        this.names = names;
        colors = new int[numbers.length];
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
            //随机颜色
            colors[i] = randomColor();
        }
        //计算总和数字的宽高
        centerTextPaint.getTextBounds(sum + "", 0, (sum + "").length(), centerTextBound);
        //计算绘制所需信息
        calculateArc();
        invalidate();
    }

    /**
     * 设置数据(自定义颜色)
     *
     * @param numbers 数字数组
     * @param names   名称数组
     * @param colors  颜色数组
     */
    public void setData(int[] numbers, String[] names, int[] colors) {
        if (numbers == null || numbers.length == 0
                || names == null || names.length == 0
                || colors == null || colors.length == 0) {
            return;
        }
        if (numbers.length != names.length || numbers.length != colors.length) {
            return;
        }
        this.numbers = numbers;
        this.names = names;
        this.colors = colors;
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
        }
        //计算总和数字的宽高
        centerTextPaint.getTextBounds(sum + "", 0, (sum + "").length(), centerTextBound);
        //计算绘制所需信息
        calculateArc();
        invalidate();
    }

    /**
     * 存储每段扇形的角度，中心角度，百分比，颜色
     */
    class ArcInfo {
        //开始角度
        float startAngle;
        //所占角度
        float angle;
        //中心角度
        float centerAngle;
        //所占百分比
        float percent;
        //颜色
        int color;

        float getCenterAngle() {
            return centerAngle;
        }

        void setCenterAngle(float centerAngle) {
            this.centerAngle = centerAngle;
        }

        float getStartAngle() {
            return startAngle;
        }

        void setStartAngle(float startAngle) {
            this.startAngle = startAngle;
        }

        float getAngle() {
            return angle;
        }

        void setAngle(float angle) {
            this.angle = angle;
        }

        float getPercent() {
            return percent;
        }

        void setPercent(float percent) {
            this.percent = percent;
        }

        int getColor() {
            return color;
        }

        void setColor(int color) {
            this.color = color;
        }
    }
}
