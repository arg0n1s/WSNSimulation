package wsnsimulation.core.statistics.utils;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnSimulationModel.WSNNode;

public class DijkstrasAlgorithm {
	
	protected final Set<WSNNode> nodes;
	protected final Set<Link> links;
	protected Map<Entry<WSNNode, WSNNode>, Link> node2link = new HashMap<>();
	protected Map<WSNNode, Entry<WSNNode, Double>> distance2Src;
	protected Map<WSNNode, WSNNode> previous; 
	
	public DijkstrasAlgorithm(Set<WSNNode> nodes) {
		this.nodes = nodes;
		links = new HashSet<>();
		for(WSNNode node : nodes) {
			links.addAll(node.getLinks());
		}
		
		for(Link link : links) {
			WSNNode n1 = link.getWsnNodes().get(0);
			WSNNode n2 = link.getWsnNodes().get(1);
			node2link.put(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(n1, n2), link);
		}
	}
	
	public Path findPath(WSNNode src, WSNNode trg, Function<LinkState, Boolean> valid) {
		return findAllPaths(src, valid).get(trg);
	}
	
	public Map<WSNNode, Path> findAllPaths(WSNNode src, Function<LinkState, Boolean> valid) {
		PriorityQueue<Entry<WSNNode, Double>> queue = init(src);
		while(!queue.isEmpty()) {
			Entry<WSNNode, Double> current = queue.poll();
			WSNNode u = current.getKey();
			for(Link link : u.getLinks()) {
				if(!valid.apply(link.getLinkState())) {
					continue;
				}
				
				WSNNode v = null;
				if(link.getWsnNodes().get(0)!=u) {
					v = link.getWsnNodes().get(0);
				} else {
					v = link.getWsnNodes().get(1);
				}
				
				double alternative = distance2Src.get(u).getValue() + link.getCost();
				if(alternative < distance2Src.get(v).getValue()) {
					distance2Src.get(v).setValue(alternative);
					previous.put(v, u);
				}
			}
		}
		Map<WSNNode, Path> paths = new HashMap<>();
		for(WSNNode node : nodes) {
			if(node != src) {
				Path p = constructPath(src, node);
				if(p != null) {
					paths.put(node, p);
				}
			}
		}
		return paths;
	}
	
	private PriorityQueue<Entry<WSNNode, Double>> init(WSNNode src) {
		PriorityQueue<Entry<WSNNode, Double>> queue = new PriorityQueue<>(1, new MyComp() );
		queue.add(new AbstractMap.SimpleEntry<WSNNode, Double>(src, 0.0));
		for(WSNNode node : nodes) {
			if(node != src) {
				queue.add(new AbstractMap.SimpleEntry<WSNNode, Double>(node, Double.MAX_VALUE));
			}
		}
		distance2Src = new HashMap<>();
		previous = new HashMap<>();
		for(Entry<WSNNode, Double> entry : queue) {
			distance2Src.put(entry.getKey(), entry);
			previous.put(entry.getKey(), null);
		}
		return queue;
	}
	
	private Path constructPath(WSNNode src, WSNNode trg) {
		WSNNode prev = previous.get(trg);
		if(prev == null) {
			return null;
		}
		
		LinkedList<WSNNode> pN = new LinkedList<>();
		LinkedList<Link> pL = new LinkedList<>();
		pL.addFirst(getLink(prev, trg));
		pN.add(trg);
		
		while(prev != src) {
			WSNNode next = previous.get(prev);
			if(next == null) {
				return null;
			}
			pL.addFirst(getLink(next, prev));
			pN.addFirst(prev);
			prev = previous.get(prev);
			prev = next;
		}
		
		pN.addFirst(src);
		return new Path(src, trg, pN, pL);
	}
	
	private Link getLink(WSNNode o1, WSNNode o2) {
		Link link = node2link.get(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(o1, o2));
		if(link == null) {
			link = node2link.get(new AbstractMap.SimpleEntry<WSNNode, WSNNode>(o2, o1));
		}
		return link;
	}
	
}

class MyComp implements Comparator<Entry<WSNNode, Double>> {
	
	@Override
	public int compare(Entry<WSNNode, Double> arg0, Entry<WSNNode, Double> arg1) {
		if(arg0.getValue() > arg1.getValue()) {
			return 1;
		} else if(arg0.getValue() == arg1.getValue()) {
			return 0;
		} else {
			return -1;
		}
	}

	
}
