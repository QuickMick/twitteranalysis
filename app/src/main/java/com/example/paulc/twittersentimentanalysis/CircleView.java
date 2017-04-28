package com.example.paulc.twittersentimentanalysis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    private int start=0;
    private int end = 360;

    public CircleView(Context context)
    {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

        @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        float width = (float) getWidth();
        float height = (float) getHeight();
        float radius;

        if (width > height) {
            radius = height / 4;
        } else {
            radius = width / 4;
        }

        Path path = new Path();
        path.addCircle(width / 2,
                height / 2, radius,
                Path.Direction.CW);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);    //TODO: @paul --> please change it to the color of the desing
        paint.setStrokeWidth(80);
        paint.setStyle(Paint.Style.FILL);

        float center_x, center_y;
        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);

        center_x = width / 2;
        center_y = height / 2;

        oval.set(center_x - radius,
                center_y - radius,
                center_x + radius,
                center_y + radius);
        canvas.drawArc(oval, this.start, this.end, false, paint);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}