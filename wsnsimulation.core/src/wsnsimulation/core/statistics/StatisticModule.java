package wsnsimulation.core.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import wsnSimulationModel.WSNSimulationContainer;
import wsnsimulation.core.runtime.WSNSimulation;
import wsnsimulation.core.statistics.utils.DataPoint;

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
	
	public static boolean allHaveNext(List<Iterator<DataPoint<Integer>>> iterators) {
		for(Iterator<DataPoint<Integer>> iterator : iterators) {
			if(!iterator.hasNext()) {
				return false;
			}
		}
		return true;
	}
	
	public static void createFolderIfNotExist(String path) {
		File dir = new File(path);
		if(dir.isDirectory() && dir.exists())
			return;
		dir.mkdir();
	}
	
	public static void writeToFile(String data, String path) {
		try {
			FileWriter csvWriter = new FileWriter(path);
			csvWriter.append(data);
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
