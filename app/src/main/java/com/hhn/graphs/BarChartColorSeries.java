package com.hhn.graphs;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.Series;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * GraphView
 * Copyright (C) 2014  Jonas Gehring
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * with the "Linking Exception", which can be found at the license.txt
 * file in this program.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with the "Linking Exception" along with this program; if not,
 * write to the author Jonas Gehring <g.jjoe64@gmail.com>.
 *
 * edited by matthias figura,
 * valuedependendColor is now indexDependenColor
 */

public class BarChartColorSeries extends BarGraphSeries<DataPoint> {

    public BarChartColorSeries() {
        super();
        this.setCustomPaint(new Paint());
    }

    public BarChartColorSeries(DataPoint[] data) {
        super(data);
        this.setCustomPaint(new Paint());
    }

    /**
     * draws the bars on the canvas
     *
     * @param graphView corresponding graphview
     * @param canvas canvas
     * @param isSecondScale whether we are plotting the second scale or not
     */
    @Override
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
        super.getCustomPaint().setTextAlign(Paint.Align.CENTER);
        if (this.getValuesOnTopSize() == 0) {
            this.setValuesOnTopSize(graphView.getGridLabelRenderer().getTextSize());
        }
        this.getCustomPaint().setTextSize(this.getValuesOnTopSize());

        resetDataPoints();

