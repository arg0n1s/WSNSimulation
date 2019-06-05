package wsnsimulation.core.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnSimulationModel.WSNNode;
import wsnsimulation.core.geometry.VectorSimulationObject;

public class ComplexWSNNode extends VectorSimulationObject {

	protected Map<ComplexWSNNode, Link> adjacentNodes = new HashMap<>();
	protected Map<ComplexWSNNode, ComplexWSNNode> routingTable = new HashMap<>();
	protected Map<ComplexWSNNode, Integer> hopTable = new HashMap<>();
	
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
	
	public void exploreNetwork( ) {
		for(ComplexWSNNode other : adjacentNodes.keySet()) {
			if(adjacentNodes.get(other).getLinkState() == LinkState.ACTIVE) {
				other.receiveDiscoveryMessage(new DiscoveryMessage(this, discoveryMsgID));
				discoveryMsgID++;
			}
		}
	}
	
	public void receiveDiscoveryMessage(DiscoveryMessage msg) {
		if(!msg.isInPath(this)) {
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
				
				routingTable.get(msg.origin).returnDiscoveryMessage(new DiscoveryMessage(msg));
			}
		}
	}
	
	public void returnDiscoveryMessage(DiscoveryMessage msg) {
		if(msg.origin != this) {
			routingTable.get(msg.origin).returnDiscoveryMessage(msg);
		}
		
		Set<ComplexWSNNode> path = msg.getPath();
		ComplexWSNNode first = path.iterator().next();
		int count = 0;
		for(ComplexWSNNode hop : path) {
			count++;
			if(routingTable.containsKey(hop)) {
				if(hopTable.get(hop)> count) {
					routingTable.replace(hop, first);
					hopTable.replace(hop, count);
				}
			}else {
				routingTable.put(hop, first);
				hopTable.put(hop, count);
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
