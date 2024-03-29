package wsnsimulation.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import wsnSimulationModel.Link;
import wsnSimulationModel.NetworkContainer;
import wsnSimulationModel.Obstacle;
import wsnSimulationModel.RealVector;
import wsnSimulationModel.Rectangle;
import wsnSimulationModel.WSNNode;
import wsnSimulationModel.WSNSimulationContainer;
import wsnSimulationModel.WorldContainer;
import wsnsimulation.core.geometry.GeometryUtils;
import wsnsimulation.core.geometry.VectorObstacle;
import wsnsimulation.core.geometry.VectorRectangle;
import wsnsimulation.core.geometry.VectorShape;
import wsnSimulationModel.Battery;
import wsnSimulationModel.Bounds;

public class WSNSimulationUI {
	
	private UIContenAdapter adapter;
	
	private Resource model;
	private WorldContainer wc;
	private NetworkContainer nc;
	
	private Graph graph;
	
	private Map<WSNNode, Node> vertices = new HashMap<>();
	private Map<Link, Edge> edges = new HashMap<>();
	private Map<Set<Node>, Edge> node2edge = new HashMap<>();
	private Map<Obstacle, VectorObstacle> obstacles = new HashMap<>();
	private Map<VectorObstacle, Set<Node>> obstaclePoints = new HashMap<>();
	
	
	public WSNSimulationUI(Resource model) {
		this.model = model;
		WSNSimulationContainer sc = (WSNSimulationContainer)model.getContents().get(0);
		wc = sc.getWorldcontainer();
		nc = sc.getNetworkcontainer();
		adapter = new UIContenAdapter(model, this);
		init();
	}
	
	public WSNSimulationUI(Resource model, Map<Obstacle, VectorObstacle> obstacles) {
		this.model = model;
		WSNSimulationContainer sc = (WSNSimulationContainer)model.getContents().get(0);
		wc = sc.getWorldcontainer();
		nc = sc.getNetworkcontainer();
		adapter = new UIContenAdapter(model, this);
		this.obstacles = obstacles;
		init();
	}
	
	private void init() {
		graph = new MultiGraph("WSNNetwork");
		
		nc.getWsnNodes().forEach(wNode -> {
			Node gsNode = graph.addNode(wNode.getName());
			gsNode.addAttribute("ui.label", wNode.getName());
			gsNode.setAttribute("xyz", 
					wNode.getPose().getPosition().getX(),
					wNode.getPose().getPosition().getY(),
					wNode.getPose().getPosition().getZ());
			if(wNode.equals(nc.getGateway())) {
				gsNode.addAttribute("ui.style", "fill-color: rgb(155,25,155); text-size: 12; size: 24px; text-style: bold;");
			}else {
				gsNode.addAttribute("ui.style", "fill-color: rgb(25,195,15); text-size: 12; size: 16px; text-style: bold;");
			}
			vertices.put(wNode, gsNode);
		});
		
		obstacles.forEach((obstacle, shape) -> buildShape(obstacle, shape));
		
		addBoundaries();
	}
	
	private void buildShape(Obstacle obstacle, VectorObstacle shape) {
		List<Node> nodes = new LinkedList<>();
		shape.getPointsOnHull().forEach(p -> {
			Node gsNode = graph.addNode(obstacle.getName()+"@"+p.hashCode());
			gsNode.setAttribute("xyz", 
					p.getX(),
					p.getY(),
					p.getZ());
			nodes.add(gsNode);
		});
		obstaclePoints.put(shape, new LinkedHashSet<Node>(nodes));
		
		Node[] nArray = new Node[nodes.size()];
		nArray = nodes.toArray(nArray);
		
		for(int i=0; i<nodes.size()-1; i++) {
			Node n1 = nArray[i];
			Node n2 = nArray[i+1];
			Edge e = graph.addEdge(n1.getId()+"-"+n2.getId(), n1, n2);
			e.addAttribute("ui.style", "fill-color: rgb(155,155,155); text-size: 12; size: 4px; text-style: bold;");
		}
		
		Node n1 = nArray[nodes.size()-1];
		Node n2 = nArray[0];
		Edge e = graph.addEdge(n1.getId()+"-"+n2.getId(), n1, n2);
		e.addAttribute("ui.style", "fill-color: rgb(155,155,155); text-size: 12; size: 4px; text-style: bold;");
	}
	
