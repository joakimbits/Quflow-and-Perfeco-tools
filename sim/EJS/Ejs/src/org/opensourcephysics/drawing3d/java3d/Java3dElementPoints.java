package org.opensourcephysics.drawing3d.java3d;

import java.awt.Color;

import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPoints;

/**
 * <p>Title: Java3dElementPoints</p>
 * <p>Description: A group of points using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */ 
public class Java3dElementPoints extends Java3dElement {

	//Java 3D variables
	private PointArray points;
	private Shape3D shape;
	
	public Java3dElementPoints (ElementPoints _element){
		super(_element);
		getAppearance().getPointAttributes().setPointSize(1.0f);
		getAppearance().getPointAttributes().setPointAntialiasingEnable(true);
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		
		if ((_change & Element.CHANGE_POSITION) != 0 ) {
			int npoints = ((ElementPoints)element).getData().length;
			if(npoints<=0) return;
			points = new PointArray(npoints, PointArray.COORDINATES|PointArray.COLOR_3);
			for(int n = 0; n < npoints;  n++){
				points.setCoordinate(n, new Point3d(((ElementPoints)element).getData()[n][0],
						                              ((ElementPoints)element).getData()[n][1],
						                               ((ElementPoints)element).getData()[n][2]));
				points.setColor(n, new Color3f((Color)element.getStyle().getFillColor()));
			}
			shape = new Shape3D(points, getAppearance());
			addNode(shape);
		}
		if ((_change & Element.CHANGE_COLOR) != 0) {
			for(int n = 0; n < ((ElementPoints)element).getData().length;  n++)
				points.setColor(n, new Color3f((Color)element.getStyle().getFillColor()));
		}
	}
		
	@Override
	public boolean isPrimitive() {return false;}

}
