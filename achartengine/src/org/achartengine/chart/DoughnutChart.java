/**
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.Point;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

/**
 * The doughnut chart rendering class.
 */
public class DoughnutChart extends RoundChart {
  /** The series dataset. */
  private MultipleCategorySeries mDataset;
  /** A step variable to control the size of the legend shape. */
  private int mStep;
  
  /** Handles returning values when tapping on Doughnut Chart */
  private DoughnutMapper mDoughnutMapper;

  /**
   * Builds a new doughnut chart instance.
   * 
   * @param dataset the series dataset
   * @param renderer the series renderer
   */
  public DoughnutChart(MultipleCategorySeries dataset, DefaultRenderer renderer) {
    super(null, renderer);
    mDataset = dataset;
    mDoughnutMapper = new DoughnutMapper();
  }

  /**
   * The graphical representation of the doughnut chart.
   * 
   * @param canvas the canvas to paint to
   * @param x the top left x value of the view to draw to
   * @param y the top left y value of the view to draw to
   * @param width the width of the view to draw to
   * @param height the height of the view to draw to
   * @param paint the paint
   */
  @Override
  public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
    paint.setAntiAlias(mRenderer.isAntialiasing());
    paint.setStyle(Style.FILL);
    paint.setTextSize(mRenderer.getLabelsTextSize());
    
    // List of the labels on the doughnut
    List<RectF> prevLabelsBounds = new ArrayList<RectF>();
    
    // Defining the shape of the doughnut. Bottom is defined after legend
    int marginAngle = 3;
    int legendSize = getLegendSize(mRenderer, height / 5, 0);
    int left = x;
    int top = y;
    int right = x + width;
    
    // Defining how much data categories there are
    int cLength = mDataset.getCategoriesCount();
    String[] categories = new String[cLength];
    for (int category = 0; category < cLength; category++) {
      categories[category] = mDataset.getCategory(category);
    }
    
    // If a legend fits, add one
    if (mRenderer.isFitLegend()) {
      legendSize = drawLegend(canvas, mRenderer, categories, left, right, y, width, height,
          legendSize, paint, true);
    }
    int bottom = y + height - legendSize;
    
    // Draw the background of the screen
    drawBackground(mRenderer, canvas, x, y, width, height, paint, false, DefaultRenderer.NO_COLOR);
    // Used for the legend
    mStep = SHAPE_WIDTH * 3 / 4;

    
    int mRadius = Math.min(Math.abs(right - left), Math.abs(bottom - top));
    // Used to define the scale of the radius of the doughnut
    double rCoef = 0.4 * mRenderer.getScale();
    // Used to define the scale of the width of the doughnut
    double decCoef = 0.1 / cLength;
    
    // Radius of the doughnut
    int radius = (int) (mRadius * rCoef);
    // Radius of doughnut hole
    int holeRadius = (int) (radius - mRadius * decCoef);
    
    // Define the center of the doughnut
    if (mCenterX == NO_VALUE) {
      mCenterX = (left + right) / 2;
    }
    if (mCenterY == NO_VALUE) {
      mCenterY = (bottom + top) / 2;
    }
    
    // Hook in clip detection after center has been calculated
    mDoughnutMapper.setDimensions(radius, mCenterX, mCenterY);
    boolean loadDoughnutCfg = !mDoughnutMapper.areAllSegmentPresent(cLength);
    if (loadDoughnutCfg) {
      mDoughnutMapper.clearPieSegments();
    }
    mDoughnutMapper.setDimensions(radius - (int)(mRadius*decCoef), radius, mCenterX, mCenterY);
    
    // Distance for the legend labels
    float shortLegendRadius = radius * 0.9f;
    float longLegendRadius = radius * 1.1f;
    
