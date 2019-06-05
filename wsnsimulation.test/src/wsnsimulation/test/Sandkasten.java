package wsnsimulation.test;

import wsnalgorithm.ktc.core.KTCAlgorithm;
import wsnalgorithm.ktc.core.StochasticKTCAlgorithm;
import wsnsimulation.core.runtime.ExternalActor;
import wsnsimulation.core.runtime.WSNSimulation;
import wsnsimulation.model.utils.ModelGenerator;

public class Sandkasten {
	public static void main(String args[]) {
		//ModelGenerator gen = new ModelGenerator();
		//gen.generateAndSaveModelFromFile("specifications/spec2.json", "models/spec2.xmi");
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec2.xmi");
		//sim.initialize();
		ExternalActor ea = new KTCAlgorithm(1.0);
		//ExternalActor ea = new StochasticKTCAlgorithm(1.0);
		sim.registerExternalActor(ea);
		//ea.setPeriodic(true, 5.0);
		//sim.initRandomNodeVelocity(0.0, 10.0);
		//sim.initRandomObstacleVelocity(0.0, 20.0);
		//sim.setStochasticMotion(0.0, 35.0);
		
		sim.runUntil(1.0, true);
		System.out.println("Done..");
	}
}
