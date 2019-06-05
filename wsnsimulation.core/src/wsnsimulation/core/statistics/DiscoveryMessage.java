package wsnsimulation.core.statistics;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class DiscoveryMessage {
	final public ComplexWSNNode origin;
	final public int id;
	private Set<ComplexWSNNode> path = new LinkedHashSet<>();
	public int hopCount = 0;
	
	public DiscoveryMessage(DiscoveryMessage other) {
		origin = other.origin;
		id = other.id;
		path.addAll(other.getPath());
		hopCount = other.hopCount;
	}
	
	public DiscoveryMessage(ComplexWSNNode origin, int id) {
		this.origin = origin;
		this.id = id;
	}
	
	public void addHop(ComplexWSNNode hop) {
		path.add(hop);
	}
	
	public boolean isInPath(ComplexWSNNode hop) {
		return path.contains(hop);
	}
	
	public ComplexWSNNode getLastHop() {
		LinkedList<ComplexWSNNode> list = new LinkedList<>(path);
		return (list.isEmpty())?origin:list.getLast();
	}
	
	public Set<ComplexWSNNode> getPath() {
		return path;
	}
}
