package wsnsimulation.test;

import wsnalgorithm.ktc.core.KTCAlgorithm;
import wsnsimulation.core.runtime.ExternalActor;
import wsnsimulation.core.runtime.WSNSimulation;
import wsnsimulation.model.utils.ModelGenerator;

public class Sandkasten {
	public static void main(String args[]) {
//		ModelGenerator gen = new ModelGenerator();
//		gen.generateAndSaveModelFromFile("specifications/spec5.json", "models/spec5.xmi");
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec2.xmi");
		sim.setStochasticMotion(0.0, 50.0);
		
		//ExternalActor ea = new KTCAlgorithm(0.5);
		//sim.registerExternalActor(ea);
		
		sim.runUntil(20.0, true);
	}
}
