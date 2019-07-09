package wsnsimulation.test;

import wsnalgorithm.ktc.core.KTCAlgorithm;
import wsnalgorithm.ktc.core.StochasticKTCAlgorithm;
import wsnsimulation.core.runtime.ExternalActor;
import wsnsimulation.core.runtime.WSNSimulation;
import wsnsimulation.core.statistics.HopsToGateway;
import wsnsimulation.core.statistics.LinkStatistics;
import wsnsimulation.core.statistics.StretchFactor;
import wsnsimulation.core.statistics.TransmissionRange;
import wsnsimulation.model.utils.ModelGenerator;

public class Sandkasten {
	public static void main(String args[]) {
		//ModelGenerator gen = new ModelGenerator();
		//gen.generateAndSaveModelFromFile("specifications/spec7.json", "models/spec7.xmi");
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec7.xmi");
		//sim.initialize();
		ExternalActor ea = new KTCAlgorithm(1.0);
		//ExternalActor ea = new StochasticKTCAlgorithm(1.0);
		sim.registerExternalActor(ea);
		sim.registerStatisticModule(new HopsToGateway());
		sim.registerStatisticModule(new TransmissionRange());
		sim.registerStatisticModule(new LinkStatistics());
		sim.registerStatisticModule(new StretchFactor());
		//ea.setPeriodic(true, 0.5);
		//sim.initRandomNodeVelocity(0.0, 5.0);
		//sim.initRandomObstacleVelocity(0.0, 20.0);
		//sim.setStochasticMotion(0.0, 35.0);
		
		sim.displayStatistics();
		//sim.runUntil(5.0, false);
		sim.runOnce();
		System.out.println("Done..");
		
		//sim.printStatistics();
		//sim.saveStatistics("output");
	}
}
