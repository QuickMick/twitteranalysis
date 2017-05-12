package com.hhn.paulc.twittersentimentanalysis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    private float start=0;
    private float end = 360;
    private float percent;

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

        paint.setStrokeWidth(radius);
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


       // paint.setColor(Color.rgb(161,224,169));
            paint.setColor(ContextCompat.getColor(this.getContext(), R.color.circleNegative));
        canvas.drawArc(oval, 0, 360, false, paint);
        //paint.setColor(Color.rgb(39,142,136));
          //  paint.setColor(Color.rgb(30,112,107));
            paint.setColor(ContextCompat.getColor(this.getContext(), R.color.circlePositive));

        canvas.drawArc(oval, this.start, this.end, false, paint);


            Paint paintText = new Paint();
            paintText.setTypeface(Typeface.DEFAULT);// your preference here
            paintText.setColor(ContextCompat.getColor(this.getContext(), R.color.circleText));
            paintText.setTextSize(radius);// have this the same as your text size


          /*  NumberFormat defaultFormat = NumberFormat.getPercentInstance();
            defaultFormat.setMinimumFractionDigits(1);
            defaultFormat.setMaximumFractionDigits(1);
            System.out.println("Percent format: " + defaultFormat.format(num));
*/
            String text = (((float)((int)(this.percent*10)))/10)+"%";

            Rect bounds = new Rect();
            paintText.getTextBounds(text, 0, text.length(),bounds);

            canvas.drawText(text,center_x-bounds.width()/2,center_y+ bounds.height()/2,paintText);
    }

    public float getStart() {
        return start;
    }

    public void setStart(float start) {
        this.start = start;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public void setPercent(float percent) {
        this.percent = percent;
        this.start = 180;
        this.end = ((percent/100f)*360);

        this.invalidate();
    }

    public float getPercent() {
        return percent;
    }
}