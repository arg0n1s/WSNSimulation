package wsnsimulation.model.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wsnSimulationModel.Battery;
import wsnSimulationModel.BatteryType;
import wsnSimulationModel.Bounds;
import wsnSimulationModel.Circle;
import wsnSimulationModel.NetworkContainer;
import wsnSimulationModel.Obstacle;
import wsnSimulationModel.Pose;
import wsnSimulationModel.Quaternion;
import wsnSimulationModel.RealVector;
import wsnSimulationModel.Rectangle;
import wsnSimulationModel.TransmitterType;
import wsnSimulationModel.WSNNode;
import wsnSimulationModel.WSNSimulationContainer;
import wsnSimulationModel.WorldContainer;
import wsnSimulationModel.WsnSimulationModelFactory;

public class ModelGenerator {
	
	public static final String JSON_SIM_PARAMETERS = "SimulationParameters";
	public static final String JSON_NAME_ATR = "name";
	public static final String JSON_TIMESTEP_ATR = "timeStep";
	public static final String JSON_DETERMINISTIC_ATR = "deterministic";
	public static final String JSON_BOUNDS_ATR = "bounds";
	
	public static final String JSON_TRANSMITTER_TYPES = "TransmitterTypes";
	public static final String JSON_DETERMINISTIC_RANGE_ATR = "deterministicRange";
	public static final String JSON_PROBABILISTIC_RANGE_ATR = "deterministicRange";
	
	public static final String JSON_BATTERY_TYPES = "BatteryTypes";
	public static final String JSON_CAPACITY_ATR = "capacity";
	
	public static final String JSON_NODES = "Nodes";
	public static final String JSON_NODES_NAMEPREFIX_ATR = "namePrefix";
	public static final String JSON_NODES_AMOUNT_ATR = "amount";
	public static final String JSON_NODES_GENERATED_ATR = "generated";
	public static final String JSON_NODES_SPECIFIED_ATR = "specified";
	public static final String JSON_TRANSMITTER_ATR = "transmitterType";
	public static final String JSON_BATTERY_ATR = "batteryType";
	public static final String JSON_POSITION_ATR = "position";
	public static final String JSON_ORIENTATION_ATR = "orientation";
	public static final String JSON_VELOCITY_ATR = "velocity";
	public static final String JSON_ANGULAR_VELOCITY_ATR = "angularVelocity";
	
	public static final String JSON_OBSTACLES = "Obstacles";
	public static final String JSON_OBSTACLE_TYPE_ATR = "type";
	
	public static final String JSON_OBSTACLE_TYPE_RECTANGLE = "Rectangle";
	public static final String JSON_OBSTACLE_RECTANGLE_WIDTH_ATR = "width";
	public static final String JSON_OBSTACLE_RECTANGLE_LENGTH_ATR = "length";
	
	public static final String JSON_OBSTACLE_TYPE_CRICLE = "Circle";
	public static final String JSON_OBSTACLE_CIRCLE_RADIUS_ATR = "radius";
	
	private JSONObject specification;
	private WsnSimulationModelFactory factory = WsnSimulationModelFactory.eINSTANCE;
	
	private Random rnd = new Random();
	
	private Map<String, BatteryType> batteryTypes;
	private Map<String, TransmitterType> transmitterTypes;
	private Bounds bounds;
	
	public Resource generateAndSaveModelFromFile(String specPath, String outputPath) {
		if(!loadSpecificationFile(specPath)) {
			throw new RuntimeException("Specification in "+specPath+" could not be loaded!");
		}
		Resource rs = generateModel(outputPath);
		saveModel(rs);
		return rs;
	}
	
	public boolean loadSpecificationFile(String path) {
		specification = GeneratorUtils.loadJSONFile(path);
		return specification != null;
	}
	
	public Resource generateModel(String filePath) {
		URI uri = URI.createFileURI(filePath);
		return generateModel(uri);
	}
	
	public Resource generateModel(URI uri) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi_resource", new XMIResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		
		Resource resource = rs.createResource(uri);
		
		WSNSimulationContainer simContainer = createSimulationContainer();
		NetworkContainer netContainer = factory.createNetworkContainer();
		WorldContainer worldContainer = createWorldContainer();
		simContainer.setNetworkcontainer(netContainer);
		simContainer.setWorldcontainer(worldContainer);
		
