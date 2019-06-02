package wsnsimulation.core.runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

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
		
		for(Obstacle obstacle : container.getWorldcontainer().getObstacles()) {
			if(linearMotion) {
				simulateLinearMotion(obstacle.getPose());
			} else {
				simulateBrownianMotion(obstacle.getPose());
			}	
		}
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
			actor.actOnModel();
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
		if(Math.abs(velocity.getX()) <= meanVelocity*0.3) {
			//velocity.setX(rnd.nextGaussian()*meanVelocity);
		} else {
			
		}
		velocity.setX(velocity.getX() + rnd.nextGaussian()*stdDevVelocity);
		
		if(Math.abs(velocity.getY()) <= meanVelocity*0.3) {
			//velocity.setY(rnd.nextGaussian()*meanVelocity);
		} else {
			
		}
		velocity.setY(velocity.getY() + rnd.nextGaussian()*stdDevVelocity);
		
		if(velocity.getZ() == 0.0) {
			//velocity.setZ(rnd.nextGaussian()*meanVelocity);
		} else {
			//velocity.setZ(velocity.getZ()+rnd.nextGaussian()*stdDevVelocity);
		}
		
		double newX = position.getX() + velocity.getX()*container.getTimeStep();
		double newY = position.getY() + velocity.getY()*container.getTimeStep();
		//double newZ = position.getZ() + velocity.getZ()*container.getTimeStep();
		if(isInBounds(newX, newY, 0.0)) {
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
	
	private boolean isInBounds(double x, double y, double z) {
		Bounds bounds = container.getWorldcontainer().getBounds();
		if(x>bounds.getMaxX() || x<bounds.getMinX())
			return false;
		if(y>bounds.getMaxY() || y<bounds.getMinY())
			return false;
		if(z>bounds.getMaxY() || z<bounds.getMinY())
			return false;
		
		return true;
	}
	
	private void reflectAtBound(Pose pose) {
		RealVector position = pose.getPosition();
		RealVector velocity = pose.getVelocity();
		
		velocity.setX(-velocity.getX());
		velocity.setY(-velocity.getY());
		//velocity.setZ(-velocity.getZ());
		
		position.setX(position.getX() + velocity.getX()*container.getTimeStep());
		position.setY(position.getY() + velocity.getY()*container.getTimeStep());
		//position.setZ(position.getZ() + velocity.getZ()*container.getTimeStep());
	}
}
