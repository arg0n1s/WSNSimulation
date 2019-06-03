package wsnsimulation.core.runtime;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.apache.commons.math3.geometry.Point;
import org.apache.commons.math3.geometry.euclidean.threed.*;

import wsnSimulationModel.Bounds;
import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnSimulationModel.Obstacle;
import wsnSimulationModel.Pose;
import wsnSimulationModel.RealVector;
import wsnSimulationModel.WSNNode;
import wsnSimulationModel.WSNSimulationContainer;
import wsnSimulationModel.WsnSimulationModelFactory;
import wsnsimulation.core.ui.WSNSimulationUI;
import wsnsimulation.model.utils.GeneratorUtils;

public class WSNSimulation {
	
	private WsnSimulationModelFactory factory = WsnSimulationModelFactory.eINSTANCE;
	
	private double time = 0;
	private WSNSimulationContainer container;
	private Boundary bound;
	private List<ExternalActor> externalActors = new LinkedList<>();
	
	private boolean linearMotion = true;
	private double meanVelocity = 0;
	private double stdDevVelocity = 0;
	
	private WSNSimulationUI ui;
	private Random rnd = new Random();
	
	private Map<WSNNode, Map<WSNNode, Link>> adjacencyMap = new HashMap<>();
	//private List<Link> removedLinks = new LinkedList<>();
	
	public void setLinearMotion() {
		linearMotion = true;
	}
	
	public void setStochasticMotion(double meanVelocity, double stdDevVelocity) {
		this.meanVelocity = meanVelocity;
		this.stdDevVelocity = stdDevVelocity;
		linearMotion = false;
	}
	
	public double getTime() {
		return time;
	}
	
	public WSNSimulationContainer getContainer() {
		return container;
	}
	
