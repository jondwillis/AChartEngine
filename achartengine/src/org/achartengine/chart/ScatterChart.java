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

import java.util.List;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.SerializableBitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * The scatter chart rendering class.
 */
public class ScatterChart extends XYChart {
  /** The constant to identify this chart type. */
  public static final String TYPE = "Scatter";
  /** The default point shape size. */
  private static final float SIZE = 3;
  /** The legend shape width. */
  private static final int SHAPE_WIDTH = 10;
  /** The point shape size. */
  private float size = SIZE;

  ScatterChart() {
  }

  /**
   * Builds a new scatter chart instance.
   * 
   * @param dataset the multiple series dataset
   * @param renderer the multiple series renderer
   */
  public ScatterChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
    super(dataset, renderer);
    size = renderer.getPointSize();
  }

  // TODO: javadoc
  protected void setDatasetRenderer(XYMultipleSeriesDataset dataset,
      XYMultipleSeriesRenderer renderer) {
    super.setDatasetRenderer(dataset, renderer);
    size = renderer.getPointSize();
  }

  /**
   * The graphical representation of a series.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param points the array of points to be used for drawing the series
   * @param seriesRenderer the series renderer
   * @param yAxisValue the minimum value of the y axis
   * @param seriesIndex the index of the series currently being drawn
   * @param startIndex the start index of the rendering points
   */
  @Override
  public void drawSeries(Canvas canvas, Paint paint, List<Float> points,
      XYSeriesRenderer renderer, float yAxisValue, int seriesIndex, int startIndex) {
    paint.setColor(renderer.getColor());
    final float stroke = paint.getStrokeWidth();
    if (renderer.isFillPoints()) {
      paint.setStyle(Style.FILL);
    } else {
      paint.setStrokeWidth(renderer.getPointStrokeWidth());
      paint.setStyle(Style.STROKE);
    }
    int length = points.size();
    switch (renderer.getPointStyle()) {
    case X:
      paint.setStrokeWidth(renderer.getPointStrokeWidth());
      for (int i = 0; i < length; i += 2) {
        drawX(canvas, paint, points.get(i), points.get(i + 1));
      }
      break;
    case CIRCLE:
      for (int i = 0; i < length; i += 2) {
        drawCircle(canvas, paint, points.get(i), points.get(i + 1));
      }
      break;
    case TRIANGLE:
      float[] path = new float[6];
      for (int i = 0; i < length; i += 2) {
        drawTriangle(canvas, paint, path, points.get(i), points.get(i + 1));
      }
      break;
    case SQUARE:
      for (int i = 0; i < length; i += 2) {
        drawSquare(canvas, paint, points.get(i), points.get(i + 1));
      }
      break;
    case DIAMOND:
      path = new float[8];
      for (int i = 0; i < length; i += 2) {
        drawDiamond(canvas, paint, path, points.get(i), points.get(i + 1));
      }
      break;
    case IMAGE:
      SerializableBitmap sb = renderer.getBitmap();
      Bitmap bitmap = null;
      if(sb!=null){
        bitmap = sb.getBitmap();
      }
      for (int i = 0; i < length; i += 2) {
        drawImage(canvas, paint, points.get(i), points.get(i + 1), bitmap);
      }
      break;
    case POINT:
      for (int i = 0; i < length; i += 2) {
        canvas.drawPoint(points.get(i), points.get(i + 1), paint);
      }
      break;
    }
    paint.setStrokeWidth(stroke);
  }

  @Override
  protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values,
      float yAxisValue, int seriesIndex, int startIndex) {
    int length = points.size();
    ClickableArea[] ret = new ClickableArea[length / 2];
    for (int i = 0; i < length; i += 2) {
      int selectableBuffer = mRenderer.getSelectableBuffer();
      ret[i / 2] = new ClickableArea(new RectF(points.get(i) - selectableBuffer, points.get(i + 1)
          - selectableBuffer, points.get(i) + selectableBuffer, points.get(i + 1)
          + selectableBuffer), values.get(i), values.get(i + 1));
    }
    return ret;
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
    if (((XYSeriesRenderer) renderer).isFillPoints()) {
      paint.setStyle(Style.FILL);
    } else {
      paint.setStyle(Style.STROKE);
    }
    switch (((XYSeriesRenderer) renderer).getPointStyle()) {
    case X:
      drawX(canvas, paint, x + SHAPE_WIDTH, y);
      break;
    case CIRCLE:
      drawCircle(canvas, paint, x + SHAPE_WIDTH, y);
      break;
    case TRIANGLE:
      drawTriangle(canvas, paint, new float[6], x + SHAPE_WIDTH, y);
      break;
    case SQUARE:
      drawSquare(canvas, paint, x + SHAPE_WIDTH, y);
      break;
    case DIAMOND:
      drawDiamond(canvas, paint, new float[8], x + SHAPE_WIDTH, y);
      break;
    case IMAGE:
      SerializableBitmap sb = ((XYSeriesRenderer)renderer).getBitmap();
      Bitmap bitmap = null;
      if(sb!=null){
        bitmap = sb.getBitmap();
      }
      drawImage(canvas, paint, x, y, bitmap);
      break;
    case POINT:
      canvas.drawPoint(x + SHAPE_WIDTH, y, paint);
      break;
    default:
      break;
    }
  }

  /**
   * The graphical representation of an X point shape.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   */
  private void drawX(Canvas canvas, Paint paint, float x, float y) {
    canvas.drawLine(x - size, y - size, x + size, y + size, paint);
    canvas.drawLine(x + size, y - size, x - size, y + size, paint);
  }

  /**
   * The graphical representation of a circle point shape.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   */
  private void drawCircle(Canvas canvas, Paint paint, float x, float y) {
    canvas.drawCircle(x, y, size, paint);
  }

  /**
   * The graphical representation of a triangle point shape.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param path the triangle path
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   */
  private void drawTriangle(Canvas canvas, Paint paint, float[] path, float x, float y) {
    path[0] = x;
    path[1] = y - size - size / 2;
    path[2] = x - size;
    path[3] = y + size;
    path[4] = x + size;
    path[5] = path[3];
    drawPath(canvas, path, paint, true);
  }

  /**
   * The graphical representation of a square point shape.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   */
  private void drawSquare(Canvas canvas, Paint paint, float x, float y) {
    canvas.drawRect(x - size, y - size, x + size, y + size, paint);
  }

  /**
   * The graphical representation of a diamond point shape.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param path the diamond path
   * @param x the x value of the point the shape should be drawn at
   * @param y the y value of the point the shape should be drawn at
   */
  private void drawDiamond(Canvas canvas, Paint paint, float[] path, float x, float y) {
    path[0] = x;
    path[1] = y - size;
    path[2] = x - size;
    path[3] = y;
    path[4] = x;
    path[5] = y + size;
    path[6] = x + size;
    path[7] = y;
    drawPath(canvas, path, paint, true);
  }

  /**
   * The graphical representation of an image.
   * 
   * @param canvas the canvas to paint to
   * @param paint the paint to be used for drawing
   * @param x the x value of the point the image should be drawn at
   * @param y the y value of the point the image should be drawn at
   * @param bitmap the image to be drawn
   */
  private void drawImage(Canvas canvas, Paint paint, float x, float y, Bitmap bitmap) {
    if(bitmap==null){
      canvas.drawRect(x - size, y - size, x + size, y + size, paint);
    } else {
      canvas.drawBitmap(bitmap, x-bitmap.getWidth()/2, y-bitmap.getHeight()/2, paint);
    }
  }

  /**
   * Returns the chart type identifier.
   * 
   * @return the chart type
   */
  public String getChartType() {
    return TYPE;
  }

}