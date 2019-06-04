package wsnsimulation.core.geometry;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wsnSimulationModel.Rectangle;

public class VectorRectangle extends VectorObstacle{
	
	public VectorRectangle(Rectangle rect) {
		super(rect);
	}
	
	@Override
	public void createHullPoints() {
		Rectangle rect = (Rectangle) object;
		Vector3D c1 = position.add(Vector3D.PLUS_I.scalarMultiply(rect.getLength()/2.0)).add(Vector3D.PLUS_J.scalarMultiply(rect.getWidth()/2.0));
		Vector3D c2 = position.add(Vector3D.PLUS_I.scalarMultiply(-rect.getLength()/2.0)).add(Vector3D.PLUS_J.scalarMultiply(rect.getWidth()/2.0));
		Vector3D c3 = position.add(Vector3D.PLUS_I.scalarMultiply(-rect.getLength()/2.0)).add(Vector3D.PLUS_J.scalarMultiply(-rect.getWidth()/2.0));
		Vector3D c4 = position.add(Vector3D.PLUS_I.scalarMultiply(rect.getLength()/2.0)).add(Vector3D.PLUS_J.scalarMultiply(-rect.getWidth()/2.0));
		
		points = new LinkedHashSet<>();
		points.add(c1);
		points.add(c2);
		points.add(c3);
		points.add(c4);
	}
	
}
