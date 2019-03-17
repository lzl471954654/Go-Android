package com.chaoyu.go.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.chaoyu.go.R;
import com.chaoyu.go.StatusCallback;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {

    private static int MAX_LINE = 19;

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
    private int type = 1;
    private Context context;

    private StatusCallback callback;

    private boolean isGameStart = false;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initPaint();
        initRes();
        setBackgroundColor(Color.YELLOW);
    }

    public void setCallback(StatusCallback callback){
        this.callback = callback;
    }

    public void setGameStart(int type){
        mIsWhite =  type == 1;
        this.type = type;
        isGameStart = true;
    }

    public void reInit(int lines, int type, StatusCallback callback){
        this.callback = callback;
        mIsWhite = type == 1;
        this.type = type;
        MAX_LINE = lines > 19 ? 19 : lines;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        //棋子宽度
        int mWhiteWidth = (int) (mLineHeight * rowSize);

        //修改棋子大小
        mWhite = Bitmap.createScaledBitmap(mWhite, mWhiteWidth, mWhiteWidth, false);
        mBlack = Bitmap.createScaledBitmap(mBlack, mWhiteWidth, mWhiteWidth, false);
        mWhiteArray.clear();
        mBlackArray.clear();
        invalidate();
    }

    public void addPoint(int x,int y,boolean mIsWhite){
        Point point = new Point(x,y);
        if (type == 1)
            mBlackArray.add(point);
        else
            mWhiteArray.add(point);
        this.mIsWhite = !mIsWhite;
        invalidate();
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
        System.out.println("onTouch");
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:{
                int x = (int)event.getX();
                int y = (int)event.getY();

                Point point = getValidPoint(x,y);
                Log.e(this.getClass().getName(),point.toString());

                if (!isGameStart){
                    Toast.makeText(context, "请等待游戏开始", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (mWhiteArray.contains(point) || mBlackArray.contains(point)){
                    return false;
                }

                if (type == 1){
                    if (mIsWhite){
                        mWhiteArray.add(point);
                        callback.send(point.x,point.y,mIsWhite);
                        mIsWhite = !mIsWhite;
                    }else {
                        Toast.makeText(context,"请等待黑棋",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (!mIsWhite){
                        mBlackArray.add(point);
                        callback.send(point.x,point.y,mIsWhite);
                        mIsWhite = !mIsWhite;
                    }else {
                        Toast.makeText(context,"请等待白棋",Toast.LENGTH_SHORT).show();
                    }
                }

                invalidate();

            }
        }

        return true;
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
            canvas.drawBitmap(mWhite, (whitePoint.x + (1 - rowSize) / 2) * mLineHeight, (whitePoint.y + (1 - rowSize) / 2) * mLineHeight, null);
        }

        for (int i = 0; i < mBlackArray.size(); i++) {
            //获取黑棋子的坐标
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlack, (blackPoint.x + (1 - rowSize) / 2) * mLineHeight, (blackPoint.y + (1 - rowSize) / 2) * mLineHeight, null);
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
