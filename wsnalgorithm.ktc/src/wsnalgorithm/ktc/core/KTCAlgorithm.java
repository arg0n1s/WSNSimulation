package wsnalgorithm.ktc.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import wsnSimulationModel.WSNNode;
import wsnalgorithm.ktc.rules.api.RulesAPI;
import wsnalgorithm.ktc.rules.api.RulesApp;
import wsnalgorithm.ktc.rules.api.RulesDemoclesApp;
import wsnsimulation.core.runtime.ExternalActor;

public class KTCAlgorithm  extends ExternalActor{

		Map<String, WSNNode> nodes = new LinkedHashMap<String, WSNNode>();
		RulesApp app;
		RulesAPI api;

		@Override
		public void initialize() {	
			app = new RulesDemoclesApp();
			
			Resource r = app.loadModel(simulation.getContainer().eResource().getURI());
			app.registerMetaModels();
			
			api = app.initAPI();
			api.updateMatches();
			
			simulation.loadModel(r);
			simulation.getContainer().getNetworkcontainer().getWsnNodes().forEach(node -> nodes.put(node.getName(), node));
		}

		@Override
		public void actOnModel() {
			markUnmarked();
			cleanDeleted();
		}
		
		private void markUnmarked() {
			api.updateMatches();
			
			while(api.markUnknownLinks().hasMatches()) {
				api.markUnknownLinks().apply();
				api.updateMatches();
			}
		}
		
		private void cleanDeleted() {
			api.updateMatches();
			
			while(api.deleteLinks().hasMatches()) {
				api.deleteLinks().apply();
				api.updateMatches();
			}
		}
		
}
