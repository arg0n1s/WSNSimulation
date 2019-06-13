package wsnalgorithm.ktc.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.resource.Resource;
import wsnSimulationModel.WSNNode;
import wsnalgorithm.ktc.rules.api.RulesAPI;
import wsnalgorithm.ktc.rules.api.RulesApp;
import wsnalgorithm.ktc.rules.api.RulesDemoclesApp;
import wsnalgorithm.ktc.rules.api.matches.PowerupIsMaximalButKMatch;
import wsnalgorithm.ktc.rules.api.matches.ShutdownIsMaximalMatch;
import wsnsimulation.core.runtime.ExternalActor;

public class KTCAlgorithm  extends ExternalActor{

		protected Map<String, WSNNode> nodes = new LinkedHashMap<String, WSNNode>();
		protected RulesApp app;
		protected RulesAPI api;
		protected double k;
		
		public KTCAlgorithm(double k) {
			this.k = k;
		}
		
		@Override
		public void initialize() {	
			app = new RulesDemoclesApp();
			
			Resource r = app.loadModel(simulation.getModel().getURI());
			app.registerMetaModels();
			
			api = app.initAPI();
			api.updateMatches();
			
			simulation.loadModel(r);
			simulation.initialize();
			simulation.getContainer().getNetworkcontainer().getWsnNodes().forEach(node -> nodes.put(node.getName(), node));
		}

		@Override
		public void actOnModel() {
			// discover mark unknown links
			markUnmarked();
			
			// repair broken connections
			for(String node : nodes.keySet()) {
				repair1(node);
				repair2(node);
				repair3(node);
			}
			
			// apply ktc
			for(String node : nodes.keySet()) {
				powerUp(node);
				powerDown(node);
			}
			
			// delete marked links
			cleanDeleted();
		}	

		
		protected void markUnmarked() {
			api.updateMatches();
			
			while(api.markUnknownLinks().hasMatches()) {
				api.markUnknownLinks().apply();
				api.updateMatches();
			}
		}
		
		protected void cleanDeleted() {
			api.updateMatches();
			
			while(api.deleteLinks().hasMatches()) {
				api.deleteLinks().apply();
				api.updateMatches();
			}
		}
		
		protected void repair1(String node) {
			api.updateMatches();
			
			while(api.repair(node).hasMatches()) {
				api.repair(node).apply();
				api.updateMatches();
				
			}
			
		}
		
		protected void repair2(String node) {
			api.updateMatches();
			
			while(api.repair2(node).hasMatches()) {
				api.repair2(node).apply();
				api.updateMatches();
				
			}
			
		}
		
		protected void repair3(String node) {
			api.updateMatches();
			
			while(api.repair3(node).hasMatches()) {
				api.repair3(node).apply();
				api.updateMatches();
				
			}
			
		}
		
		protected void powerUp(String node) {
			api.updateMatches();
			
			while(api.powerupNotMaximal(node).hasMatches()) {
				api.powerupNotMaximal(node).apply();
				api.updateMatches();
			}
			
			Collection<PowerupIsMaximalButKMatch> matches = new LinkedList<>();
			while(matches.size() != api.powerupIsMaximalButK(node).findMatches().size() && api.powerupIsMaximalButK(node).hasMatches()) {
				matches = api.powerupIsMaximalButK(node).findMatches();
				
				Optional<PowerupIsMaximalButKMatch> match = api.powerupIsMaximalButK(node).findAnyMatch();
				match.ifPresent(m -> {
					double currentK = (m.getL1().getCost() < m.getL2().getCost()) ? 
							m.getL1().getCost()  : m.getL2().getCost();
							
					currentK *= k;
					
					if(m.getL3().getCost() < currentK) {
						api.powerupIsMaximalButK(node).apply(m);
					}
					
				});
				api.updateMatches();
				
			}
		}
		
		protected void powerDown(String node) {
			api.updateMatches();
			
			Collection<ShutdownIsMaximalMatch> matches = new LinkedList<>();
			while(matches.size() != api.shutdownIsMaximal(node).findMatches().size() && api.shutdownIsMaximal(node).hasMatches()) {
				matches = api.shutdownIsMaximal(node).findMatches();
				
				Optional<ShutdownIsMaximalMatch> match = api.shutdownIsMaximal(node).findAnyMatch();
				match.ifPresent(m -> {
					double currentK = (m.getL1().getCost() < m.getL2().getCost()) ? 
							m.getL1().getCost()  : m.getL2().getCost();
							
					currentK *= k;
					
					if(m.getL3().getCost() >= currentK) {
						api.shutdownIsMaximal(node).apply(m);
					}
					
				});
				api.updateMatches();
				
			}
		}
		
}
