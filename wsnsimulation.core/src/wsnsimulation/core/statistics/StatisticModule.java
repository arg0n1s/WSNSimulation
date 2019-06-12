package wsnsimulation.core.statistics;

import wsnSimulationModel.WSNSimulationContainer;
import wsnsimulation.core.runtime.WSNSimulation;

public abstract class StatisticModule {

	protected WSNSimulation simulation;
	protected WSNSimulationContainer container;
	
	public void setSimulation(WSNSimulation simulation) {
		this.simulation = simulation;
		this.container = simulation.getContainer();
	}
	
	abstract public void initialize();
	
	abstract public void update();
	
	abstract public void displayGraph();
	
	abstract public void printStatistics();
	
	abstract public void saveToCSV(String outputFolder);
	
}
