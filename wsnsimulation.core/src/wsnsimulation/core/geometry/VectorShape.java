package wsnsimulation.core.geometry;

import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface VectorShape {
	
	public Set<Vector3D> getPointsOnHull();

	public void createHullPoints();
}
