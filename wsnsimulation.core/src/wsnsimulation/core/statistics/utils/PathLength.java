package wsnsimulation.core.statistics.utils;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import wsnSimulationModel.Link;
import wsnSimulationModel.WSNNode;

public class PathLength {
	
	protected final List<WSNNode> nodes;
	protected final List<Link> links;
	protected Map<Entry<WSNNode, WSNNode>, Link> node2link = new HashMap<>();
	
	public PathLength(List<WSNNode> nodes, List<Link> links) {
		this.nodes = nodes;
		this.links = links;
		for(Link link : links) {
			WSNNode n1 = link.getWsnNodes().get(0);
			WSNNode n2 = link.getWsnNodes().get(1);
			node2link.put(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(n1, n2), link);
		}
	}
	
	public Path findPath(WSNNode src, WSNNode trg) {
		
		return null;
	}
	
	public List<Path> findAllPaths(WSNNode src) {
		
		return null;
	}
	
	private PriorityQueue<WSNNode> initQueue() {
		PriorityQueue<WSNNode> queue = new PriorityQueue<>(0, new MyComp(node2link) );
		
		return queue;
	}
	
}

class MyComp implements Comparator<WSNNode> {
	
	protected final Map<Entry<WSNNode, WSNNode>, Link> node2link;
	
	public MyComp(Map<Entry<WSNNode, WSNNode>, Link> node2link) {
		this.node2link = node2link;
	}

	@Override
	public int compare(WSNNode o1, WSNNode o2) {
		Link link = getLink(o1, o2);
		if(link == null) {
		}
		return 0;
	}
	
	public Link getLink(WSNNode o1, WSNNode o2) {
		Link link = node2link.get(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(o1, o2));
		if(link == null) {
			link = node2link.get(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(o2, o1));
		}
		return link;
	}

	
}
