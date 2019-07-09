package wsnsimulation.core.runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.eclipse.emf.ecore.resource.Resource;
import org.apache.commons.math3.geometry.euclidean.threed.*;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnSimulationModel.Obstacle;
import wsnSimulationModel.Rectangle;
import wsnSimulationModel.WSNNode;
import wsnSimulationModel.WSNSimulationContainer;
import wsnSimulationModel.WsnSimulationModelFactory;
import wsnsimulation.core.geometry.Boundary;
import wsnsimulation.core.geometry.GeometryUtils;
import wsnsimulation.core.geometry.VectorObject;
import wsnsimulation.core.geometry.VectorObstacle;
import wsnsimulation.core.geometry.VectorRectangle;
import wsnsimulation.core.geometry.VectorSimulationObject;
import wsnsimulation.core.statistics.ComplexWSNNode;
import wsnsimulation.core.statistics.StatisticModule;
import wsnsimulation.core.ui.WSNSimulationUI;
import wsnsimulation.model.utils.GeneratorUtils;

public class WSNSimulation {
	
	private WsnSimulationModelFactory factory = WsnSimulationModelFactory.eINSTANCE;
	
	private double time = 0;
	private Resource model;
	private WSNSimulationContainer container;
	private Boundary bound;
	private List<ExternalActor> externalActors = new LinkedList<>();
	private List<StatisticModule> statisticModules = new LinkedList<>();
	
	private boolean linearMotion = true;
	private double meanVelocity = 0;
	private double stdDevVelocity = 0;
	
	private WSNSimulationUI ui;
	private Random rnd = new Random();
	
	private Map<WSNNode, ComplexWSNNode> nodes = new HashMap<>();
	private Map<Obstacle, VectorObstacle> obstacles = new HashMap<>();
	
	public void setLinearMotion() {
		linearMotion = true;
	}
	
	public void setStochasticMotion(double meanVelocity, double stdDevVelocity) {
		this.meanVelocity = meanVelocity;
		this.stdDevVelocity = stdDevVelocity;
		linearMotion = false;
	}
	
