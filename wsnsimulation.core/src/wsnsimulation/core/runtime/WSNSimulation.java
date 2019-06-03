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
	
	public void setLinearMotion() {
		linearMotion = true;
	}
	
	public void setStochasticMotion(double meanVelocity, double stdDevVelocity) {
		this.meanVelocity = meanVelocity;
		this.stdDevVelocity = stdDevVelocity;
		linearMotion = false;
	}
	
	public void initRandomVelocity(double meanVelocity, double stdDevVelocity) {
		for(WSNNode node : container.getNetworkcontainer().getWsnNodes()) {
			node.getPose().getVelocity().setX(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			node.getPose().getVelocity().setY(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			//node.getPose().getVelocity().setZ(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
		}
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
				simulateLinearMotion(node);
			} else {
				simulateBrownianMotion(node);
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
				double cost = calculateCost(node, node2);
				
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
	
	private void simulateLinearMotion(WSNNode node) {
		Vector3D position = realVec2Vec3D(node.getPose().getPosition());
		Vector3D velocity = realVec2Vec3D(node.getPose().getVelocity());
		
		position = position.add(velocity.scalarMultiply(container.getTimeStep()));
		
		if(bound.isInBounds(position)) {
			vec3dToRealVec(position, node.getPose().getPosition());
		}else {
			reflectAtBound(node);
		}
	}
	
	private void simulateBrownianMotion(WSNNode node) {
		Vector3D position = realVec2Vec3D(node.getPose().getPosition());
		Vector3D velocity = realVec2Vec3D(node.getPose().getVelocity());

		velocity = velocity.scalarMultiply(0);
		velocity = velocity.add(Vector3D.PLUS_I.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		velocity = velocity.add(Vector3D.PLUS_J.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		//velocity = velocity.add(Vector3D.PLUS_K.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		
		position = position.add(velocity.scalarMultiply(container.getTimeStep()));
		
		if(bound.isInBounds(position)) {
			vec3dToRealVec(position, node.getPose().getPosition());
			vec3dToRealVec(velocity, node.getPose().getVelocity());
		}else {
			reflectAtBound(node);
		}
		
	}
	
	private double calculateCost(WSNNode node1, WSNNode node2) {
		double distance = calculateDistance(node1, node2);
		double loss = 20.0 * Math.log10(node1.getTransmitterType().getFrequency()) + 
				20.0 * Math.log10(distance) + 
				20.0 * Math.log10(4.0*Math.PI / 299792458.0);
		return loss;
	}
	
	private double calculateDistance(WSNNode node1, WSNNode node2) {
		Vector3D p1 = realVec2Vec3D(node1.getPose().getPosition());
		Vector3D p2 = realVec2Vec3D(node2.getPose().getPosition());
		return p1.subtract(p2).getNormInf();
	}
	
	private boolean checkReachableDeterministic(WSNNode n1, WSNNode n2) {
		double distance = calculateDistance(n1, n2);
		double reach = Math.min(n1.getTransmitterType().getDeterministicRange(), 
				n2.getTransmitterType().getDeterministicRange());
		return distance <= reach;
	}
	
	private boolean checkReachableProbabilistic(WSNNode n1, WSNNode n2) {
		//TODO
		return false;
	}

	private void reflectAtBound(WSNNode node) {
		Vector3D position = realVec2Vec3D(node.getPose().getPosition());
		Vector3D velocity = realVec2Vec3D(node.getPose().getVelocity());
		
		Vector3D reflection = bound.calculateReflection(position, velocity, container.getTimeStep());
		Vector3D p2 = position.add(reflection.scalarMultiply(container.getTimeStep()));
		
		vec3dToRealVec(reflection, node.getPose().getVelocity());
		vec3dToRealVec(p2, node.getPose().getPosition());
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
	
	public boolean isInBounds(Vector3D position) {
		if(position.getX()>bounds.getMaxX() || position.getX()<bounds.getMinX())
			return false;
		if(position.getY()>bounds.getMaxY() || position.getY()<bounds.getMinY())
			return false;
		if(position.getZ()>bounds.getMaxY() || position.getZ()<bounds.getMinY())
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
