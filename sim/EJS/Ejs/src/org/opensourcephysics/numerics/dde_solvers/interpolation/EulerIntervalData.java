/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

/**
 * Takes a (second order) Euler step to interpolate data for an interval
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class EulerIntervalData extends IntervalData {
	private double[] mLeftState, mLeftRate;
	
	public EulerIntervalData(double[] aState, double[] aRate, double right) {
    int dimension = aState.length;
    mLeftState = new double[dimension]; System.arraycopy(aState,0,mLeftState,0,dimension);
    mLeftRate  = new double[dimension]; System.arraycopy(aRate, 0,mLeftRate, 0,dimension);
    mLeft = aState[dimension-1]; 
    mRight = right; 
	}
	
  public double interpolate(double time, int index) {
    double step = time - mLeft;
    return mLeftState[index] + step*mLeftRate[index];
  }

  public double[] interpolate(double time, double[] state, int beginIndex, int length) {
    double step = time - mLeft;
    for (int index=beginIndex, i=0; i<length; index++, i++) {
      state[i] = mLeftState[index] + step*mLeftRate[index];
    }
    return state;
  }

}

/* 
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
