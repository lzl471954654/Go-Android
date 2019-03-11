package com.chaoyu.go.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.chaoyu.go.R;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {

    private static final int MAX_LINE = 11;

    private int mPanelWidth;

    private float mLineHeight;

    private Paint mPaint = new Paint();

    private Bitmap mBlack;
    private Bitmap mWhite;
    private float rowSize = 3 * 1.0f/4;

    private List<Point> mWhiteArray = new ArrayList<>();
    private List<Point> mBlackArray = new ArrayList<>();

    //当前是否为 白棋 ， 默认情况白棋先手。
    private boolean mIsWhite = true;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initRes();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        //棋子宽度
        int mWhiteWidth = (int) (mLineHeight * rowSize);

        //修改棋子大小
        mWhite = Bitmap.createScaledBitmap(mWhite, mWhiteWidth, mWhiteWidth, false);
        mBlack = Bitmap.createScaledBitmap(mBlack, mWhiteWidth, mWhiteWidth, false);

    }

    private void initPaint(){
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
    }

    private void initRes(){
        mBlack = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        mWhite = BitmapFactory.decodeResource(getResources(), R.drawable.white);
    }

    public GameView(Context context) {
        super(context);
        initPaint();
        initRes();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:{
                int x = (int)event.getX();
                int y = (int)event.getY();

                Point point = new Point(x,y);

                if (mWhiteArray.contains(point) || mBlackArray.contains(point)){
                    return false;
                }

                if (mIsWhite){
                    mWhiteArray.add(point);
                }else {
                    mBlackArray.add(point);
                }

                invalidate();

                mIsWhite = !mIsWhite;

                break;
            }
        }

        return false;
    }

    /**
     * 不能重复点击
     *
     * @param x
     * @param y
     * @return
     */
    private Point getValidPoint(int x, int y) {

        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    /**
     * 绘制棋子的方法
     *
     * @param canvas
     */
    private void drawPieces(Canvas canvas) {
        for (int i = 0; i < mWhiteArray.size(); i++) {
            //获取白棋子的坐标
            Point whitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mBlack, (whitePoint.x + (1 - rowSize) / 2) * mLineHeight, (whitePoint.y + (1 - rowSize) / 2) * mLineHeight, null);
        }

        for (int i = 0; i < mBlackArray.size(); i++) {
            //获取黑棋子的坐标
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mWhite, (blackPoint.x + (1 - rowSize) / 2) * mLineHeight, (blackPoint.y + (1 - rowSize) / 2) * mLineHeight, null);
        }
    }

    private void drawLine(Canvas canvas){
        int w = mPanelWidth;
        float lineHeight = mLineHeight;

        for (int i = 0; i<MAX_LINE ; i++){
            int startX = (int)(lineHeight / 2);
            int endX = (int)(w - lineHeight / 2);

            int y = (int)((0.5 + i) * lineHeight);

            canvas.drawLine(startX,y,endX,y,mPaint);

            canvas.drawLine(y,startX,y,endX,mPaint);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        drawPieces(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int hightSize = MeasureSpec.getSize(heightMeasureSpec);
        int hightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize,hightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED){
            width = hightSize;
        }else if (hightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }

        setMeasuredDimension(width,width);
    }
}
