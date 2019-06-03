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
		sim.setStochasticMotion(0.0, 10.0);
		
		///ExternalActor ea = new KTCAlgorithm(0.5);
		ExternalActor ea = new StochasticKTCAlgorithm(1.0);
		sim.registerExternalActor(ea);
		ea.setPeriodic(true, 0.5);
		
		sim.runUntil(20.0, true);
	}
}
