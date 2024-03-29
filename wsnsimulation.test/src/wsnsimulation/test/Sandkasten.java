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
		//runSkTC();
		runKTC();
//		ModelGenerator gen = new ModelGenerator();
//		gen.generateAndSaveModelFromFile("specifications/spec7_50.json", "models/spec7_50.xmi");
//		gen.generateAndSaveModelFromFile("specifications/spec7_100.json", "models/spec7_100.xmi");
//		gen.generateAndSaveModelFromFile("specifications/spec7_150.json", "models/spec7_150.xmi");
//		gen.generateAndSaveModelFromFile("specifications/spec7_200.json", "models/spec7_200.xmi");
//		gen.generateAndSaveModelFromFile("specifications/spec7_250.json", "models/spec7_250.xmi");
	}
	
	public static void runSkTC(int threads) {
		for(int i = 0; i<threads; i++) {
			Thread thread1 = new Thread(new Runnable() {

			    @Override
			    public void run() {
			    	runSkTC();   
			    }
			            
			});
			
			thread1.run();
		}
	}
	
	public static void runKTC(int threads) {
		for(int i = 0; i<threads; i++) {
			Thread thread1 = new Thread(new Runnable() {

			    @Override
			    public void run() {
			    	runKTC();   
			    }
			            
			});
			
			thread1.run();
		}
	}
	
	public static void runKTC() {
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec7_200.xmi");
		ExternalActor ea = new KTCAlgorithm(1.0);
		sim.registerExternalActor(ea);
		sim.registerStatisticModule(new HopsToGateway());
		//sim.registerStatisticModule(new TransmissionRange());
		sim.registerStatisticModule(new LinkStatistics());
		sim.registerStatisticModule(new StretchFactor());
		sim.displayStatistics();
		sim.runOnce();
	}
	
	public static void runSkTC() {
		WSNSimulation sim = new WSNSimulation();
		sim.loadModel("models/spec7.xmi");
		ExternalActor ea = new StochasticKTCAlgorithm(1.0);
		sim.registerExternalActor(ea);
		sim.registerStatisticModule(new HopsToGateway());
		//sim.registerStatisticModule(new TransmissionRange());
		sim.registerStatisticModule(new LinkStatistics());
		sim.registerStatisticModule(new StretchFactor());
		sim.displayStatistics();
		sim.runOnce();
	}
}
