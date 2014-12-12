**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.model.Point;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * The time chart rendering class.
 */
public class TimeChart extends LineChart {
  /** The constant to identify this chart type. */
  public static final String TYPE = "Time";
  /** The number of milliseconds in a day. */
  public static final long DAY = 24 * 60 * 60 * 1000;
  /** The date format pattern to be used in formatting the X axis labels. */
  private String mDateFormat;
  /** The starting point for labels. */
  private Double mStartPoint;
  
  private float mFirstMultiplier;

  private float mSecondMultiplier;

  /** A path measure for retrieving the points on the path. */
  private PathMeasure mPathMeasure;
  
  
  TimeChart() {
    // default is to have first control point at about 33% of the distance,
    mFirstMultiplier = 0.33f;
    // and the next at 66% of the distance.
    mSecondMultiplier = 1 - mFirstMultiplier;
  }

  /**
   * Builds a new time chart instance.
   * 
   * @param dataset the multiple series dataset
   * @param renderer the multiple series renderer
   */
  public TimeChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, float smoothness) {
    super(dataset, renderer);
    mFirstMultiplier = smoothness;
    mSecondMultiplier = 1 - mFirstMultiplier;
  }
  
  @Override
  protected void drawPath(Canvas canvas, List<Float> points, Paint paint, boolean circular) {

    if (mFirstMultiplier > 0)
    {
      Path p = new Path();
      float x = points.get(0);
      float y = points.get(1);
      p.moveTo(x, y); 

      int length = points.size();
      if (circular) {
        length -= 4;
      }
  
      Point p1 = new Point();
      Point p2 = new Point();
      Point p3 = new Point();
      for (int i = 0; i < length; i += 2) {
        int nextIndex = i + 2 < length ? i + 2 : i;
        int nextNextIndex = i + 4 < length ? i + 4 : nextIndex;
        calc(points, p1, i, nextIndex, mSecondMultiplier);
        p2.setX(points.get(nextIndex));
        p2.setY(points.get(nextIndex + 1));
        calc(points, p3, nextIndex, nextNextIndex, mFirstMultiplier);
        // From last point, approaching x1/y1 and x2/y2 and ends up at x3/y3
        p.cubicTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
      }
      mPathMeasure = new PathMeasure(p, false);
      if (circular) {
        for (int i = length; i < length + 4; i += 2) {
          p.lineTo(points.get(i), points.get(i + 1));
        }
        p.lineTo(points.get(0), points.get(1));
      }    
      canvas.drawPath(p, paint);
    }
    else
      super.drawPath(canvas, points, paint, circular);
  }

  private void calc(List<Float> points, Point result, int index1, int index2, final float multiplier) {
    float p1x = points.get(index1);
    float p1y = points.get(index1 + 1);
    float p2x = points.get(index2);
    float p2y = points.get(index2 + 1);

    float diffX = p2x - p1x; // p2.x - p1.x;
    float diffY = p2y - p1y; // p2.y - p1.y;
    result.setX(p1x + (diffX * multiplier));
    result.setY(p1y + (diffY * multiplier));
  }

  /**
   * Draws the series points.
   * 
   * @param canvas the canvas
   * @param paint the paint object
   * @param pointsList the points to be rendered
   * @param seriesRenderer the series renderer
   * @param yAxisValue the y axis value in pixels
   * @param seriesIndex the series index
   * @param startIndex the start index of the rendering points
   */
  protected void drawPoints(Canvas canvas, Paint paint, List<Float> pointsList,
      XYSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
    if (isRenderPoints(seriesRenderer)) {
      ScatterChart pointsChart = getPointsChart();
      if (pointsChart != null) {
        if (mFirstMultiplier > 0)
        {
          int length = (int) mPathMeasure.getLength();
          int pointsLength = pointsList.size();
          float[] coords = new float[2];
          for (int i = 0; i < length; i++) {
            mPathMeasure.getPosTan(i, coords, null);
            double prevDiff = Double.MAX_VALUE;
            boolean ok = true;
            for (int j = 0; j < pointsLength && ok; j += 2) {
              double diff = Math.abs(pointsList.get(j) - coords[0]);
              if (diff < 1) {
                pointsList.set(j + 1, coords[1]);
                prevDiff = diff;
              }
              ok = prevDiff > diff;
            }
          }
        }
        pointsChart.drawSeries(canvas, paint, pointsList, seriesRenderer, yAxisValue, seriesIndex,
            startIndex);
      }
    }
  }

  /**
   * Returns the date format pattern to be used for formatting the X axis
   * labels.
   * 
   * @return the date format pattern for the X axis labels
   */
  public String getDateFormat() {
    return mDateFormat;
  }

  /**
   * Sets the date format pattern to be used for formatting the X axis labels.
   * 
   * @param format the date format pattern for the X axis labels. If null, an
   *          appropriate default format will be used.
   */
  public void setDateFormat(String format) {
    mDateFormat = format;
  }

  /**
   * The graphical representation of the labels on the X axis.
   * 
   * @param xLabels the X labels values
   * @param xTextLabelLocations the X text label locations
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param left the left value of the labels area
   * @param top the top value of the labels area
   * @param bottom the bottom value of the labels area
   * @param xPixelsPerUnit the amount of pixels per one unit in the chart labels
   * @param minX the minimum value on the X axis in the chart
   * @param maxX the maximum value on the X axis in the chart
   */
  @Override
  protected void drawXLabels(List<Double> xLabels, Double[] xTextLabelLocations, Canvas canvas,
      Paint paint, int left, int top, int bottom, double xPixelsPerUnit, double minX, double maxX) {
    int length = xLabels.size();
    if (length > 0) {
      boolean showLabels = mRenderer.isShowLabels();
      boolean showGridY = mRenderer.isShowGridY();
      DateFormat format = getDateFormat(xLabels.get(0), xLabels.get(length - 1));
      for (int i = 0; i < length; i++) {
        long label = Math.round(xLabels.get(i));
        float xLabel = (float) (left + xPixelsPerUnit * (label - minX));
        if (showLabels) {
          paint.setColor(mRenderer.getXLabelsColor());
          canvas
              .drawLine(xLabel, bottom, xLabel, bottom + mRenderer.getLabelsTextSize() / 3, paint);
          drawText(canvas, format.format(new Date(label)), xLabel,
              bottom + mRenderer.getLabelsTextSize() * 4 / 3 + mRenderer.getXLabelsPadding(), paint, mRenderer.getXLabelsAngle());
        }
        if (showGridY) {
          paint.setColor(mRenderer.getGridColor(0));
          canvas.drawLine(xLabel, bottom, xLabel, top, paint);
        }
      }
    }
    drawXTextLabels(xTextLabelLocations, canvas, paint, true, left, top, bottom, xPixelsPerUnit,
        minX, maxX);
  }

  /**
   * Returns the date format pattern to be used, based on the date range.
   * 
   * @param start the start date in milliseconds
   * @param end the end date in milliseconds
   * @return the date format
   */
  private DateFormat getDateFormat(double start, double end) {
    if (mDateFormat != null) {
      SimpleDateFormat format = null;
      try {
        format = new SimpleDateFormat(mDateFormat);
        return format;
      } catch (Exception e) {
        // do nothing here
      }
    }
    DateFormat format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    double diff = end - start;
    if (diff > DAY && diff < 5 * DAY) {
      format = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
    } else if (diff < DAY) {
      format = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);
    }
    return format;
  }

  /**
   * Returns the chart type identifier.
   * 
   * @return the chart type
   */
  public String getChartType() {
    return TYPE;
  }

  @Override
  protected List<Double> getXLabels(double min, double max, int count) {
    final List<Double> result = new ArrayList<Double>();
    if (!mRenderer.isXRoundedLabels()) {
      if (mDataset.getSeriesCount() > 0) {
        XYSeries series = mDataset.getSeriesAt(0);
        int length = series.getItemCount();
        int intervalLength = 0;
        int startIndex = -1;
        for (int i = 0; i < length; i++) {
          double value = series.getX(i);
          if (min <= value && value <= max) {
            intervalLength++;
            if (startIndex < 0) {
              startIndex = i;
            }
          }
        }
        if (intervalLength < count) {
          for (int i = startIndex; i < startIndex + intervalLength; i++) {
            result.add(series.getX(i));
          }
        } else {
          float step = (float) intervalLength / count;
          int intervalCount = 0;
          for (int i = 0; i < length && intervalCount < count; i++) {
            double value = series.getX(Math.round(i * step));
            if (min <= value && value <= max) {
              result.add(value);
              intervalCount++;
            }
          }
        }
        return result;
      } else {
        return super.getXLabels(min, max, count);
      }
    }
    if (mStartPoint == null) {
      mStartPoint = min - (min % DAY) + DAY + new Date(Math.round(min)).getTimezoneOffset() * 60
          * 1000;
    }
    if (count > 25) {
      count = 25;
    }

    
    final double cycleMath = (max - min) / count;
    if (cycleMath <= 0) {
      return result;
    }
    double cycle = DAY;

    if (cycleMath <= DAY) {
      while (cycleMath < cycle / 2) {
        cycle = cycle / 2;
      }
    } else {
      while (cycleMath > cycle) {
        cycle = cycle * 2;
      }
    }

    double val = mStartPoint - Math.floor((mStartPoint - min) / cycle) * cycle;
    int i = 0;
    while (val < max && i++ <= count) {
      result.add(val);
      val += cycle;
    }

    return result;
  }
}

