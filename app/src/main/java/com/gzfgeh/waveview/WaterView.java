package com.gzfgeh.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import java.math.BigDecimal;

/**
 * Description:
 * Created by guzhenfu on 2016/5/20 09:53.
 */
public class WaterView extends View{
    private int width, height;
    private Paint outCirclePaint;
    private Paint interCirclePaint;
    private Paint waterPaint;
    private Paint progressText;
    private Paint paint;
    private Rect mTextBound = new Rect();

    private long c = 0L;
    private final float f = 0.033F;
    private float mAmplitude = 15.0F; // 振幅
    float mWaterLevel = 0.0f;
    private float textSize = 50.f;
    private int outStrokeWidth = 15;
    private int alpha = 50;
    private int color = Color.WHITE;
    private float progressStartX = 0, progressStartY = 0;

    private String downloading = "下载中...";

    public WaterView(Context context) {
        this(context, null);
    }

    public WaterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        outCirclePaint = new Paint();
        outCirclePaint.setAntiAlias(true);
        outCirclePaint.setDither(true);
        outCirclePaint.setStyle(Paint.Style.STROKE);
        outCirclePaint.setColor(color);
        outCirclePaint.setStrokeWidth(outStrokeWidth);
        outCirclePaint.setAlpha(alpha);

        interCirclePaint = new Paint();
        interCirclePaint.setStyle(Paint.Style.STROKE);
        interCirclePaint.setColor(color);
        interCirclePaint.setStrokeWidth(2);
        interCirclePaint.setAntiAlias(true);

        waterPaint = new Paint();
        waterPaint.setAntiAlias(true);
        waterPaint.setDither(true);
        waterPaint.setStrokeWidth(1.0f);
        //下面两个顺序比较重要
        waterPaint.setColor(color);
        waterPaint.setAlpha(alpha);

        progressText = new Paint();
        progressText.setColor(color);
        progressText.setTextSize(textSize);
        progressText.setAntiAlias(true);
        progressText.setStyle(Paint.Style.FILL);

        paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setWaterLevel(float level) {
        this.mWaterLevel = level;
        if (mWaterLevel >= 1.0f) {
            mWaterLevel = 1.0f;
            downloading = "下载完成";
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasureSize(widthMeasureSpec, true);
        int height = getMeasureSize(heightMeasureSpec, false);

        if (width > height){
            setMeasuredDimension(width, width);
        }else{
            setMeasuredDimension(height, height);
        }

    }

    private int getMeasureSize(int measureSpec, boolean isWidth){
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();

        if (mode == MeasureSpec.EXACTLY){
            result = size;
        }else{
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST){
                result = isWidth ? Math.max(result, size) : Math.min(result, size);
            }
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //计算当前油量线和水平中线的距离
        float centerOffset = Math.abs(width / 2 * mWaterLevel - width / 4);
        //计算油量线和与水平中线的角度
        float horiAngle = (float)(Math.asin(centerOffset / (width / 4)) * 180 / Math.PI);
        //扇形的起始角度和扫过角度
        float startAngle, sweepAngle;
        if (mWaterLevel > 0.5F) {
            startAngle = 360F - horiAngle;
            sweepAngle = 180F + 2 * horiAngle;
        } else {
            startAngle = horiAngle;
            sweepAngle = 180F - 2 * horiAngle;
        }
        RectF rectF = new RectF(width/4, height/4, width*3/4, height*3/4);
        canvas.drawArc(rectF, startAngle, sweepAngle, false, waterPaint);

        float progress = new BigDecimal(mWaterLevel * 100).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        String text = String.valueOf(progress+ "%");
        float textWidth = progressText.measureText(text);

        progressText.getTextBounds(text, 0, text.length(), mTextBound);
        float textHeight = mTextBound.height();

        progressStartX = width/2 - textWidth/2;
        progressStartY = height/2 - textHeight/2;
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        progressText.setColor(color);
        canvas.clipRect(progressStartX, progressStartY, progressStartX + textWidth, progressStartY + (1 - mWaterLevel) * textHeight);
        canvas.drawText(text, width/2 - textWidth/2, height/2 + textHeight/2, progressText);
        canvas.restore();
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        if (mWaterLevel != 1.0f) {
            progressText.setAlpha(alpha);
        }
        canvas.clipRect(progressStartX, progressStartY + (1 - mWaterLevel) * textHeight, progressStartX + textWidth, progressStartY + textHeight);
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, progressText);
        canvas.restore();



        textWidth = paint.measureText(downloading);
        paint.getTextBounds(downloading, 0, downloading.length(), mTextBound);
        textHeight = mTextBound.height();
        canvas.drawText(downloading, width/2 - textWidth/2, height *7/8 + textHeight/2, paint);

        //关键的一段
        if (this.c >= 8388607L) {
            this.c = 0L;
        }
        c++;
        float f1 = width * (1.0F - (0.25F + mWaterLevel / 2)) - mAmplitude;
        //当前油量线的长度
        float waveWidth = (float)Math.sqrt(width * width / 16 - centerOffset * centerOffset);
        //与圆半径的偏移量
        float offsetWidth = width / 4 - waveWidth;

        int top = (int) (f1 + mAmplitude);

        //起始振动X坐标，结束振动X坐标
        int startX, endX;
        if (mWaterLevel > 0.50F) {
            startX = (int) (width / 4 + offsetWidth);
            endX = (int) (width / 2 + width / 4 - offsetWidth);
        } else {
            startX = (int) (width / 4 + offsetWidth - mAmplitude);
            endX = (int) (width / 2 + width / 4 - offsetWidth + mAmplitude);
        }
        // 波浪效果
        while (startX < endX) {
            int startY = (int) (f1 - mAmplitude * Math.sin(Math.PI * (2.0F * (startX + this.c * width * this.f)) / width));
            canvas.drawLine(startX, startY, startX, top, waterPaint);
            startX++;
        }
        //关键的一段
        canvas.drawCircle(width / 2, width / 2, width / 4 + outStrokeWidth / 2, outCirclePaint);
        canvas.drawCircle(width / 2, width / 2, width / 4, interCirclePaint);
        canvas.restore();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = (int) c;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        c = ss.progress;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 关闭硬件加速，防止异常unsupported operation exception
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    static class SavedState extends BaseSavedState {
        int progress;

        /**
         * Constructor called from {@link ProgressBar#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }



}
