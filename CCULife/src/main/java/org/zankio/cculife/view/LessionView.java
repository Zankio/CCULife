package org.zankio.cculife.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.zankio.cculife.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
public class LessionView extends View {


    private String mStartTime;
    private String mEndTime;

    private String mClassName;
    private String mClassRoom;
    private String mClassTeacher;

    private boolean isShowName;
    private boolean isShowRoom;
    private boolean isShowTeacher;
    private boolean isShowStartTime;
    private boolean isShowEndTime;

    private RectF mBound = new RectF();

    private int mBackgroundColor = 0xFFFFBB33;
    private int mTextColor = 0xFF000000;
    private int mTimeColor = 0xAA000000;
    private Paint mBackgroundPaint;
    private Paint mTimePaint;
    private Paint mTextPaint;

    private float mClassNameX = 0.0f;
    private float mClassNameY = 0.0f;
    private float mClassNameWidth = 0.0f;
    private float mClassNameHeight = 0.0f;

    private float mStartTimeX = 0.0f;
    private float mStartTimeY = 0.0f;
    private float mStartTimeWidth = 0.0f;
    private float mStartTimeHeight = 0.0f;

    private float mEndTimeX = 0.0f;
    private float mEndTimeY = 0.0f;
    private float mEndTimeWidth = 0.0f;
    private float mEndTimeHeight = 0.0f;

    public LessionView(Context context) {
        super(context);
        init();

    }

    public LessionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LessionView,
                0, 0
        );

        try {
            assert a != null;
            mClassName = a.getString(R.styleable.LessionView_ClassName);
            mTextColor = a.getColor(R.styleable.LessionView_TextColor, 0xFF000000);
            mTimeColor = a.getColor(R.styleable.LessionView_TextColor, 0xAA000000);
            mBackgroundColor = a.getColor(R.styleable.LessionView_BackgroundColor, 0xFFFFBB33);
        } finally {
            a.recycle();
        }

        init();
    }


    private void init() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(20);

        mTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimePaint.setColor(mTimeColor);
        mTimePaint.setTextSize(15);
        measureTextSize();

    }

    public void measureTextSize(){
        Rect rect;
        rect = new Rect();
        if(mClassName != null) {
            mTextPaint.getTextBounds(mClassName, 0, mClassName.length(), rect);
            mClassNameHeight = rect.height();
            mClassNameWidth = rect.width();
            mClassNameX = mBound.centerX() - mClassNameWidth / 2.0f;
            mClassNameY = mBound.centerY();
        }

        if (mStartTime != null) {
            mTimePaint.getTextBounds(mStartTime, 0, mStartTime.length(), rect);
            mStartTimeHeight = rect.height();
            mStartTimeWidth = rect.width();
            mStartTimeX = 6;
            mStartTimeY = 6 + mStartTimeHeight;
        }

        if (mEndTime != null) {
            mTimePaint.getTextBounds(mEndTime, 0, mEndTime.length(), rect);
            mEndTimeHeight = rect.height();
            mEndTimeWidth = rect.width();
            mEndTimeX = mBound.width() - 6 - mEndTimeWidth;
            mEndTimeY = mBound.height() - 6;
        }

    }

    public void setClassName(String className) {
        mClassName = className;
        measureTextSize();
        invalidate();
    }

    public void setClassRoom(String classRoom) {
        mClassRoom = classRoom;
        invalidate();
    }

    public void setClassTeacher(String classTeacher) {
        mClassTeacher = classTeacher;
        invalidate();
    }

    public void setEndTime(Calendar endTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        mEndTime = simpleDateFormat.format(endTime.getTime());
        measureTextSize();
        invalidate();
    }

    public void setStartTime(Calendar startTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        mStartTime = simpleDateFormat.format(startTime.getTime());
        measureTextSize();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mBound = new RectF(0.0f, 0.0f, w, h);

        mClassNameX = mBound.centerX() - mClassNameWidth / 2.0f;
        mClassNameY = mBound.centerY();
        measureTextSize();
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x, y;
        String[] drawText;

        if(mClassName != null) {
            drawText = splitText(mClassName, mTextPaint, mBound.width());
            x = mClassNameX;
            y = mClassNameY;

            for (String text : drawText) {
                canvas.drawText(text, x, y, mTextPaint);
                y += mClassNameHeight + 3;
            }
        }

        if (mStartTime != null) {
            canvas.drawText(mStartTime, mStartTimeX, mStartTimeY, mTimePaint);
        }


        if (mEndTime != null) {
            canvas.drawText(mEndTime, mEndTimeX, mEndTimeY, mTimePaint);
        }

    }

    private String[] splitText(String str, Paint paint, float width) {
        int nextPos, length;
        String substr;
        float drawHeight, drawWidth = 0;
        ArrayList<String> result = new ArrayList<String>();
        length = str.length();

        while (length > 0) {
            nextPos = paint.breakText(str, true, width, null);
            substr = str.substring(0, nextPos);
            drawWidth = Math.max(paint.measureText(substr), drawWidth);

            result.add(substr);
            length -= nextPos;

            if (length > 0)
                str = str.substring(nextPos);
        }

        drawHeight = result.size() * mClassNameHeight + ((result.size() - 1) * 3);
        mClassNameX = mBound.centerX() - drawWidth / 2.0f;
        mClassNameY = mBound.centerY() - drawHeight / 2.0f + mClassNameHeight / 2.0f;

        return result.toArray(new String[result.size()]);
    }

}