	private void addBoundaries() {
		Bounds bounds = wc.getBounds();
		Node b1 = graph.addNode("bound1");
		b1.setAttribute("xyz", bounds.getMaxX(), bounds.getMaxY(), 0.0);
		Node b2 = graph.addNode("bound2");
		b2.setAttribute("xyz", bounds.getMinX(), bounds.getMaxY(), 0.0);
		Node b3 = graph.addNode("bound3");
		b3.setAttribute("xyz", bounds.getMinX(), bounds.getMinY(), 0.0);
		Node b4 = graph.addNode("bound4");
		b4.setAttribute("xyz", bounds.getMaxX(), bounds.getMinY(), 0.0);
		
		Edge e1 = graph.addEdge("b1-b2", b1, b2);
		e1.addAttribute("ui.style", "fill-color: rgb(100,100,100); text-size: 12; size: 3px; text-style: bold;");
		Edge e2 = graph.addEdge("b2-b3", b2, b3);
		e2.addAttribute("ui.style", "fill-color: rgb(100,100,100); text-size: 12; size: 3px; text-style: bold;");
		Edge e3 = graph.addEdge("b3-b4", b3, b4);
		e3.addAttribute("ui.style", "fill-color: rgb(100,100,100); text-size: 12; size: 3px; text-style: bold;");
		Edge e4 = graph.addEdge("b4-b1", b4, b1);
		e4.addAttribute("ui.style", "fill-color: rgb(100,100,100); text-size: 12; size: 3px; text-style: bold;");
		
	}
	
	public void display() {
		Viewer view = graph.display(false);
	}
	
	public void addEdge(Notification notification) {
		if(notification.getNotifier() instanceof NetworkContainer && notification.getNewValue() instanceof Link) {
			Link link = (Link) notification.getNewValue();
			
			if(!edges.containsKey(link)) {
				edges.put(link, null);
			}
		}
		
		if(notification.getNotifier() instanceof WSNNode && notification.getNewValue() instanceof Link) {
			Link link = (Link) notification.getNewValue();
			
			if(!edges.containsKey(link)) {
				edges.put(link, null);
			}
			
			if(link.getWsnNodes().size() != 2) {
				return;
			}
			
			Node n1 = vertices.get(link.getWsnNodes().get(0));
			Node n2 = vertices.get(link.getWsnNodes().get(1));
			Set<Node> nodeSet = new HashSet<>();
			nodeSet.add(n1);
			nodeSet.add(n2);
			
			Edge edge = edges.get(link);
			Edge other = node2edge.get(nodeSet);
			
			if(other == null) {
				other = graph.addEdge(n1.getId()+"<->"+n2.getId(), n1, n2);
				node2edge.put(nodeSet, other);
			}
			
			if(edge == null) {
				edge = other;
				edges.put(link, edge);
			}
			
			if(edge != other) {
				graph.removeEdge(edge);
				edge = other;
				edges.replace(link, edge);
			}
			 
			setEdgeColor(edge, link);
		}
		
	}
	