    // Add a transparent center to the doughnut
    Rect oldClipBounds = canvas.getClipBounds();
    Path doughnutHole = new Path();
    doughnutHole.addCircle(mCenterX, mCenterY, holeRadius, Path.Direction.CCW);
    canvas.clipPath(doughnutHole, Region.Op.DIFFERENCE);
    
    
    for (int category = 0; category < cLength; category++) {
      
      // Fetch all of the informatoin in the category
      int sLength = mDataset.getItemCount(category);
      double total = 0;
      String[] titles = new String[sLength];
      for (int i = 0; i < sLength; i++) {
        total += mDataset.getValues(category)[i];
        titles[i] = mDataset.getTitles(category)[i];
      }
      
      // Define the characteristics for the category
      float currentAngle = mRenderer.getStartAngle();
      RectF oval = new RectF(mCenterX - radius, mCenterY - radius, mCenterX + radius, mCenterY
          + radius);
      for (int i = 0; i < sLength; i++) {
        paint.setColor(mRenderer.getSeriesRendererAt(i).getColor());
        float value = (float) mDataset.getValues(category)[i];
        float angle = (float) (value / total * 360);
        
        // Draw the arc and the label for the category
        canvas.drawArc(oval, currentAngle+marginAngle, angle-marginAngle, true, paint);
        drawLabel(canvas, mDataset.getTitles(category)[i], mRenderer, prevLabelsBounds, mCenterX,
            mCenterY, shortLegendRadius, longLegendRadius, currentAngle, angle, left, right,
            mRenderer.getLabelsColor(), paint, true, false);
        
        // Save details for getSeries functionality
        if (loadDoughnutCfg) {
          mDoughnutMapper.addPieSegment(i, value, currentAngle, angle);
        }
        currentAngle += angle;
      }
      
      // Checking the background of the renderer. Defining a color
      if (mRenderer.getBackgroundColor() != 0) {
        paint.setColor(mRenderer.getBackgroundColor());
      } else {
        paint.setColor(Color.WHITE);
      }
      paint.setStyle(Style.FILL);
      shortLegendRadius -= mRadius * decCoef - 2;
    }
    canvas.clipRect(oldClipBounds, Region.Op.REPLACE);
    prevLabelsBounds.clear();
    drawLegend(canvas, mRenderer, mDataset.getTitles(0), left, right, y, width, height, legendSize, paint,
        false);
    drawTitle(canvas, x, y, width, paint);
    
    // Display the values at the center
    if (mRenderer.isCenterDisplay() != -1)
    {
      Paint textPaint = new Paint();
      textPaint.setTextAlign(Align.CENTER);
      textPaint.setTextSize(60);
      textPaint.setColor(mRenderer.getDisplayColor());
      drawString(canvas, 
          mDataset.getTitles(0)[mRenderer.isCenterDisplay()]+"\n"+
     mDataset.getValues(0)[mRenderer.isCenterDisplay()]
          , mCenterX, mCenterY, textPaint);
    }
  }

  /**
   * Returns the legend shape width.
   * 
   * @param seriesIndex the series index
   * @return the legend shape width
   */
  public int getLegendShapeWidth(int seriesIndex) {
    return SHAPE_WIDTH;
  }

  /**
   * The graphical representation of the legend shape.
   * 
   * @param canvas the canvas to paint to
   * @param renderer the series renderer
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   * @param seriesIndex the series index
   * @param paint the paint to be used for drawing
   */
  public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y,
      int seriesIndex, Paint paint) {
    // mStep--;
    canvas.drawCircle(x + SHAPE_WIDTH - mStep, y - mStep, mStep, paint);
  }

  @Override
  public SeriesSelection getSeriesAndPointForScreenCoordinate(Point screenPoint) {
    return mDoughnutMapper.getSeriesAndPointForScreenCoordinate(screenPoint);
  }

 /**
  * Draw a multiple lines string.
  * 
  * @param canvas the canvas to paint to
  * @param text the text to be painted
  * @param x the x value of the area to draw to
  * @param y the y value of the area to draw to
  * @param paint the paint to be used for drawing
  */
 public void drawString(Canvas canvas, String text, float x, float y, Paint paint) {
   if (text != null) {
     String[] lines = text.split("\n");
     Rect rect = new Rect();
     int yOff = 0;
     for (int i = 0; i < lines.length; ++i) {
       canvas.drawText(lines[i], x, y + yOff, paint);
       paint.getTextBounds(lines[i], 0, lines[i].length(), rect);
       yOff = yOff + rect.height() + 5; // space between lines is 5
     }
   }
 }
}
