package wsnsimulation.core.statistics.utils;

import java.util.List;

import wsnSimulationModel.Link;
import wsnSimulationModel.WSNNode;

public class Path {
	public final List<WSNNode> pathNodes;
	public final List<Link> pathLinks;
	public final WSNNode src;
	public final WSNNode trg;
	
	public Path(WSNNode src, WSNNode trg, List<WSNNode> pathNodes, List<Link> pathLinks) {
		this.src = src;
		this.trg = trg;
		this.pathNodes = pathNodes;
		this.pathLinks = pathLinks;
	}
	
	public int length() {
		return pathNodes.size();
	}
	
	public double cost() {
		return pathLinks.stream().reduce(0.0, (sum, l) -> sum + l.getCost(), (sum1, sum2) -> sum1 + sum2);
	}
	
}