        // get data
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);

        double maxY;
        double minY;
        if (isSecondScale) {
            maxY = graphView.getSecondScale().getMaxY(false);
            minY = graphView.getSecondScale().getMinY(false);
        } else {
            maxY = graphView.getViewport().getMaxY(false);
            minY = graphView.getViewport().getMinY(false);
        }

        // Iterate through all bar graph series
        // so we know how wide to make our bar,
        // and in what position to put it in
        int numBarSeries = 0;
        int currentSeriesOrder = 0;
        int numValues = 0;
        boolean isCurrentSeries;
        SortedSet<Double> xVals = new TreeSet<Double>();
        for(Series inspectedSeries: graphView.getSeries()) {
            if(inspectedSeries instanceof BarGraphSeries) {
                isCurrentSeries = (inspectedSeries == this);
                if(isCurrentSeries) {
                    currentSeriesOrder = numBarSeries;
                }
                numBarSeries++;

                // calculate the number of slots for bars based on the minimum distance between
                // x coordinates in the series.  This is divided into the range to find
                // the placement and width of bar slots
                // (sections of the x axis for each bar or set of bars)
                // TODO: Move this somewhere more general and cache it, so we don't recalculate it for each series
                Iterator<DataPoint> curValues = inspectedSeries.getValues(minX, maxX);
                if (curValues.hasNext()) {
                    xVals.add(curValues.next().getX());
                    if(isCurrentSeries) { numValues++; }
                    while (curValues.hasNext()) {
                        xVals.add(curValues.next().getX());
                        if(isCurrentSeries) { numValues++; }
                    }
                }
            }
        }
        if (numValues == 0) {
            return;
        }

        double minGap = 0;

        if(super.getDataWidth() > 0.0) {
            minGap = super.getDataWidth();
        } else {
            Double lastVal = null;

            for(Double curVal: xVals) {
                if(lastVal != null) {
                    double curGap = Math.abs(curVal - lastVal);
                    if (minGap == 0 || (curGap > 0 && curGap < minGap)) {
                        minGap = curGap;
                    }
                }
                lastVal = curVal;
            }
        }

        int numBarSlots = (minGap == 0) ? 1 : (int)Math.round((maxX - minX)/minGap) + 1;

        Iterator<DataPoint> values = getValues(minX, maxX);

        // Calculate the overall bar slot width - this includes all bars across
        // all series, and any spacing between sets of bars
        int barSlotWidth = numBarSlots == 1
                ? graphView.getGraphContentWidth()
                : graphView.getGraphContentWidth() / (numBarSlots-1);

        // Total spacing (both sides) between sets of bars
        double spacing = Math.min(barSlotWidth*super.getSpacing()/100, barSlotWidth*0.98f);
        // Width of an individual bar
        double barWidth = (barSlotWidth - spacing) / numBarSeries;
        // Offset from the center of a given bar to start drawing
        double offset = barSlotWidth/2;

        double diffY = maxY - minY;
        double diffX = maxX - minX;
        double contentHeight = graphView.getGraphContentHeight();
        double contentWidth = graphView.getGraphContentWidth();
        double contentLeft = graphView.getGraphContentLeft();
        double contentTop = graphView.getGraphContentTop();

        // draw data
        int i=0;
        while (values.hasNext()) {
            DataPoint value = (DataPoint)values.next();

            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = contentHeight * ratY;

            double valY0 = 0 - minY;
            double ratY0 = valY0 / diffY;
            double y0 = contentHeight * ratY0;

            double valueX = value.getX();
            double valX = valueX - minX;
            double ratX = valX / diffX;
            double x = contentWidth * ratX;

            // hook for value dependent color
            if (getValueDependentColor() != null) {

                DataPoint currr = new DataPoint(i,i);

                this.getCustomPaint().setColor(getValueDependentColor().get(currr));
            } else {
                this.getCustomPaint().setColor(getColor());
            }

            double left = x + contentLeft - offset + spacing/2 + currentSeriesOrder*barWidth;
            double top = (contentTop - y) + contentHeight;
            double right = left + barWidth;
            double bottom = (contentTop - y0) + contentHeight - (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);

            boolean reverse = top > bottom;

         /* TODO: fix needed?
            if (this.isAnimated()) {
                if ((Double.isNaN(mLastAnimatedValue) || mLastAnimatedValue < valueX)) {
                    long currentTime = System.currentTimeMillis();
                    if (mAnimationStart == 0) {
                        // start animation
                        mAnimationStart = currentTime;
                        mAnimationStartFrameNo = 0;
                    } else {
                        // anti-lag: wait a few frames
                        if (mAnimationStartFrameNo < 15) {
                            // second time
                            mAnimationStart = currentTime;
                            mAnimationStartFrameNo++;
                        }
                    }
                    float timeFactor = (float) (currentTime-mAnimationStart) / ANIMATION_DURATION;
                    float factor = mAnimationInterpolator.getInterpolation(timeFactor);
                    if (timeFactor <= 1.0) {
                        double barHeight = bottom - top;
                        barHeight = barHeight * factor;
                        top = bottom-barHeight;
                        ViewCompat.postInvalidateOnAnimation(graphView);
                    } else {
                        // animation finished
                        mLastAnimatedValue = valueX;
                    }
                }
            }*/

            if (reverse) {
                double tmp = top;
                top = bottom + (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);
                bottom = tmp;
            }

            // overdraw
            left = Math.max(left, contentLeft);
            right = Math.min(right, contentLeft+contentWidth);
            bottom = Math.min(bottom, contentTop+contentHeight);
            top = Math.max(top, contentTop);

            //TODO
       //     mDataPoints.put(new RectD(left, top, right, bottom), value);

            Paint p;
            if (this.getCustomPaint() != null) {
                p = this.getCustomPaint();
            } else {
                p = this.getCustomPaint();
            }
            canvas.drawRect((float)left, (float)top, (float)right, (float)bottom, p);

            // set values on top of graph
            if (this.isDrawValuesOnTop()) {
                if (reverse) {
                    top = bottom + this.getValuesOnTopSize() + 4;
                    if (top > contentTop+contentHeight) top = contentTop + contentHeight;
                } else {
                    top -= 4;
                    if (top<=contentTop) top+=contentTop+4;
                }

                this.getCustomPaint().setColor(this.getValuesOnTopColor());
                canvas.drawText(
                        graphView.getGridLabelRenderer().getLabelFormatter().formatLabel(value.getY(), false)
                        , (float) (left+right)/2, (float) top, this.getCustomPaint());
            }

            i++;
        }
    }
}
