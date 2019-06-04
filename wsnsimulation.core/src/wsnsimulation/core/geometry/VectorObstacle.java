package wsnsimulation.core.geometry;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wsnSimulationModel.Obstacle;

public abstract class VectorObstacle extends VectorSimulationObject implements VectorShape{
	
	protected Set<Vector3D> points;

	public VectorObstacle(Obstacle obstacle) {
		super(obstacle);
		createHullPoints();
	}

	@Override
	public Set<Vector3D> getPointsOnHull() {
		return points;
	}
	
	@Override
	public void setPosition(Vector3D position) {
		Vector3D translation = position.subtract(this.position);
		super.setPosition(position);
		points = points.stream().map(point -> point.add(translation)).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	@Override
	public void moveBy(Vector3D translation) {
		super.moveBy(translation);
		points = points.stream().map(point -> point.add(translation)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
