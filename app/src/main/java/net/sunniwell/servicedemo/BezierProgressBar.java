package net.sunniwell.servicedemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;

import net.sunniwell.servicedemo.Utils.Density;

public class BezierProgressBar extends View {


    //绘制波浪画笔
    private Paint wavePaint;
    //绘制波浪Path
    private Path wavePath;
    //波浪的宽度
    private float waveLength;
    //波浪的高度
    private float waveHeight;
    //波浪组的数量 一个波浪是一低一高
    private int waveNumber;
    //自定义View的波浪宽高
    private int waveDefaultSize;
    //自定义View的最大宽高 就是比波浪高一点
    private int waveMaxHeight;

    //测量后的View实际宽高
    private int waveActualSize;

    //当前进度值占总进度值的占比
    private float currentPercent;
    //当前进度值
    private float currentProgress;
    //进度的最大值
    private float maxProgress;
    //动画对象
    private WaveProgressAnimat waveProgressAnimat;
    //波浪平移距离
    private float moveDistance = 0;
    //圆形背景画笔
    private Paint circlePaint;
    //bitmap
    private Bitmap circleBitmap;
    //bitmap画布
    private Canvas bitmapCanvas;
    //波浪颜色
    private int wave_color;
    //圆形背景进度框颜色
    private int circle_bgcolor;
    //进度显示 TextView
    private TextView tv_progress;
    //进度条显示值监听接口
    private UpdateTextListener updateTextListener;
    //是否绘制双波浪线
    private boolean isCanvasSecond_Wave;
    //第二层波浪的颜色
    private int second_WaveColor;
    //第二层波浪的画笔
    private Paint secondWavePaint;


    public BezierProgressBar(Context context) {
        this(context,null);
    }

    public BezierProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BezierProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray =context.obtainStyledAttributes(attrs,R.styleable.BezierProgressBar);
        //获取波浪宽度 第二个参数，如果xml设置这个属性，则会取设置的默认值 也就是说xml没有指定wave_length这个属性，就会取默认值
        waveLength = typedArray.getDimension(R.styleable.BezierProgressBar_wave_length, Density.dip2px(context,80));
        //获取波浪高度
        waveHeight = typedArray.getDimension(R.styleable.BezierProgressBar_wave_height,Density.dip2px(context,15));
        //获取波浪颜色
        wave_color = typedArray.getColor(R.styleable.BezierProgressBar_wave_color,Color.parseColor("#ff7c9e"));
        //圆形背景颜色
        circle_bgcolor = typedArray.getColor(R.styleable.BezierProgressBar_circlebg_color,Color.GRAY);
        //当前进度
        currentProgress = typedArray.getFloat(R.styleable.BezierProgressBar_currentProgress,10);
        //最大进度
        maxProgress = typedArray.getFloat(R.styleable.BezierProgressBar_maxProgress,100);
        //第二层波浪的颜色
        second_WaveColor = typedArray.getColor(R.styleable.BezierProgressBar_second_color,Color.BLUE);
        //记得把TypedArray回收
        //程序在运行时维护了一个 TypedArray的池，程序调用时，会向该池中请求一个实例，用完之后，调用 recycle() 方法来释放该实例，从而使其可被其他模块复用。
        //那为什么要使用这种模式呢？答案也很简单，TypedArray的使用场景之一，就是上述的自定义View，会随着 Activity的每一次Create而Create，
        //因此，需要系统频繁的创建array，对内存和性能是一个不小的开销，如果不使用池模式，每次都让GC来回收，很可能就会造成OutOfMemory。
        //这就是使用池+单例模式的原因，这也就是为什么官方文档一再的强调：使用完之后一定 recycle,recycle,recycle
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        //设置自定义View的宽高
        waveDefaultSize = Density.dip2px(context,250);
        //设置自定义View的最大宽高
        waveMaxHeight = Density.dip2px(context,300);

        wavePath = new Path();
        wavePaint = new Paint();
        //设置波浪颜色
        wavePaint.setColor(wave_color);
        //设置抗锯齿
        wavePaint.setAntiAlias(true);
        //设置画笔为取交集模式
        wavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        //圆形背景初始化
        circlePaint = new Paint();
        //设置圆形背景颜色
        circlePaint.setColor(circle_bgcolor);
        //设置抗锯齿
        circlePaint.setAntiAlias(true);


        //wavePaint.setStyle(Paint.Style.FILL);
        //占比一开始设置为0
        currentPercent = 0;
        //进度条进度开始设置为0
        currentProgress = 0;
        //进度条的最大值设置为100
        maxProgress = 100;

