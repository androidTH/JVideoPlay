package com.example.min.jvideoplay.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.example.min.jvideoplay.R;


/**
 * http://circleprogress.osslab.online
 */
public class CircleProgressBar extends View {

    // Properties
    private float progress = 0;
    private float strokeWidth = getResources().getDimension(R.dimen.default_stroke_width);
    private float backgroundStrokeWidth = getResources().getDimension(R.dimen.default_background_stroke_width);
    private int color = Color.BLACK;
    private int backgroundColor = Color.GRAY;

    // Object used to draw
    private int startAngle = -90;
    private RectF rectF;
    private Paint backgroundPaint;
    private Paint foregroundPaint;
    private Paint whitePaint;

    private int height;
    private int width;
    private long maxLen=100;

    private ProgressBarListener mProgressBarListener;

    //region Constructor & Init Method
    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rectF = new RectF();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, 0, 0);
        //Reading values from the XML layout
        try {
            // Value
            progress = typedArray.getFloat(R.styleable.CircleProgressBar_progress_value, progress);
            // StrokeWidth
            strokeWidth = typedArray.getDimension(R.styleable.CircleProgressBar_progress_width, strokeWidth);
            backgroundStrokeWidth = typedArray.getDimension(R.styleable.CircleProgressBar_background_width, backgroundStrokeWidth);
            // Color
            color = typedArray.getInt(R.styleable.CircleProgressBar_progress_color, color);
            backgroundColor = typedArray.getInt(R.styleable.CircleProgressBar_background_color, backgroundColor);
        } finally {
            typedArray.recycle();
        }

        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setColor(Color.parseColor("#00000000"));
//        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setAntiAlias(true);
        whitePaint.setStrokeWidth(backgroundStrokeWidth);

        // Init Background
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeWidth(backgroundStrokeWidth);

        // Init Foreground
        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(color);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(strokeWidth);
        foregroundPaint.setAntiAlias(true);
        foregroundPaint.setStrokeCap(Paint.Cap.ROUND);
    }
    //endregion

    //region Draw Method
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rectF, backgroundPaint);
        canvas.drawCircle(width/2,height/2,height/2-backgroundStrokeWidth,whitePaint);
        float angle = 360 * progress / maxLen;
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint);
    }
    //endregion

    //region Mesure Method
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        float highStroke = (strokeWidth > backgroundStrokeWidth) ? strokeWidth : backgroundStrokeWidth;
        rectF.set(0 + highStroke / 2, 0 + highStroke / 2, min - highStroke / 2, min - highStroke / 2);
    }
    //endregion

    //region Method Get/Set
    public float getProgress() {
        return progress;
    }

    public void setMax(long max){
        this.maxLen=max;
    }
    public long getMaxLen(){
        return maxLen;
    }
    public void setProgress(float progress) {
        this.progress = (progress<=maxLen) ? progress : maxLen;
        invalidate();
        if(progress==maxLen){
            if(mProgressBarListener!=null){
                mProgressBarListener.stopProgressLinstener();
            }
        }
    }

    public void setProgress(float progress,boolean reset) {
        if(reset){
          whitePaint.setColor(Color.parseColor("#00000000"));
        }
        this.progress = (progress<=maxLen) ? progress : maxLen;
        invalidate();
    }


    public float getProgressBarWidth() {
        return strokeWidth;
    }

    public void setProgressBarWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        foregroundPaint.setStrokeWidth(strokeWidth);
        requestLayout();//Because it should recalculate its bounds
        invalidate();
    }

    public float getBackgroundProgressBarWidth() {
        return backgroundStrokeWidth;
    }

    public void setBackgroundProgressBarWidth(float backgroundStrokeWidth) {
        this.backgroundStrokeWidth = backgroundStrokeWidth;
        backgroundPaint.setStrokeWidth(backgroundStrokeWidth);
        requestLayout();//Because it should recalculate its bounds
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        foregroundPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundPaint.setColor(backgroundColor);
        invalidate();
        requestLayout();
    }
    //endregion

    //region Other Method
    /**
     * Set the progress with an animation.
     * Note that the {@link ObjectAnimator} Class automatically set the progress
     * so don't call the {@link CircleProgressBar#setProgress(float)} directly within this method.
     *
     * @param progress The progress it should animate to it.
     */
    public void setProgressWithAnimation(float progress) {
        setProgressWithAnimation(progress, 1500);
    }

    /**
     * Set the progress with an animation.
     * Note that the {@link ObjectAnimator} Class automatically set the progress
     * so don't call the {@link CircleProgressBar#setProgress(float)} directly within this method.
     *
     * @param progress The progress it should animate to it.
     * @param duration The length of the animation, in milliseconds.
     */
    public void setProgressWithAnimation(float progress, int duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }
    private ValueAnimator mProgressAnimation;
    public void startProgressWithAnimation(float progress, int duration) {
        whitePaint.setColor(Color.parseColor("#ffb92c"));
        if(mProgressAnimation==null){
            mProgressAnimation = ValueAnimator.ofFloat(0, progress);
        }
        mProgressAnimation.setDuration(duration);
        mProgressAnimation.setInterpolator(new LinearInterpolator());
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress(Float.valueOf(animation.getAnimatedValue().toString()));
            }
        });
        mProgressAnimation.start();
    }

    public void stopProgressWidthAnimation(){
        mProgressAnimation.removeAllUpdateListeners();
    }

    public void setmProgressBarListener(ProgressBarListener mPbL){
        this.mProgressBarListener=mPbL;
    }

    public interface ProgressBarListener{
        public void stopProgressLinstener();
    }
}
