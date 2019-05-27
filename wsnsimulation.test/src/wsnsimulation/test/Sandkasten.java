package wsnsimulation.test;

import wsnsimulation.core.runtime.WSNSimulation;
import wsnsimulation.model.utils.ModelGenerator;

public class Sandkasten {
	public static void main(String args[]) {
		//ModelGenerator gen = new ModelGenerator();
		//gen.generateAndSaveModelFromFile("specifications/spec2.json", "models/spec2.xmi");
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec2.xmi");
		sim.setStochasticMotion(5.0, 1.5);
		sim.runUntil(20.0, true);
	}
}
