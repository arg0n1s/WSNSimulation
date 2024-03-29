package wsnsimulation.core.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnSimulationModel.WSNNode;
import wsnsimulation.core.geometry.VectorSimulationObject;
import wsnsimulation.core.statistics.utils.DijkstrasAlgorithm;
import wsnsimulation.core.statistics.utils.Path;

public class ComplexWSNNode extends VectorSimulationObject {

	protected Map<ComplexWSNNode, Link> adjacentNodes = new ConcurrentHashMap<>();
	protected Map<ComplexWSNNode, ComplexWSNNode> routingTable = new ConcurrentHashMap<>();
	protected Map<ComplexWSNNode, Integer> hopTable = new ConcurrentHashMap<>();
	
	private static int discoveryMsgID = 0;
	private Set<Integer> relayedDiscoveryMsgs = new HashSet<>();
	
	public ComplexWSNNode(WSNNode node) {
		super(node);
	}
	
	public WSNNode getWSNNode() {
		return super.getSimulationObjectAs(WSNNode.class);
	}
	
	public void addLink(Link link, ComplexWSNNode other) {
		link.setLinkState(LinkState.UNKNOWN);
		getWSNNode().getLinks().add(link);
		adjacentNodes.put(other, link);
	}
	
	public void updateLink(Link link, ComplexWSNNode other) {
		Link oldLink = adjacentNodes.get(other);
		if(oldLink!=link) {
			adjacentNodes.replace(other, link);
		}
	}
	
	public void updateCost(Link link, ComplexWSNNode other, double cost) {
		updateLink(link, other);
		if(link.getCost()!= cost) {
			link.setCost(cost);
		}
	}
	
	public void updateLinkState(Link link, ComplexWSNNode other, LinkState state) {
		updateLink(link, other);
		if(link.getLinkState() != state) {
			link.setLinkState(state);
		}
	}
	
	public void removeLink(ComplexWSNNode other) {
		adjacentNodes.remove(other);
	}
	
	public void resetRoutingTable() {
		routingTable = new HashMap<>();
		hopTable = new HashMap<>();
	}
	
	public void exploreNetwork(Map<WSNNode, ComplexWSNNode> network) {
		/*
		for(ComplexWSNNode other : adjacentNodes.keySet()) {
			if(adjacentNodes.get(other).getLinkState() == LinkState.ACTIVE) {
				other.receiveDiscoveryMessage(new DiscoveryMessage(this, discoveryMsgID));
				discoveryMsgID++;
			}
		}
		*/
		DijkstrasAlgorithm da = new DijkstrasAlgorithm(network.keySet(), DijkstrasAlgorithm::hopCostFunction);
		Map<WSNNode, Path> paths = da.findAllPaths(getWSNNode(), DijkstrasAlgorithm::isLinkActive);
		for(WSNNode node : paths.keySet()) {
			Path p = paths.get(node);
			hopTable.put(network.get(p.trg), p.length());
			routingTable.put(network.get(p.trg), network.get(p.getFirstHop()));
		}
	}
	
	public Map<ComplexWSNNode, ComplexWSNNode> getRoutingTable() {
		return routingTable;
	}
	
	public Map<ComplexWSNNode, Integer> getHopTable() {
		return hopTable;
	}
	
	public Map<ComplexWSNNode, Link> getLinkTable() {
		return adjacentNodes;
	}
	
	public synchronized void receiveDiscoveryMessage(DiscoveryMessage msg) {
		if(!msg.isInPath(this) && msg.origin != this) {
			msg.hopCount++;
			ComplexWSNNode last = msg.getLastHop();
			
			if(routingTable.containsKey(msg.origin)) {
				int hops = msg.hopCount;
				if(hopTable.get(msg.origin)> hops) {
					routingTable.replace(msg.origin, last);
					hopTable.replace(msg.origin, hops);
				}
			}else {
				routingTable.put(msg.origin, last);
				hopTable.put(msg.origin, msg.hopCount);
			}
			
			if(!routingTable.containsKey(last)) {
				hopTable.put(last, 1);
				routingTable.put(last, last);
			}
			
			msg.addHop(this);
			
			if(!relayedDiscoveryMsgs.contains(msg.id)) {
				relayedDiscoveryMsgs.add(msg.id);
				for(ComplexWSNNode other : adjacentNodes.keySet()) {
					if(adjacentNodes.get(other).getLinkState() == LinkState.ACTIVE) {
						if(other != last && other != msg.origin) {
							other.receiveDiscoveryMessage(new DiscoveryMessage(msg));
						}
					}
				}
				
			}
		}
		
	}
	
	public static void connect(Link link, ComplexWSNNode n1, ComplexWSNNode n2) {
		n1.addLink(link, n2);
		n2.addLink(link, n1);
	}
	
	public static void disconnect(ComplexWSNNode n1, ComplexWSNNode n2) {
		n1.removeLink(n2);
		n2.removeLink(n1);
	}
	
	public static void updateLink(Link link, ComplexWSNNode n1, ComplexWSNNode n2) {
		n1.updateLink(link, n2);
		n2.updateLink(link, n1);
	}
	
	public static void updateCost(Link link, ComplexWSNNode n1, ComplexWSNNode n2, double cost) {
		n1.updateCost(link, n2, cost);
		n2.updateCost(link, n1, cost);
	}
	
	public static void updateLinkState(Link link, ComplexWSNNode n1, ComplexWSNNode n2, LinkState state) {
		n1.updateLinkState(link, n2, state);
		n2.updateLinkState(link, n1, state);
	}
	
	@Override
	public String toString() {
		return getWSNNode().getName();
	}
}
