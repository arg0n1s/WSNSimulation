package wsnsimulation.core.geometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
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
	
	@Override
	public boolean lineIntersectsShape(Line line) {
		Vector3D[] points = new Vector3D[4];
		points = this.points.toArray(points);
		Vector3D c5 = points[0].add(Vector3D.PLUS_K);
		Vector3D c6 = points[2].add(Vector3D.PLUS_K);
		
		List<Vector3D> pList1 = new LinkedList<>();
		pList1.add(points[0]); 
		pList1.add(points[1]); 
		pList1.add(c5);
		List<Vector3D> pList2 = new LinkedList<>();
		pList2.add(points[1]); 
		pList2.add(points[2]); 
		pList2.add(c6);
		List<Vector3D> pList3 = new LinkedList<>();
		pList3.add(points[2]); 
		pList3.add(points[3]); 
		pList3.add(c6);
		List<Vector3D> pList4 = new LinkedList<>();
		pList4.add(points[3]); 
		pList4.add(points[0]); 
		pList4.add(c5);
		
		Map<Plane, List<Vector3D>> planes = new HashMap<>();
		planes.put(new Plane(points[0],points[1], c5, GeometryUtils.precision), pList1);
		planes.put(new Plane(points[1],points[2], c6, GeometryUtils.precision), pList2);
		planes.put(new Plane(points[2],points[3], c6, GeometryUtils.precision), pList3);
		planes.put(new Plane(points[3],points[0], c5, GeometryUtils.precision), pList4);
		
		for(Plane plane : planes.keySet()) {
			Vector3D intersection = plane.intersection(line);
			if(intersection != null) {
				if(pointInLocalBounds(intersection, planes.get(plane))) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean pointInLocalBounds(Vector3D point, Collection<Vector3D> points) {
		double minX = findMinX(points);
		double minY = findMinY(points);
		double maxX = findMaxX(points);
		double maxY = findMaxY(points);
		
		if(point.getX()>maxX || point.getX()<minX)
			return false;
		if(point.getY()>maxY || point.getY()<minY)
			return false;
		
		return true;
	}
	
	public double findMaxX(Collection<Vector3D> points) {
		double val = points.iterator().next().getX();
		for(Vector3D point : points) {
			if(point.getX()>val) {
				val = point.getX();
			}
		}
		return val;
	}
	
	public double findMinX(Collection<Vector3D> points) {
		double val = points.iterator().next().getX();
		for(Vector3D point : points) {
			if(point.getX()<val) {
				val = point.getX();
			}
		}
		return val;
	}
	
	public double findMaxY(Collection<Vector3D> points) {
		double val = points.iterator().next().getY();
		for(Vector3D point : points) {
			if(point.getY()>val) {
				val = point.getY();
			}
		}
		return val;
	}
	
	public double findMinY(Collection<Vector3D> points) {
		double val = points.iterator().next().getY();
		for(Vector3D point : points) {
			if(point.getY()<val) {
				val = point.getY();
			}
		}
		return val;
	}

	@Override
	public boolean isInBody(Vector3D queryPoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnHull(Vector3D queryPoint) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean lineSegmentIntersectsShape(Line line, Vector3D start, Vector3D end) {
		if(!lineIntersectsShape(line)) {
			return false;
		}
		
		
		return false;
	}
}
