package wsnalgorithm.ktc.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

import wsnalgorithm.ktc.rules.api.matches.PowerupIsMaximalButKMatch;
import wsnalgorithm.ktc.rules.api.matches.ShutdownIsMaximalMatch;

public class StochasticKTCAlgorithm extends KTCAlgorithm {
	
	protected Random rnd = new Random();

	public StochasticKTCAlgorithm(double k) {
		super(k);
		// TODO Auto-generated constructor stub
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
			this.powerUp(node);
			this.powerDown(node);
		}
		
		// delete marked links
		cleanDeleted();
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
				double minCost = (m.getL1().getCost() < m.getL2().getCost()) ? 
						m.getL1().getCost()  : m.getL2().getCost();
				double kPrime = k * minCost;
				double powerUp = 1.0 - probability(m.getL3().getCost(), minCost, kPrime);
				
				if(rnd.nextDouble() <= powerUp) {
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
				double minCost = (m.getL1().getCost() < m.getL2().getCost()) ? 
						m.getL1().getCost()  : m.getL2().getCost();
				double kPrime = k * minCost;
				double pShutdown = probability(m.getL3().getCost(), minCost, kPrime);
				
				if(rnd.nextDouble() <= pShutdown) {
					api.shutdownIsMaximal(node).apply(m);
				}
				
			});
			api.updateMatches();
			
		}
	}
	
	private double probability(double cost, double minCost, double kPrime) {
		return (cost>=minCost) ? 
				(1.0 - Math.pow(Math.E, -(cost/kPrime))) : 
					0.0;
	}
}
