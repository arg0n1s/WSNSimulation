package wsnsimulation.core.geometry;

import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface VectorShape {
	
	public Set<Vector3D> getPointsOnHull();

	public void createHullPoints();
	
	public boolean lineIntersectsShape(Line line);
	
	public boolean lineSegmentIntersectsShape(Line line, Vector3D start, Vector3D end);
	
	public boolean isInBody(Vector3D queryPoint);
	
	public boolean isOnHull(Vector3D queryPoint);
}
