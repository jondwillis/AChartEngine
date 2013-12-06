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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.model.Point;

/**
 * DoughnutChart Segment Selection Management.
 * @author andyzg
 */
public class DoughnutMapper  extends PieMapper implements Serializable {
  
  /** List of all of the segments. PieSegment can be used rather than making a new class */
  private List<PieSegment> mPieSegmentList = new ArrayList<PieSegment>();
  
  /** The radius of the empty and Dougnut circle */ 
  private int mInnerRadius, mOuterRadius;

  
  /** Amount squared allowed if touches edge of doughnut slice */
  private static final int MARGIN_TOUCH = 500;
  
  public void setDimensions(int innerRadius, int outerRadius, int centerX, int centerY)
  {
    mInnerRadius = innerRadius;
    mOuterRadius = outerRadius;
    mCenterX = centerX;
    mCenterY = centerY;
  }
  

  /**
   * Checks if Point falls within the DoughnutChart
   * 
   * @param screenPoint
   * @return true if in PieChart
   */
  public boolean isOnPieChart(Point screenPoint) {
    // Using a bit of Pythagoras
    // inside circle if (x-center_x)**2 + (y-center_y)**2 <= radius**2:

    double sqValue = (Math.pow(mCenterX - screenPoint.getX(), 2) + Math.pow(
        mCenterY - screenPoint.getY(), 2));

    double outerRadiusSquared = mOuterRadius * mOuterRadius;
    double innerRadiusSquared = mInnerRadius * mInnerRadius;
    boolean isOnPieChart = sqValue <= outerRadiusSquared + MARGIN_TOUCH && sqValue >= innerRadiusSquared - MARGIN_TOUCH;
    return isOnPieChart;
  }

}
