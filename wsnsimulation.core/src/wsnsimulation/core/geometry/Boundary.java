package wsnsimulation.core.geometry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.geometry.Point;
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wsnSimulationModel.Bounds;

public class Boundary {
	
	private Map<String, Vector3D> corners = new LinkedHashMap<>();
	private Map<String, Plane> planes = new LinkedHashMap<>();
	public Bounds bounds;
	
	public Boundary(Bounds bounds) {
		double maxZ = bounds.getMaxZ();
		double minZ = bounds.getMinZ();
		if(bounds.getMaxZ() == bounds.getMinZ() && bounds.getMaxZ() == 0) {
			maxZ = 1;
			minZ = -1;
		}
		corners.put("b1", new Vector3D(bounds.getMaxX(), bounds.getMaxY(), maxZ));
		corners.put("b2", new Vector3D(bounds.getMinX(), bounds.getMaxY(), maxZ));
		corners.put("b3", new Vector3D(bounds.getMinX(), bounds.getMinY(), maxZ));
		corners.put("b4", new Vector3D(bounds.getMaxX(), bounds.getMinY(), maxZ));
		corners.put("b5", new Vector3D(bounds.getMaxX(), bounds.getMaxY(), minZ));
		corners.put("b6", new Vector3D(bounds.getMinX(), bounds.getMaxY(), minZ));
		corners.put("b7", new Vector3D(bounds.getMinX(), bounds.getMinY(), minZ));
		corners.put("b8", new Vector3D(bounds.getMaxX(), bounds.getMinY(), minZ));
		
		//planes.put("p1", new Plane(corners.get("b1"), corners.get("b2"), corners.get("b3"), precision));
		planes.put("p2", new Plane(corners.get("b1"), corners.get("b2"), corners.get("b5"), GeometryUtils.precision));
		//planes.put("p3", new Plane(corners.get("b5"), corners.get("b6"), corners.get("b7"), precision));
		planes.put("p4", new Plane(corners.get("b3"), corners.get("b4"), corners.get("b7"), GeometryUtils.precision));
		planes.put("p5", new Plane(corners.get("b1"), corners.get("b4"), corners.get("b5"), GeometryUtils.precision));
		planes.put("p6", new Plane(corners.get("b2"), corners.get("b3"), corners.get("b6"), GeometryUtils.precision));
		
		this.bounds = bounds;
	}
	
	public boolean isInBounds(VectorObject object) {
		if(object instanceof VectorObstacle) {
			VectorObstacle vo = (VectorObstacle) object;
			for(Vector3D point : vo.getPointsOnHull()) {
				if(!isInBounds(point)) {
					return false;
				}
			}
			return true;
		}else{
			return isInBounds(object.getPosition());
		}
	}
	
	public boolean isInBounds(Vector3D position) {
		if(position.getX()>bounds.getMaxX() || position.getX()<bounds.getMinX())
			return false;
		if(position.getY()>bounds.getMaxY() || position.getY()<bounds.getMinY())
			return false;
		if(position.getZ()>bounds.getMaxY() || position.getZ()<bounds.getMinY())
			return false;
		
		return true;
	}
	
	public Vector3D calculateReflection(VectorObject object, double timeStep) {
		Vector3D position = object.getPosition();
		Vector3D velocity = object.getVelocity();
		Plane plane = findNearestPlane(position);
		Line trajectoryLine = trajectoryLine(position, velocity, timeStep);
		
		Vector3D intersection = plane.intersection(trajectoryLine);
		Vector3D projection = (Vector3D) plane.project(position);
		
		Vector3D onPlane = intersection.subtract(projection);
		Vector3D fromProjection = position.subtract(projection);
		
		Vector3D reflectionPoint = intersection.add(onPlane).add(fromProjection);
		Vector3D reflection = reflectionPoint.subtract(intersection).normalize();
		return reflection.scalarMultiply(velocity.getNormInf());
	}
	
	public Plane findNearestPlane(Vector3D queryPoint) {
		Plane nearest = null;
		double distance = Double.MAX_VALUE;
		for(Plane plane : planes.values()) {
			double currentDistance = Math.abs(plane.getOffset((Point<Euclidean3D>)queryPoint));
			if(currentDistance<distance) {
				nearest = plane;
				distance = currentDistance;
			}
		}
		
		return nearest;
	}
	
	public Line trajectoryLine(Vector3D position, Vector3D velocity, double timeStep) {
		Vector3D p2 = velocity.scalarMultiply(timeStep).add(position);
		return new Line(position, p2, GeometryUtils.precision);
	}
}
