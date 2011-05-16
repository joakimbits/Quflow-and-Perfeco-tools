/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementTesselation</p>
 * <p>Description: A Tesselation is a collection of planar tiles</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementTessellation extends Element {
  // Configuration variables
  protected double[][][] data = new double[0][0][3];

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementTessellation(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementTessellation(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the data of the tiles. Each tile is a polygon with possible different number of vertex.
   * @param data the double[][][3] array of coordinates for the surface.
   */
  public void setTiles(double[][][] data) {
    this.data = data;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the data of the surface.
   * @return the double[nu][nv][3] the actual array of coordinates of the surface (not a copy)
   */
  public double[][][] getTiles() { return this.data; }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------

  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    for(int i = 0,n=data.length; i<n; i++) {
      for(int j = 0,sides=data[i].length; j<sides; j++) {
        System.arraycopy(data[i][j], 0, aPoint, 0, 3);
        sizeAndToSpaceFrame(aPoint);
        minX = Math.min(minX, aPoint[0]);
        maxX = Math.max(maxX, aPoint[0]);
        minY = Math.min(minY, aPoint[1]);
        maxY = Math.max(maxY, aPoint[1]);
        minZ = Math.min(minZ, aPoint[2]);
        maxZ = Math.max(maxZ, aPoint[2]);
      }
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
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
