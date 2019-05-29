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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private int[][] panelData = new int[19][19];

    private int blackCount = 0;
    private int whiteCount = 0;
    private static int WIN_COUNT = 3;

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
        if (type == 1){
            mBlackArray.add(point);
            panelData[x][y] = -1;
        }
        else{
            mWhiteArray.add(point);
            panelData[x][y] = 1;
        }
        this.mIsWhite = !mIsWhite;
        String color = "";
        if (mIsWhite)
            color = "白棋";
        else
            color = "黑棋";
        callback.updateMsg("luozi",color+"成功落子点："+x+","+y);
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
                        if (canLuoZi(mIsWhite,point.x,point.y)){
                            mWhiteArray.add(point);
                            callback.send(point.x,point.y,mIsWhite);
                            panelData[point.x][point.y] = 1;
                            mIsWhite = !mIsWhite;
                            eatCheck();
                        }
                    }else {
                        Toast.makeText(context,"请等待黑棋",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (!mIsWhite){
                        if (canLuoZi(mIsWhite,point.x,point.y)){
                            mBlackArray.add(point);
                            callback.send(point.x,point.y,mIsWhite);
                            panelData[point.x][point.y] = -1;
                            mIsWhite = !mIsWhite;
                            eatCheck();
                        }
                    }else {
                        Toast.makeText(context,"请等待白棋",Toast.LENGTH_SHORT).show();
                    }
                }

                invalidate();

            }
        }

        return true;
    }


    public void eatCheck(){
        Set<Point> set = new HashSet<>();
        for (Point point : mWhiteArray) {
            if (!checkRange(true,point.x,point.y,panelData)){
                eatInner(true,point);
                set.add(point);
            }
        }
        mWhiteArray.removeAll(set);
        set.clear();
        for (Point point : mBlackArray) {
            if (!checkRange(false,point.x,point.y,panelData)){
                eatInner(false,point);
                set.add(point);
            }
        }
        mBlackArray.removeAll(set);
        set.clear();
    }

    private void eatInner(boolean isWhite,Point point){
        String color = "白棋";
        if (isWhite){
            color = "白棋";
            blackCount++;
        }else {
            color = "黑棋";
            whiteCount++;
        }
        panelData[point.x][point.y] = 0;
        callback.updateMsg("eat",color+":"+point.x+","+point.y+"被吃");
        callback.sendEat(point.x,point.y,isWhite);
        if (blackCount >= WIN_COUNT){
            callback.win(false);
        }
        if (whiteCount >= WIN_COUNT){
            callback.win(true);
        }
    }

    public void beEat(boolean isWhite,Point point){
        String color = "白棋";
        if (isWhite){
            mWhiteArray.remove(point);
            blackCount++;
        }else {
            mBlackArray.remove(point);
            color = "黑棋";
            whiteCount++;
        }
        panelData[point.x][point.y] = 0;
        callback.updateMsg("eat",color+":"+point.x+","+point.y+"被吃");
        invalidate();
        if (blackCount >= WIN_COUNT){
            callback.win(false);
        }
        if (whiteCount >= WIN_COUNT){
            callback.win(true);
        }
    }

    public boolean canLuoZi(boolean isWhite,int x,int y){
        if (checkRange(isWhite,x,y,panelData)){
            String color = "";
            if (isWhite)
                color = "白棋";
            else
                color = "黑棋";
            callback.updateMsg("luozi",color+"成功落子点："+x+","+y);
            return true;
        }
        else{
            callback.updateMsg("luozi","此处为死期，无法落子");
            return false;
        }
    }

    public  boolean checkRange(boolean isWhite,int x,int y,int[][] panel){
        if (isWhite){
            if (x>=1&&x<=17){
                if (y>=1&&y<=17){
                    return panel[x + 1][y] != -1 || panel[x - 1][y] != -1 || panel[x][y - 1] != -1 || panel[x][y + 1] != -1;
                }else if (y == 0){
                    return panel[x + 1][y] != -1 || panel[x - 1][y] != -1 || panel[x][y+1] != -1;
                }else {
                    return panel[x + 1][y] != -1 || panel[x - 1][y] != -1 || panel[x][y-1] != -1;
                }
            }else if (x == 0){
                if (y>=1&&y<=17){
                    return panel[x][y - 1] != -1 || panel[x][y + 1] != -1 || panel[x+1][y] != -1;
                }else if (y == 0){
                    return panel[x+1][y] != -1 || panel[x][y+1] != -1;
                }else {
                    return panel[x+1][y] != -1 || panel[x][y-1] != -1;
                }
            }else {
                if (y>=1&&y<=17){
                    return panel[x][y - 1] != -1 || panel[x][y + 1] != -1 || panel[x-1][y] != -1;
                }else if (y==0){
                    return panel[x-1][y] != -1 || panel[x][y+1] != -1;
                }else {
                    return panel[x-1][y] != -1 || panel[x][y-1] != -1;
                }
            }
        }else {
            if (x>=1&&x<=17){
                if (y>=1&&y<=17){
                    return panel[x + 1][y] != 1 || panel[x - 1][y] != 1 || panel[x][y - 1] != 1 || panel[x][y + 1] != 1;
                }else if (y == 0){
                    return panel[x + 1][y] != 1 || panel[x - 1][y] != 1 || panel[x][y+1] != 1;
                }else {
                    return panel[x + 1][y] != 1 || panel[x - 1][y] != 1 || panel[x][y-1] != 1;
                }
            }else if (x == 0){
                if (y>=1&&y<=17){
                    return panel[x][y - 1] != 1 || panel[x][y + 1] != 1 || panel[x+1][y] != 1;
                }else if (y == 0){
                    return panel[x+1][y] != 1|| panel[x][y+1] != 1;
                }else {
                    return panel[x+1][y] != 1 || panel[x][y-1] != 1;
                }
            }else {
                if (y>=1&&y<=17){
                    return panel[x][y - 1] != 1 || panel[x][y + 1] != 1 || panel[x-1][y] != 1;
                }else if (y==0){
                    return panel[x-1][y] != 1 || panel[x][y+1] != 1;
                }else {
                    return panel[x-1][y] != 1 || panel[x][y-1] != 1;
                }
            }
        }
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