	public Resource loadModel(String path) {
		Resource model;
		try {
			model = GeneratorUtils.loadResource(path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		container = (WSNSimulationContainer)model.getContents().get(0);
		ui = new WSNSimulationUI(model);
		bound = new Boundary(container.getWorldcontainer().getBounds());
		
		return model;
	}
	
	public void loadModel(Resource model) {
		container = (WSNSimulationContainer)model.getContents().get(0);
		ui = new WSNSimulationUI(model);
	}
	
	public void registerExternalActor(ExternalActor actor) {
		externalActors.add(actor);
		actor.setSimulation(this);
		actor.initialize();
	}
	
	public void runUntil(double timeLimit, boolean inRealTime) {
		ui.display();
		
		while(timeLimit>=time) {
			simulateMotion();
			simulateSignalReach();
			simulateBatteryDrain();
			runExternalActors();
			//cleanRemovedLinks();
			time += container.getTimeStep();
			if(inRealTime) {
				try {
					Thread.sleep((long) (container.getTimeStep()*1000.0));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private void simulateMotion() {
		for(WSNNode node : container.getNetworkcontainer().getWsnNodes()) {
			if(linearMotion) {
				simulateLinearMotion(node.getPose());
			} else {
				simulateBrownianMotion(node.getPose());
			}
			
		}
		
		/*
		for(Obstacle obstacle : container.getWorldcontainer().getObstacles()) {
			if(linearMotion) {
				simulateLinearMotion(obstacle.getPose());
			} else {
				simulateBrownianMotion(obstacle.getPose());
			}	
		}
		*/
	}
	
	private void simulateSignalReach() {
		for(WSNNode node : container.getNetworkcontainer().getWsnNodes()) {
			Map<WSNNode, Link> currentAdjecency = adjacencyMap.get(node);
			if(currentAdjecency == null) {
				currentAdjecency = new HashMap<>();
				adjacencyMap.put(node, currentAdjecency);
			}
			
			for(WSNNode node2 : container.getNetworkcontainer().getWsnNodes()) {
				if(node == node2)
					continue;
				
				Map<WSNNode, Link> currentAdjecency2 = adjacencyMap.get(node2);
				if(currentAdjecency2 == null) {
					currentAdjecency2 = new HashMap<>();
					adjacencyMap.put(node2, currentAdjecency2);
				}
				
				boolean inReach = (container.isDeterministic()) ? 
						checkReachableDeterministic(node, node2) : 
							checkReachableProbabilistic(node, node2);
				double cost = calculateDistance(node.getPose(), node2.getPose());
				
				if(inReach) {	
					Link link = currentAdjecency.get(node2);
					if(link == null) {
						link = factory.createLink();
						container.getNetworkcontainer().getLinks().add(link);
						
						link.setLinkState(LinkState.UNKNOWN);
						node.getLinks().add(link);
						node2.getLinks().add(link);
						node.getCanReach().add(node2);
						
						currentAdjecency.put(node2, link);
						currentAdjecency2.put(node, link);
					}
					
					if(link.getCost() != cost) {
						link.setCost(cost);
					}
				}else {
					Link link = currentAdjecency.get(node2);
					if(link != null) {
						link.setLinkState(LinkState.DELETED);
						
						currentAdjecency.remove(node2);
						currentAdjecency2.remove(node);
						//removedLinks.add(link);
					}
				}
			}
		}
	}
	
	private void simulateBatteryDrain() {
		for(WSNNode node : container.getNetworkcontainer().getWsnNodes()) {
			node.getBattery().setCharge(node.getBattery().getCharge()-container.getTimeStep()*10.0);
		}
	}
	
	private void runExternalActors() {
		for(ExternalActor actor : externalActors) {
			if(actor.isPeriodic()) {
				actor.actPeriodic();
			}else {
				actor.actOnModel();
			}
		}
	}
	
	/*
	private void cleanRemovedLinks() {
		for(Link link : removedLinks) {
			EcoreUtil.remove(link);
		}
		removedLinks = new LinkedList<>();
	}
	*/
	
	private void simulateLinearMotion(Pose pose) {
		RealVector position = pose.getPosition();
		RealVector velocity = pose.getVelocity();
		
		position.setX(position.getX() + velocity.getX()*container.getTimeStep());
		position.setY(position.getY() + velocity.getY()*container.getTimeStep());
		//position.setZ(position.getZ() + velocity.getZ()*container.getTimeStep());
	}
	
	private void simulateBrownianMotion(Pose pose) {
		RealVector position = pose.getPosition();
		RealVector velocity = pose.getVelocity();
		if(velocity.getX() == velocity.getY() && velocity.getX() == 0) {
			velocity.setX(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			velocity.setY(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			//velocity.setZ(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
		}

		double newX = position.getX() + velocity.getX()*container.getTimeStep();
		double newY = position.getY() + velocity.getY()*container.getTimeStep();
		//double newZ = position.getZ() + velocity.getZ()*container.getTimeStep();
		if(bound.isInBounds(newX, newY, 0.0)) {
			position.setX(newX);
			position.setY(newY);
			//position.setZ();
		}else {
			reflectAtBound(pose);
		}
		
	}
	
	private double calculateDistance(Pose p1, Pose p2) {
		RealVector pos1 = p1.getPosition();
		RealVector pos2 = p2.getPosition();
		
		double dx = pos1.getX()-pos2.getX();
		double dy = pos1.getY()-pos2.getY();
		double dz = pos1.getZ()-pos2.getZ();
		
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	private boolean checkReachableDeterministic(WSNNode n1, WSNNode n2) {
		double distance = calculateDistance(n1.getPose(), n2.getPose());
		double reach = Math.min(n1.getTransmitterType().getDeterministicRange(), 
				n2.getTransmitterType().getDeterministicRange());
		return distance <= reach;
	}
	
	private boolean checkReachableProbabilistic(WSNNode n1, WSNNode n2) {
		//TODO
		return false;
	}
	
	
	
	private void reflectAtBound(Pose pose) {
		Vector3D position = realVec2Vec3D(pose.getPosition());
		Vector3D velocity = realVec2Vec3D(pose.getVelocity());
		
		Vector3D reflection = bound.calculateReflection(position, velocity, container.getTimeStep());
		
		RealVector v = pose.getVelocity();
		vec3dToRealVec(reflection, v);
		
		Vector3D p2 = position.add(reflection.scalarMultiply(container.getTimeStep()));
		RealVector p = pose.getPosition();
		vec3dToRealVec(p2, p);
	}
	

	public static Vector3D realVec2Vec3D(RealVector vec) {
		return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
	}
	
	public static void vec3dToRealVec(Vector3D vec3d, RealVector vec) {
		vec.setX(vec3d.getX());
		vec.setY(vec3d.getY());
		vec.setZ(vec3d.getZ());
	}
}

class Boundary{
	public static final double precision = 0.000001;
	
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
		planes.put("p2", new Plane(corners.get("b1"), corners.get("b2"), corners.get("b5"), precision));
		//planes.put("p3", new Plane(corners.get("b5"), corners.get("b6"), corners.get("b7"), precision));
		planes.put("p4", new Plane(corners.get("b3"), corners.get("b4"), corners.get("b7"), precision));
		planes.put("p5", new Plane(corners.get("b1"), corners.get("b4"), corners.get("b5"), precision));
		planes.put("p6", new Plane(corners.get("b2"), corners.get("b3"), corners.get("b6"), precision));
		
		this.bounds = bounds;
	}
	
	public boolean isInBounds(double x, double y, double z) {
		if(x>bounds.getMaxX() || x<bounds.getMinX())
			return false;
		if(y>bounds.getMaxY() || y<bounds.getMinY())
			return false;
		if(z>bounds.getMaxY() || z<bounds.getMinY())
			return false;
		
		return true;
	}
	
	public Vector3D calculateReflection(Vector3D position, Vector3D velocity, double timeStep) {
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
		return new Line(position, p2, precision);
	}
	
}
