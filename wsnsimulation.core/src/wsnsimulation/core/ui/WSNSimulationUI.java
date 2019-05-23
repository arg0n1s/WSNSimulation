package wsnsimulation.core.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import wsnSimulationModel.RealVector;
import wsnSimulationModel.WSNNode;
import wsnSimulationModel.WSNSimulationContainer;
import wsnSimulationModel.WorldContainer;
import wsnSimulationModel.Battery;

public class WSNSimulationUI {
	
	private UIContenAdapter adapter;
	
	private Resource model;
	private WorldContainer wc;
	private NetworkContainer nc;
	
	private Graph graph;
	
	private Map<WSNNode, Node> vertices = new HashMap<>();
	//private Map<WSNNode, Set<Edge>> edges = new HashMap<>();
	private Map<Link, Edge> edges = new HashMap<>();
	
	public WSNSimulationUI(Resource model) {
		this.model = model;
		WSNSimulationContainer sc = (WSNSimulationContainer)model.getContents().get(0);
		wc = sc.getWorldcontainer();
		nc = sc.getNetworkcontainer();
		adapter = new UIContenAdapter(model, this);
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
			vertices.put(wNode, gsNode);
		});
	}
	
	public void display() {
		Viewer view = graph.display(false);
	}
	
	public void addEdge(Notification notification) {
		if(notification.getNotifier() instanceof NetworkContainer && notification.getNewValue() instanceof Link) {
			Link link = (Link) notification.getNewValue();
			Node n1 = vertices.get(link.getWsnNodes().get(0));
			Node n2 = vertices.get(link.getWsnNodes().get(1));
			Edge edge = edges.get(link);
			
			edge = graph.addEdge(n1.getId()+"<->"+n2.getId(), n1, n2);
			edges.put(link, edge);
			
			edge.addAttribute("ui.label", link.getCost());
		}
	}
	
	public void removeEdge(Notification notification) {
		if(notification.getNotifier() instanceof Link) {
			Link link = (Link) notification.getNotifier();
			Edge edge = edges.get(link);
			graph.removeEdge(edge);	
			edges.remove(link);
		}
	}
	
	public void setAttributeValue(Notification notification) {
		if(notification.getNotifier() instanceof Link) {
			Link link = (Link) notification.getNotifier();
			Edge edge = edges.get(link);
			edge.setAttribute("ui.label", link.getCost());
		}
		else if(notification.getNotifier() instanceof RealVector) {
			RealVector position = (RealVector) notification.getNotifier();

			if(position.eContainer().eContainer() instanceof WSNNode && 
					position.eContainmentFeature().getName().equals("position")) {
				WSNNode wNode = (WSNNode)position.eContainer().eContainer();
				Node node = vertices.get(wNode);
				node.setAttribute("xyz", position.getX(), position.getY(), position.getZ());
			}
		}else if(notification.getNotifier() instanceof Battery) {
			
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