        //实例化动画对象
        waveProgressAnimat = new WaveProgressAnimat();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //创建位图
        circleBitmap = Bitmap.createBitmap(waveActualSize,waveActualSize,Bitmap.Config.ARGB_8888);
        //以该bitmap为底创建一块画布
        bitmapCanvas = new Canvas(circleBitmap);
        //绘制圆形 半径设小一点，是为了能让波浪填充完整个圆形背景
        bitmapCanvas.drawCircle(waveActualSize/2,waveActualSize/2,waveActualSize/2-Density.dip2px(getContext(),8),circlePaint);
        //绘制波浪
        bitmapCanvas.drawPath(paintWavePath(),wavePaint);
        //裁剪图片
        canvas.drawBitmap(circleBitmap, 0, 0, null);
    }

    private Path paintWavePath(){
        //先重置Path
        wavePath.reset();
        //起始点移至(0,waveHeight) p0 -p1 的高度随着进度的变化而变化
        wavePath.moveTo(-moveDistance,(1-currentPercent)*waveActualSize);
        for(int i = 0; i< waveNumber * 2; i++){
            wavePath.rQuadTo(waveLength/2,-waveHeight,waveLength,0);
            wavePath.rQuadTo(waveLength/2,waveHeight,waveLength,0);
        }

        wavePath.lineTo(waveActualSize,waveActualSize);
        wavePath.lineTo(0,waveActualSize);
        wavePath.lineTo(0,(1-currentPercent)*waveActualSize);
        wavePath.close();
        return wavePath;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(waveDefaultSize,heightMeasureSpec);
        int width = measureSize(waveDefaultSize,widthMeasureSpec);
        //获取View的最短边的长度
        int minSize = Math.min(height,width);
        //把view改为正方形
        setMeasuredDimension(minSize,minSize);
        //waveActualSize是实际的宽高
        waveActualSize = minSize;
        //这里是调整波浪数量 就是View中能容下几个波浪 用到ceil就是一定让View完全能被波浪占满 为循环绘制做准备 分母越小就约精准
        waveNumber = (int) Math.ceil(Double.parseDouble(String.valueOf(waveActualSize / waveLength / 2)));
    }

    /**
     * 返回指定的值
     * @param defaultSize 默认的值
     * @param measureSpec 模式
     * @return
     */
    private int measureSize(int defaultSize,int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        //View.MeasureSpec.EXACTLY：如果是match_parent 或者设置定值
        //View.MeasureSpec.AT_MOST：wrap_content
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    public class WaveProgressAnimat extends Animation {


        //在绘制动画的过程中会反复的调用applyTransformation函数，
        // 每次调用参数interpolatedTime值都会变化，该参数从0渐 变为1，当该参数为1时表明动画结束
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t){
            super.applyTransformation(interpolatedTime, t);
            //波浪高度达到最高就不用循环，只需要平移
            if(currentPercent < currentProgress / maxProgress){
                currentPercent = interpolatedTime * currentProgress / maxProgress;
                //这里直接根据进度值显示
                tv_progress.setText(updateTextListener.updateText(interpolatedTime,currentProgress,maxProgress));
            }
            //左边的距离
            moveDistance = interpolatedTime * waveNumber * waveLength * 2;
            //重新绘制
            invalidate();

        }
    }

    /**
     * 设置进度条数值
     * @param currentProgress 当前进度
     * @param time 动画持续时间
     */
    public void setProgress(final float currentProgress, int time){
        this.currentProgress = currentProgress;
        //从0开始变化
        currentPercent = 0;
        //设置动画时间
        waveProgressAnimat.setDuration(time);
        //设置循环播放
        waveProgressAnimat.setRepeatCount(Animation.INFINITE);

        waveProgressAnimat.setInterpolator(new LinearInterpolator());
        waveProgressAnimat.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //如果需要让波浪到达最高处后平移的速度改变，给动画设置监听即可
                if(currentPercent == currentProgress /maxProgress){
                    waveProgressAnimat.setDuration(7000);
                }
            }
        });
        //当前视图开启动画
        this.startAnimation(waveProgressAnimat);
    }

    //定义数值监听
    public interface UpdateTextListener{
        /**
         * 提供接口 给外部修改数值样式 等
         * @param interpolatedTime 这个值是动画的 从0变成1
         * @param currentProgress 进度条的数值
         * @param maxProgress 进度条的最大数值
         * @return
         */
        String updateText(float interpolatedTime,float currentProgress,float maxProgress);
    }
    //设置监听
    public void setUpdateTextListener(UpdateTextListener updateTextListener){
        this.updateTextListener = updateTextListener;

    }

    /**
     *
     * 设置显示内容
     * @param tv_progress 内容 数值什么都可以
     *
     */
    public void setTextViewVaule(TextView tv_progress){
        this.tv_progress = tv_progress;

    }
}