	public void removeEdge(Notification notification) {
		if(notification.getNotifier() instanceof Link) {
			Link link = (Link) notification.getNotifier();
			
			if(link.getWsnNodes().size() == 2) {
				Node n1 = vertices.get(link.getWsnNodes().get(0));
				Node n2 = vertices.get(link.getWsnNodes().get(1));
				Set<Node> nodeSet = new HashSet<>();
				nodeSet.add(n1);
				nodeSet.add(n2);
				
				Edge other = node2edge.get(nodeSet);
				if(graph.getEdgeSet().contains(other)) {
					graph.removeEdge(other);
				}
				
				node2edge.remove(nodeSet);
			}
			
			
			Edge edge = edges.get(link);
			
			if(graph.getEdgeSet().contains(edge)) {
				graph.removeEdge(edge);
			}
			
			edges.remove(link);
			
			if(node2edge.containsValue(edge)) {
				node2edge.values().remove(edge);
			}
			
		}
	}
	
	public void setAttributeValue(Notification notification) {
		if(notification.getNotifier() instanceof Link) {
			Link link = (Link) notification.getNotifier();
			Edge edge = edges.get(link);
			if(edge == null) {
				return;
			}
			setEdgeColor(edge, link);
		}
		else if(notification.getNotifier() instanceof RealVector) {
			RealVector position = (RealVector) notification.getNotifier();

			if(position.eContainer().eContainer() instanceof WSNNode && 
					position.eContainmentFeature().getName().equals("position")) {
				
				WSNNode wNode = (WSNNode)position.eContainer().eContainer();
				Node node = vertices.get(wNode);
				node.setAttribute("xyz", position.getX(), position.getY(), position.getZ());
			}
			
			if(position.eContainer().eContainer() instanceof Obstacle && 
					position.eContainmentFeature().getName().equals("position")) {
				
				Obstacle obstacle = (Obstacle)position.eContainer().eContainer();
				VectorObstacle vo = obstacles.get(obstacle);
				ArrayList<Node> points = new ArrayList<>(obstaclePoints.get(vo));
				ArrayList<Vector3D> vsPoints = new ArrayList<>(vo.getPointsOnHull());
				
				for(int i = 0; i<points.size(); i++) {
					Node n = points.get(i);
					Vector3D p = vsPoints.get(i);
					n.setAttribute("xyz", p.getX(), p.getY(), p.getZ());
				}
			}
		}else if(notification.getNotifier() instanceof Battery) {
			
		}
	}
	
	public void setEdgeColor(Edge edge, Link link) {
		switch(link.getLinkState()) {
			case ACTIVE : {
				edge.addAttribute("ui.style", "fill-color: rgb(55,155,55); text-size: 12; size: 4px; text-style: bold;");
				return;
			}
			case INACTIVE : {
				edge.addAttribute("ui.style", "fill-color: rgb(55,55,155); text-size: 12; size: 2px; text-style: bold;");
				return;
			}
			case UNKNOWN : {
				edge.addAttribute("ui.style", "fill-color: rgb(55,55,55); text-size: 12; size: 2px; text-style: bold;");
				return;
			}
			case DELETED : {
				edge.addAttribute("ui.style", "fill-color: rgb(155,55,55); text-size: 12; size: 2px; text-style: bold;");
				return;
			}
		}
	}

}

class UIContenAdapter extends EContentAdapter {
	
	private WSNSimulationUI ui;
	
	public UIContenAdapter(Resource model, WSNSimulationUI ui) {
		this.ui = ui;
		model.eAdapters().add(this);
	}
	
	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		
		switch(notification.getEventType()) {
			case Notification.ADD: {
				ui.addEdge(notification);
				break;
			}
			case Notification.REMOVE: {	
				break;
			}
			case Notification.REMOVING_ADAPTER: {
				ui.removeEdge(notification);
				break;
			}
			case Notification.RESOLVE: {
				break;
			}
			case Notification.SET: {
				ui.setAttributeValue(notification);
				break;
			}
			case Notification.UNSET: {
				break;
			}
			case Notification.MOVE: {
				break;
			}
			case Notification.ADD_MANY: {
				break;
			}
			case Notification.REMOVE_MANY: {
				break;
			}
			default: return;
	}
	}
}
