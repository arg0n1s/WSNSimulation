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
import wsnalgorithm.ktc.rules.api.RulesHiPEApp;
import wsnalgorithm.ktc.rules.api.matches.MarkUnknownLinksMatch;
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
			app = new RulesHiPEApp();
			Resource r = app.loadModel(simulation.getModel().getURI());
			app.registerMetaModels();
			
			api = app.initAPI();
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("batch: "+(toc-tic)+"ms");
			simulation.loadModel(r);
			simulation.initialize();
			simulation.getContainer().getNetworkcontainer().getWsnNodes().forEach(node -> nodes.put(node.getName(), node));
		}

		@Override
		public void actOnModel() {
			// discover mark unknown links
			double tic = System.currentTimeMillis();
			markUnmarked();
			double toc = System.currentTimeMillis();
			System.err.println("Marking took: "+(toc-tic)+"ms");
			
			// repair broken connections
			tic = System.currentTimeMillis();
			for(String node : nodes.keySet()) {
				repair1(node);
				repair2(node);
				repair3(node);
			}
			toc = System.currentTimeMillis();
			System.err.println("Repairing took: "+(toc-tic)+"ms");
			
			// apply ktc
			tic = System.currentTimeMillis();
			for(String node : nodes.keySet()) {
				powerUp(node);
				powerDown(node);
			}
			toc = System.currentTimeMillis();
			System.err.println("KTc took: "+(toc-tic)+"ms");
			
			// delete marked links
			tic = System.currentTimeMillis();
			cleanDeleted();
			toc = System.currentTimeMillis();
			System.err.println("Cleanup took: "+(toc-tic)+"ms");
		}	

		
		protected void markUnmarked() {
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("Update markUnmarked1: "+(toc-tic)+"ms");
			/*
			System.out.println("Number of Links: "+api.findLinks().countMatches());
			int triangles = 0;
			for(String node : nodes.keySet()) {
				triangles += api.findTriangle(node).countMatches();
			}
			System.out.println("Number of Triangles: " + triangles);
			*/
			double tic4 = System.currentTimeMillis();
			Collection<MarkUnknownLinksMatch> matches = api.markUnknownLinks().findMatches();
			double toc4 = System.currentTimeMillis();
			System.out.println("Gathering all marks: "+(toc4-tic4)+"ms");
			double tic3 = System.currentTimeMillis();
			matches.forEach(match -> api.markUnknownLinks().apply(match));
			double toc3 = System.currentTimeMillis();
			System.out.println("Applying all marks: "+(toc3-tic3)+"ms");
			/*
			while(api.markUnknownLinks().hasMatches()) {
				api.markUnknownLinks().apply();
			}
			*/
			double tic2 = System.currentTimeMillis();
			api.updateMatches();
			double toc2 = System.currentTimeMillis();
			System.out.println("Update markUnmarked2: "+(toc2-tic2)+"ms");
		}
		
		protected void cleanDeleted() {
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("Update delete1: "+(toc-tic)+"ms");
			
			while(api.deleteLinks().hasMatches()) {
				api.deleteLinks().apply();	
			}
			
			double tic2 = System.currentTimeMillis();
			api.updateMatches();
			double toc2 = System.currentTimeMillis();
			System.out.println("Update delete2: "+(toc2-tic2)+"ms");
		}
		
		protected void repair1(String node) {
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("Update repair1: "+(toc-tic)+"ms");
			
			while(api.repair(node).hasMatches()) {
				double tic2 = System.currentTimeMillis();
				api.repair(node).apply();
				api.updateMatches();
				double toc2 = System.currentTimeMillis();
				System.out.println("Apply and update repair1: "+(toc2-tic2)+"ms");
			}
			
		}
		
		protected void repair2(String node) {
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("Update repair2: "+(toc-tic)+"ms");
			
			while(api.repair2(node).hasMatches()) {
				double tic2 = System.currentTimeMillis();
				api.repair2(node).apply();
				api.updateMatches();
				double toc2 = System.currentTimeMillis();
				System.out.println("Apply and update repair2: "+(toc2-tic2)+"ms");
			}
			
		}
		
		protected void repair3(String node) {
			double tic = System.currentTimeMillis();
			api.updateMatches();
			double toc = System.currentTimeMillis();
			System.out.println("Update repair3: "+(toc-tic)+"ms");
			
			while(api.repair3(node).hasMatches()) {
				double tic2 = System.currentTimeMillis();
				api.repair3(node).apply();
				api.updateMatches();
				double toc2 = System.currentTimeMillis();
				System.out.println("Apply and update repair3: "+(toc2-tic2)+"ms");
				
			}
			
		}
		
		protected void powerUp(String node) {
			double tic0 = System.currentTimeMillis();
			api.updateMatches();
			double toc0 = System.currentTimeMillis();
			System.out.println("Update powerUp: "+(toc0-tic0)+"ms");
			
			while(api.powerupNotMaximal(node).hasMatches()) {
				double tic00 = System.currentTimeMillis();
				api.powerupNotMaximal(node).apply();
				api.updateMatches();
				double toc00 = System.currentTimeMillis();
				System.out.println("Apply and update powerUp: "+(toc00-tic00)+"ms");
			}
			
			Collection<PowerupIsMaximalButKMatch> matches = new LinkedList<>();
			while(matches.size() != api.powerupIsMaximalButK(node).findMatches().size() && api.powerupIsMaximalButK(node).hasMatches()) {
				double tic = System.currentTimeMillis();
				matches = api.powerupIsMaximalButK(node).findMatches();
				double toc = System.currentTimeMillis();
				System.out.println("Matches for powerup took: "+(toc-tic)+"ms");
				
				double tic2 = System.currentTimeMillis();
				Optional<PowerupIsMaximalButKMatch> match = api.powerupIsMaximalButK(node).findAnyMatch();
				match.ifPresent(m -> {
					double currentK = (m.getL1().getCost() < m.getL2().getCost()) ? 
							m.getL1().getCost()  : m.getL2().getCost();
							
					currentK *= k;
					
					if(m.getL3().getCost() < currentK) {
						api.powerupIsMaximalButK(node).apply(m);
					}
					
				});
				double toc2 = System.currentTimeMillis();
				System.out.println("Applying powerup took: "+(toc2-tic2)+"ms");
				
				double tic3 = System.currentTimeMillis();
				api.updateMatches();
				double toc3 = System.currentTimeMillis();
				System.out.println("Refreshing after powerup took: "+(toc3-tic3)+"ms");
				
			}
		}
		
		protected void powerDown(String node) {
			api.updateMatches();
			
			Collection<ShutdownIsMaximalMatch> matches = new LinkedList<>();
			while(matches.size() != api.shutdownIsMaximal(node).findMatches().size() && api.shutdownIsMaximal(node).hasMatches()) {
				double tic = System.currentTimeMillis();
				matches = api.shutdownIsMaximal(node).findMatches();
				double toc = System.currentTimeMillis();
				System.out.println("Matches for powerdown took: "+(toc-tic)+"ms");
				
				double tic2 = System.currentTimeMillis();
				Optional<ShutdownIsMaximalMatch> match = api.shutdownIsMaximal(node).findAnyMatch();
				match.ifPresent(m -> {
					double currentK = (m.getL1().getCost() < m.getL2().getCost()) ? 
							m.getL1().getCost()  : m.getL2().getCost();
							
					currentK *= k;
					
					if(m.getL3().getCost() >= currentK) {
						api.shutdownIsMaximal(node).apply(m);
					}
					
				});
				double toc2 = System.currentTimeMillis();
				System.out.println("Applying powerdown took: "+(toc2-tic2)+"ms");
				
				double tic3 = System.currentTimeMillis();
				api.updateMatches();
				double toc3 = System.currentTimeMillis();
				System.out.println("Refreshing after powerdown took: "+(toc3-tic3)+"ms");
			}
		}
	
}