		batteryTypes = createBatteryTypes();
		transmitterTypes = createTransmitterTypes();
		
		simContainer.getTransmitterTypes().addAll(transmitterTypes.values());
		simContainer.getBatteryTypes().addAll(batteryTypes.values());
		
		netContainer.getWsnNodes().addAll(createNodes());
		worldContainer.getObstacles().addAll(createObstacles());
		
		resource.getContents().add(simContainer);
		
		return resource;
	}
	
	public void saveModel(Resource resource) {
		Map<Object, Object> saveOptions = ((XMIResource)resource).getDefaultSaveOptions();
		saveOptions.put(XMIResource.OPTION_ENCODING,"UTF-8");
		saveOptions.put(XMIResource.OPTION_USE_XMI_TYPE, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SAVE_TYPE_INFORMATION,Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION_IMPLEMENTATION, Boolean.TRUE);
		
		try {
			((XMIResource)resource).save(saveOptions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Model could not be saved to: "+resource.getURI().path());
			return;
		}
		System.out.println("Model saved to: "+resource.getURI().path());
	}
	
	private WSNSimulationContainer createSimulationContainer() {
		WSNSimulationContainer simContainer = factory.createWSNSimulationContainer();
		JSONObject jSimContainer =  getJObject(specification, JSON_SIM_PARAMETERS);
		simContainer.setName(this.<String>getAttributeByName(jSimContainer, JSON_NAME_ATR));
		simContainer.setTimeStep(this.<Double>getAttributeByName(jSimContainer, JSON_TIMESTEP_ATR));
		simContainer.setDeterministic(this.<Boolean>getAttributeByName(jSimContainer, JSON_DETERMINISTIC_ATR));
		return simContainer;
	}
	
	private WorldContainer createWorldContainer() {
		WorldContainer worldContainer = factory.createWorldContainer();
		JSONObject jSimContainer =  getJObject(specification, JSON_SIM_PARAMETERS);
		JSONObject jBounds =  getJObject(jSimContainer, JSON_BOUNDS_ATR);
		bounds = createBounds(jBounds);
		worldContainer.setBounds(bounds);
		return worldContainer;
	}
	
	private Bounds createBounds(JSONObject jBounds) {
		Bounds bounds = factory.createBounds();
		JSONObject x = getJObject(jBounds, "x");
		JSONObject y = getJObject(jBounds, "y");
		JSONObject z = getJObject(jBounds, "z");
		
		bounds.setMaxX(this.<Double>getAttributeByName(x, "max"));
		bounds.setMinX(this.<Double>getAttributeByName(x, "min"));
		bounds.setMaxY(this.<Double>getAttributeByName(y, "max"));
		bounds.setMinY(this.<Double>getAttributeByName(y, "min"));
		bounds.setMaxZ(this.<Double>getAttributeByName(z, "max"));
		bounds.setMinZ(this.<Double>getAttributeByName(z, "min"));
		
		return bounds;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, BatteryType> createBatteryTypes() {
		Map<String, BatteryType> bTypes = new HashMap<>();
		JSONArray jBTypes = getJArray(specification, JSON_BATTERY_TYPES);
		jBTypes.stream().filter(obj -> obj instanceof JSONObject).forEach(obj -> {
			JSONObject jObj = (JSONObject)obj;
			BatteryType bType = factory.createBatteryType();
			bType.setName(this.<String>getAttributeByName(jObj, JSON_NAME_ATR));
			bType.setCapacity(this.<Double>getAttributeByName(jObj, JSON_CAPACITY_ATR));
			bTypes.put(bType.getName(), bType);
		});
		
		return bTypes;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, TransmitterType> createTransmitterTypes() {
		Map<String, TransmitterType> trTypes = new HashMap<>();
		JSONArray jTrTypes = getJArray(specification, JSON_TRANSMITTER_TYPES);
		jTrTypes.stream().filter(obj -> obj instanceof JSONObject).forEach(obj -> {
			JSONObject jObj = (JSONObject)obj;
			TransmitterType trType = factory.createTransmitterType();
			trType.setName(this.<String>getAttributeByName(jObj, JSON_NAME_ATR));
			trType.setDeterministicRange(this.<Double>getAttributeByName(jObj, JSON_DETERMINISTIC_RANGE_ATR));
			trType.setProbabilisticRange(this.<Double>getAttributeByName(jObj, JSON_PROBABILISTIC_RANGE_ATR));
			trTypes.put(trType.getName(), trType);
		});
		
		return trTypes;
	}
	
	private List<WSNNode> createNodes() {
		List<WSNNode> nodes = new LinkedList<>();
		JSONObject jNodes = getJObject(specification, JSON_NODES);
		JSONArray jGenerated = getJArray(jNodes, JSON_NODES_GENERATED_ATR);
		JSONArray jSpecified = getJArray(jNodes, JSON_NODES_SPECIFIED_ATR);
		nodes.addAll(createGeneratedNodes(jGenerated));
		nodes.addAll(createSpecificNodes(jSpecified));
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	private List<WSNNode> createGeneratedNodes(JSONArray jGenerated) {
		List<WSNNode> nodes = new LinkedList<>();
		
		jGenerated.stream().filter(obj -> obj instanceof JSONObject).forEach(obj -> {
			JSONObject jObj = (JSONObject)obj;
			String prefix = this.<String>getAttributeByName(jObj, JSON_NODES_NAMEPREFIX_ATR);
			String transmitterType = this.<String>getAttributeByName(jObj, JSON_TRANSMITTER_ATR);
			String batteryType = this.<String>getAttributeByName(jObj, JSON_BATTERY_ATR);
			long amount = this.<Long>getAttributeByName(jObj, JSON_NODES_AMOUNT_ATR);
			
			for(int i = 0; i<amount; i++) {
				WSNNode node = factory.createWSNNode();
				node.setName(prefix+"_"+i);
				node.setTransmitterType(transmitterTypes.get(transmitterType));
				Battery bat = factory.createBattery();
				bat.setBatteryType(batteryTypes.get(batteryType));
				bat.setCharge(bat.getBatteryType().getCapacity());
				node.setBattery(bat);
				node.setPose(createRndPoseInBounds());
				
				nodes.add(node);
			}
			
		});
		
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	private List<WSNNode> createSpecificNodes(JSONArray jSpecified) {
		List<WSNNode> nodes = new LinkedList<>();
		
		jSpecified.stream().filter(obj -> obj instanceof JSONObject).forEach(obj -> {
			JSONObject jObj = (JSONObject)obj;
			WSNNode node = factory.createWSNNode();
			node.setName(this.<String>getAttributeByName(jObj, JSON_NAME_ATR));
			
			String transmitterType = this.<String>getAttributeByName(jObj, JSON_TRANSMITTER_ATR);
			String batteryType = this.<String>getAttributeByName(jObj, JSON_BATTERY_ATR);
			
			node.setTransmitterType(transmitterTypes.get(transmitterType));
			
			Battery bat = factory.createBattery();
			bat.setBatteryType(batteryTypes.get(batteryType));
			bat.setCharge(bat.getBatteryType().getCapacity());
			node.setBattery(bat);
			
			node.setPose(createPose(jObj));
			
			nodes.add(node);
		});
		
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	private List<Obstacle> createObstacles() {
		List<Obstacle> obstacles = new LinkedList<>();
		JSONArray jObstacles = getJArray(specification, JSON_OBSTACLES);
		jObstacles.stream().filter(obj -> obj instanceof JSONObject).forEach(obj -> {
			JSONObject jObj = (JSONObject)obj;
			String type = this.<String>getAttributeByName(jObj, JSON_OBSTACLE_TYPE_ATR);
			Obstacle o = null;
			if(JSON_OBSTACLE_TYPE_CRICLE.equals(type)) {
				Circle c = factory.createCircle();
				o = c;
				c.setRadius(this.<Double>getAttributeByName(jObj, JSON_OBSTACLE_CIRCLE_RADIUS_ATR));
			}else if(JSON_OBSTACLE_TYPE_RECTANGLE.equals(type)) {
				Rectangle r = factory.createRectangle();
				o = r;
				r.setLength(this.<Double>getAttributeByName(jObj, JSON_OBSTACLE_RECTANGLE_LENGTH_ATR));
				r.setWidth(this.<Double>getAttributeByName(jObj, JSON_OBSTACLE_RECTANGLE_WIDTH_ATR));
			}
			if(o != null) {
				o.setName(this.<String>getAttributeByName(jObj, JSON_NAME_ATR));
				o.setPose(createPose(jObj));
				obstacles.add(o);
			}
			
		});
		
		return obstacles;
	}
	
	private Pose createRndPoseInBounds() {
		Pose pose = factory.createPose();
		double xSpan = Math.abs(bounds.getMaxX() - bounds.getMinX());
		double ySpan = Math.abs(bounds.getMaxY() - bounds.getMinY());
		double zSpan = Math.abs(bounds.getMaxZ() - bounds.getMinZ());
		
		RealVector position = factory.createRealVector();
		pose.setPosition(position);
		Quaternion orientation = factory.createQuaternion();
		pose.setOrientation(orientation);
		RealVector velocity = factory.createRealVector();
		pose.setVelocity(velocity);
		RealVector aVelocity = factory.createRealVector();
		pose.setAngularVelocity(aVelocity);
		
		position.setX(bounds.getMinX()+rnd.nextDouble()*xSpan);
		position.setY(bounds.getMinY()+rnd.nextDouble()*ySpan);
		position.setZ(bounds.getMinZ()+rnd.nextDouble()*zSpan);
		
		return pose;
	}
	
	private Pose createPose(JSONObject jObject) {
		Pose pose = factory.createPose();
		
		JSONObject jPosition = getJObject(jObject, JSON_POSITION_ATR);
		RealVector position = factory.createRealVector();
		position.setX(this.<Double>getAttributeByName(jPosition, "x"));
		position.setY(this.<Double>getAttributeByName(jPosition, "y"));
		position.setZ(this.<Double>getAttributeByName(jPosition, "z"));
		pose.setPosition(position);
		
		JSONObject jOrientation = getJObject(jObject, JSON_ORIENTATION_ATR);
		Quaternion orientation = factory.createQuaternion();
		orientation.setX(this.<Double>getAttributeByName(jOrientation, "x"));
		orientation.setY(this.<Double>getAttributeByName(jOrientation, "y"));
		orientation.setZ(this.<Double>getAttributeByName(jOrientation, "z"));
		orientation.setW(this.<Double>getAttributeByName(jOrientation, "w"));
		pose.setOrientation(orientation);
		
		JSONObject jVelocity = getJObject(jObject, JSON_VELOCITY_ATR);
		RealVector velocity = factory.createRealVector();
		velocity.setX(this.<Double>getAttributeByName(jVelocity, "x"));
		velocity.setY(this.<Double>getAttributeByName(jVelocity, "y"));
		velocity.setZ(this.<Double>getAttributeByName(jVelocity, "z"));
		pose.setVelocity(velocity);
		
		JSONObject jAVelocity = getJObject(jObject, JSON_ANGULAR_VELOCITY_ATR);
		RealVector aVelocity = factory.createRealVector();
		aVelocity.setX(this.<Double>getAttributeByName(jAVelocity, "x"));
		aVelocity.setY(this.<Double>getAttributeByName(jAVelocity, "y"));
		aVelocity.setZ(this.<Double>getAttributeByName(jAVelocity, "z"));
		pose.setAngularVelocity(aVelocity);
		
		return pose;
	}
	
	private JSONObject getJObject(JSONObject root, String elementName) {
		JSONObject element = (JSONObject) root.get(elementName);
		if(element == null)
			throw new RuntimeException("Couldn't find Element: "+elementName+"inside node: "+root.toJSONString()+" in the specification!");
		return element;
	}
	
	private JSONArray getJArray(JSONObject root, String elementName) {
		JSONArray element = (JSONArray) root.get(elementName);
		if(element == null)
			throw new RuntimeException("Couldn't find Element: "+elementName+"inside node: "+root.toJSONString()+" in the specification!");
		return element;
	}

	
	@SuppressWarnings("unchecked")
	private <T> T getAttributeByName(JSONObject obj, String attrName) {
		T name = (T) obj.get(attrName);
		if(name == null)
			throw new RuntimeException(obj.toJSONString()+" "+attrName+" not specified!");
		return name;
	}
}