	public void initRandomNodeVelocity(double meanVelocity, double stdDevVelocity) {
		for(VectorObject node : nodes.values()) {
			Vector3D velocity = Vector3D.PLUS_I.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			velocity = velocity.add(Vector3D.PLUS_J.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity));
			//velocity = velocity.add(Vector3D.PLUS_K.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity));
			node.setVelocity(velocity);
		}
		
	}
	
	public void initRandomObstacleVelocity(double meanVelocity, double stdDevVelocity) {
		for(VectorObject obstacle : obstacles.values()) {
			Vector3D velocity = Vector3D.PLUS_I.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity);
			velocity = velocity.add(Vector3D.PLUS_J.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity));
			//velocity = velocity.add(Vector3D.PLUS_K.scalarMultiply(meanVelocity + rnd.nextGaussian()*stdDevVelocity));
			obstacle.setVelocity(velocity);
		}
	}
	
	public double getTime() {
		return time;
	}
	
	public Resource getModel() {
		return model;
	}
	
	public WSNSimulationContainer getContainer() {
		return container;
	}
	
	public Map<WSNNode, ComplexWSNNode> getNodes() {
		return nodes;
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
		
		this.model = model;
		
		return model;
	}
	
	public void loadModel(Resource model) {
		this.model = model;
	}
	
	public void initialize() {
		container = (WSNSimulationContainer)model.getContents().get(0);
		bound = new Boundary(container.getWorldcontainer().getBounds());
		
		container.getNetworkcontainer().getWsnNodes().forEach(node -> {
			nodes.put(node, new ComplexWSNNode(node));
		});
		
		container.getWorldcontainer().getObstacles().forEach(o -> {
			VectorObstacle shape = null;
			if(o instanceof Rectangle) {
				shape = new VectorRectangle((Rectangle)o);
			}
			if(shape != null) {
				obstacles.put(o, shape);
			}
		});
		
		ui = new WSNSimulationUI(model, obstacles);
	}
	
	public void registerExternalActor(ExternalActor actor) {
		externalActors.add(actor);
		actor.setSimulation(this);
		actor.initialize();
	}
	
	public void registerStatisticModule(StatisticModule statistic) {
		statisticModules.add(statistic);
		statistic.setSimulation(this);
		statistic.initialize();
	}
	
	public void runOnce() {
		ui.display();
		
		simulateSignalReach();
		runExternalActors();
		updateRoutingTable();
		
		updateStatisticModules();
		time += container.getTimeStep();
		updateStatisticModules();
		
		System.out.println("Simulation complete..");
	}
	
	public void runUntil(double timeLimit, boolean inRealTime) {
		ui.display();
		
		while(timeLimit>=time) {
			simulateMotion();
			simulateSignalReach();
			simulateBatteryDrain();
			runExternalActors();
			updateRoutingTable();
			updateStatisticModules();
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
		
		System.out.println("Simulation complete..");
	}
	
	public void printStatistics() {
		for(StatisticModule statistic : statisticModules) {
			statistic.printStatistics();
		}
	}
	
	public void displayStatistics() {
		statisticModules.parallelStream().forEach(s -> s.displayGraph());
	}
	
	public void saveStatistics(String outputFolder) {
		statisticModules.parallelStream().forEach(s -> s.saveToCSV(outputFolder));
	}
	
	private void simulateMotion() {
		for(VectorObject node : nodes.values()) {
			if(linearMotion) {
				simulateLinearMotion(node);
			} else {
				simulateBrownianMotion(node);
			}
			
		}
		
		
		for(VectorObject obstacle : obstacles.values()) {
			if(linearMotion) {
				simulateLinearMotion(obstacle);
			} else {
				simulateBrownianMotion(obstacle);
			}	
		}
		
	}
	
	private void simulateSignalReach() {
		for(WSNNode node : container.getNetworkcontainer().getWsnNodes()) {		
			for(WSNNode node2 : container.getNetworkcontainer().getWsnNodes()) {
				if(node == node2)
					continue;	
				
				ComplexWSNNode object1 = nodes.get(node);
				ComplexWSNNode object2 = nodes.get(node2);
				
				boolean inReach = (container.isDeterministic()) ? 
						checkReachableDeterministic(object1, object2) : 
							checkReachableProbabilistic(object1, object2);
				boolean haveLOS = haveLineOfSight(object1, object2);
				double cost = calculateCost(object1, object2);
				
				Link link = node.getLinks().stream().filter(l -> l.getWsnNodes().contains(node) && l.getWsnNodes().contains(node2)).findAny().orElse(null);
				
				if(inReach && haveLOS) {
					if(link == null) {
						link = factory.createLink();
						container.getNetworkcontainer().getLinks().add(link);
						
						ComplexWSNNode.connect(link, object1, object2);
					}
					
					ComplexWSNNode.updateCost(link, object1, object2, cost);
					
					if(link.getLinkState() == LinkState.DELETED) {
						ComplexWSNNode.updateLinkState(link, object1, object2, LinkState.UNKNOWN);
					}
					
					ComplexWSNNode.updateLink(link, object1, object2);
								
				}else {
					if(link != null ) {
						ComplexWSNNode.updateCost(link, object1, object2, Double.MAX_VALUE);
						ComplexWSNNode.updateLinkState(link, object1, object2, LinkState.DELETED);
						ComplexWSNNode.updateLink(link, object1, object2);
					} else {
						ComplexWSNNode.disconnect(object1, object2);
					}
				}
				
				if(link == null) {
					ComplexWSNNode.disconnect(object1, object2);
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
	
	private void updateStatisticModules() {
		statisticModules.parallelStream().forEach(s -> s.update());
	}
	
	private void updateRoutingTable() {
		for(ComplexWSNNode node : nodes.values()) {
			node.resetRoutingTable();
		}
		
		for(ComplexWSNNode node : nodes.values()) {
			node.exploreNetwork();
		}
	}
	
	private void simulateLinearMotion(VectorObject object) {
		Vector3D position = object.getPosition().add(object.getVelocity().scalarMultiply(container.getTimeStep()));
		Vector3D oldPosition = object.getPosition();
		object.setPosition(position);
		if(!bound.isInBounds(object)) {
			object.setPosition(oldPosition);
			reflectAtBound(object);
		}
	}
	
	private void simulateBrownianMotion(VectorObject object) {
		Vector3D position = object.getPosition();
		Vector3D velocity = object.getVelocity();

		velocity = velocity.scalarMultiply(0);
		velocity = velocity.add(Vector3D.PLUS_I.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		velocity = velocity.add(Vector3D.PLUS_J.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		//velocity = velocity.add(Vector3D.PLUS_K.scalarMultiply(meanVelocity+rnd.nextGaussian()*stdDevVelocity));
		
		position = position.add(velocity.scalarMultiply(container.getTimeStep()));
		
		Vector3D oldPosition = object.getPosition();
		object.setPosition(position);
		
		Vector3D oldVelocity = object.getVelocity();
		object.setVelocity(velocity);
		
		if(!bound.isInBounds(object)) {
			object.setPosition(oldPosition);
			object.setVelocity(oldVelocity);
			reflectAtBound(object);
		}
		
	}
	
	private boolean haveLineOfSight(VectorSimulationObject object1, VectorSimulationObject object2) {
		Line los = new Line(object1.getPosition(), object2.getPosition(), GeometryUtils.precision);
		for(VectorObstacle obstacle : obstacles.values()) {
			if(obstacle.lineSegmentIntersectsShape(los, object1.getPosition(), object2.getPosition())) {
				return false;
			}
		}
		
		return true;
	}
	
	private double calculateCost(VectorSimulationObject object1, VectorSimulationObject object2) {
		double distance = calculateDistance(object1, object2);
		double loss = 20.0 * Math.log10(object1.getSimulationObjectAs(WSNNode.class).getTransmitterType().getFrequency()) + 
				20.0 * Math.log10(distance) + 
				20.0 * Math.log10(4.0*Math.PI / 299792458.0);
		return loss;
	}
	
	private double calculateDistance(VectorSimulationObject object1, VectorSimulationObject object2) {
		return object1.getPosition().subtract(object2.getPosition()).getNormInf();
	}
	
	private boolean checkReachableDeterministic(VectorSimulationObject n1, VectorSimulationObject n2) {
		double distance = calculateDistance(n1, n2);
		double reach = Math.min(n1.getSimulationObjectAs(WSNNode.class).getTransmitterType().getDeterministicRange(), 
				n2.getSimulationObjectAs(WSNNode.class).getTransmitterType().getDeterministicRange());
		return distance <= reach;
	}
	
	private boolean checkReachableProbabilistic(VectorSimulationObject n1, VectorSimulationObject n2) {
		//TODO
		return false;
	}

	private void reflectAtBound(VectorObject object) {
		Vector3D reflection = bound.calculateReflection(object, container.getTimeStep());
		object.setVelocity(reflection);
		object.moveBy(reflection.scalarMultiply(container.getTimeStep()));
	}
	
}
