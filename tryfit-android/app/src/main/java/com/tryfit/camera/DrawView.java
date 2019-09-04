package com.tryfit.camera;

/**
 * Created by Rauf Yagfarov on 18/09/2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {
    Paint paintBackground;
    Paint paintGraph;
    Path path;
    Context myContext;
    int counter;
    boolean clearCan;
    int framesPerSecond = 50;
    long animationDuration = 5000;
    long startTime;
    long elapsedTime;
    boolean isFinished;
    private volatile int[] points;
    private volatile float[] statusCoordinates;


    public DrawView(Context context) {
        super(context);
        init(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context c) {
        myContext = c;
        points = new int[0];
        statusCoordinates = new float[0];

        paintBackground = new Paint();
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setColor(Color.argb(100, 0, 0, 255));
        paintBackground.setFlags(Paint.ANTI_ALIAS_FLAG);

        paintGraph = new Paint();
        path = new Path();

        paintGraph.setStyle(Paint.Style.STROKE);
        paintGraph.setColor(Color.rgb(0, 0, 255));
        paintGraph.setStrokeWidth(15);
        paintGraph.setFlags(Paint.ANTI_ALIAS_FLAG);

        clearCan = false;
        counter = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    public void updateCoordinates(int[] currPoints, int counter) {
        this.points = currPoints;
        this.counter = counter;
        if (counter <= 2) {
            this.postInvalidate();
        }
    }

    public boolean getIsFinished() {
        return this.isFinished;
    }

    public void setIsFinished(boolean value) {
        this.isFinished = value;
    }

    public void pointsUpdate() {

        int[] points = this.points.clone();
        this.statusCoordinates = new float[points.length];

        int x1 = points[0];
        int y1 = points[1];
        int x2 = points[2];
        int y2 = points[3];
        double x;
        double y;
        if (x2 - x1 == 0) {
            x = x1;
            y = y1 + ((double) Math.min(elapsedTime, animationDuration) / ((double) animationDuration) * (y2 - y1));
        } else {
            x = x1 + ((double) Math.min(elapsedTime, animationDuration)) / ((double) animationDuration) * (x2 - x1);
            y = y1 + ((double) (y2 - y1) / (x2 - x1)) * (x - x1);
        }
        statusCoordinates[0] = (float) x;
        statusCoordinates[1] = (float) y;

        x1 = points[4];
        y1 = points[5];
        x2 = points[2];
        y2 = points[3];
        if (x2 - x1 == 0) {
            x = x1;
            y = y1 + ((double) Math.min(elapsedTime, animationDuration) / ((double) animationDuration) * (y2 - y1));
        } else {
            x = x1 + ((double) Math.min(elapsedTime, animationDuration)) / ((double) animationDuration) * (x2 - x1);
            y = y1 + ((double) (y2 - y1) / (x2 - x1)) * (x - x1);
        }
        statusCoordinates[2] = (float) x;
        statusCoordinates[3] = (float) y;

        x1 = points[4];
        y1 = points[5];
        x2 = points[6];
        y2 = points[7];
        if (x2 - x1 == 0) {
            x = x1;
            y = y1 + ((double) Math.min(elapsedTime, animationDuration) / ((double) animationDuration) * (y2 - y1));
        } else {
            x = x1 + ((double) Math.min(elapsedTime, animationDuration)) / ((double) animationDuration) * (x2 - x1);
            y = y1 + ((double) (y2 - y1) / (x2 - x1)) * (x - x1);
        }
        statusCoordinates[4] = (float) x;
        statusCoordinates[5] = (float) y;

        x1 = points[0];
        y1 = points[1];
        x2 = points[6];
        y2 = points[7];
        if (x2 - x1 == 0) {
            x = x1;
            y = y1 + ((double) Math.min(elapsedTime, animationDuration) / ((double) animationDuration) * (y2 - y1));
        } else {
            x = x1 + ((double) Math.min(elapsedTime, animationDuration)) / ((double) animationDuration) * (x2 - x1);
            y = y1 + ((double) (y2 - y1) / (x2 - x1)) * (x - x1);
        }
        statusCoordinates[6] = (float) x;
        statusCoordinates[7] = (float) y;
    }

    public void clearCanvas() {

        this.clearCan = true;
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(0, 0, getWidth(), getHeight(), paintBackground);
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (!clearCan) {
            if (points.length > 0) {
                if (counter == 1) {
                    this.startTime = System.currentTimeMillis();
                    this.isFinished = false;
                }

                elapsedTime = System.currentTimeMillis() - startTime;
                path.reset();
                path.moveTo(points[0], points[1]);
                for (int i = 2; i <= 6; i += 2) {
                    path.lineTo(points[i], points[i + 1]);
                }
                path.lineTo(points[0], points[1]);

                paintBackground.setColor(Color.argb(100, (int) (225 * (1 - (double) Math.min(elapsedTime, animationDuration) / animationDuration)), (int) (225 * (double) Math.min(elapsedTime, animationDuration) / animationDuration), 0));
                canvas.drawPath(path, paintBackground);
//
                if (counter > 1) {
                    if (elapsedTime < animationDuration) {
                        pointsUpdate();
                        if (statusCoordinates.length > 0) {
                            canvas.drawLine(points[0] + 5, points[1], statusCoordinates[0], statusCoordinates[1], paintGraph);
                            canvas.drawLine(points[4], points[5], statusCoordinates[2], statusCoordinates[3], paintGraph);
                            canvas.drawLine(points[4] - 5, points[5], statusCoordinates[4], statusCoordinates[5], paintGraph);
                            canvas.drawLine(points[0], points[1], statusCoordinates[6], statusCoordinates[7], paintGraph);
                        }
                        this.postInvalidateDelayed(1000 / framesPerSecond);
                    } else {
                        isFinished = true;
                    }
                }
//                else{
//                    clearCanvas();
//                    this.postInvalidate();
//                }

            } else {
                isFinished = false;
            }
        } else {
            canvas.drawColor(Color.TRANSPARENT);
            setPoints(new int[0]);
            clearCan = false;
            this.postInvalidate();
        }
//
//        if (points.length > 0) {
//            for (int i = 2; i < 6; i += 2) {
//                canvas.drawLine(points[i], points[i+1], points[i+2], points[i+3], paintGraph);
//            }
//            canvas.drawLine(points[6], points[7], points[0], points[1], paintGraph);
//        }

//        canvas.drawLine(100,100,500,500, paintGraph);
    }

    public void setPoints(int[] points) {
        this.points = points;
    }

}