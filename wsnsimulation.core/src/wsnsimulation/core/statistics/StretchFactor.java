package wsnsimulation.core.statistics;

import java.util.Map;

import wsnSimulationModel.WSNNode;
import wsnsimulation.core.statistics.utils.DataPoint;
import wsnsimulation.core.statistics.utils.DijkstrasAlgorithm;
import wsnsimulation.core.statistics.utils.DoubleDataSeries;
import wsnsimulation.core.ui.StatisticModuleUi;
import wsnsimulation.core.statistics.utils.Path;

public class StretchFactor extends StatisticModule {
	
	protected DoubleDataSeries pathCostActive = new DoubleDataSeries();
	protected DoubleDataSeries pathCostAll = new DoubleDataSeries();
	protected DoubleDataSeries costStretchFactor = new DoubleDataSeries();
	
	protected DoubleDataSeries pathHopsActive = new DoubleDataSeries();
	protected DoubleDataSeries pathHopsAll = new DoubleDataSeries();
	protected DoubleDataSeries hopStretchFactor = new DoubleDataSeries();
	
	protected StatisticModuleUi uiCost = new StatisticModuleUi("Avg Cost per Path");
	protected StatisticModuleUi uiHops = new StatisticModuleUi("Avg Hops per Path");
	protected StatisticModuleUi uiStrech = new StatisticModuleUi("Stretch-Factor");

	@Override
	public void initialize() {
		uiCost.addDataSeries("pathCostActive", pathCostActive);
		uiCost.addDataSeries("pathCostAll", pathCostAll);
		
		uiHops.addDataSeries("pathHopsActive", pathHopsActive);
		uiHops.addDataSeries("pathHopsAll", pathHopsAll);
		
		uiStrech.addDataSeries("costStretchFactor", costStretchFactor);
		uiStrech.addDataSeries("hopStretchFactor", hopStretchFactor);
	}

	@Override
	public void update() {
		int nPathsActive = 0;
		double costActive = 0;
		double hopsActive = 0;
		
		int nPathsAll = 0;
		double costAll = 0;
		double hopsAll = 0;
		
		DijkstrasAlgorithm alg = new DijkstrasAlgorithm(simulation.getNodes().keySet(), DijkstrasAlgorithm::linkCostFunction);
		
		for(WSNNode node : simulation.getNodes().keySet()) {
			Map<WSNNode, Path> activePaths = alg.findAllPaths(node, DijkstrasAlgorithm::isLinkActive);
			Map<WSNNode, Path> allPaths = alg.findAllPaths(node, DijkstrasAlgorithm::isLinkMarked);
			nPathsActive += activePaths.size();
			nPathsAll += allPaths.size();
			
			for(Path path : activePaths.values()) {
				costActive += path.cost();
				hopsActive += path.length();
			}
			
			for(Path path : allPaths.values()) {
				costAll += path.cost();
				hopsAll += path.length();
			}
		}
		
		costActive /= nPathsActive;
		hopsActive /= nPathsActive;
		
		costAll /= nPathsAll;
		hopsAll /= nPathsAll;
		
		DataPoint<? extends Number> dp = pathCostActive.addDataPoint(simulation.getTime(), costActive, true);
		uiCost.addDataPoint("pathCostActive", dp);
		dp = pathHopsActive.addDataPoint(simulation.getTime(), hopsActive, true);
		uiHops.addDataPoint("pathHopsActive", dp);
		
		dp = pathHopsAll.addDataPoint(simulation.getTime(), hopsAll, true);
		uiHops.addDataPoint("pathHopsAll", dp);
		dp = pathCostAll.addDataPoint(simulation.getTime(), costAll, true);
		uiCost.addDataPoint("pathCostAll", dp);
		
		dp = costStretchFactor.addDataPoint(simulation.getTime(), costActive/costAll, true);
		uiStrech.addDataPoint("costStretchFactor", dp);
		dp = hopStretchFactor.addDataPoint(simulation.getTime(), hopsActive/hopsAll, true);
		uiStrech.addDataPoint("hopStretchFactor", dp);
	}

	@Override
	public void displayGraph() {
		uiCost.initUi("Time [s]", "Avg. cost per path [dB]");
		uiCost.refreshUi();
		uiCost.displayUi();
		
		uiHops.initUi("Time [s]", "Avg. #hops per path");
		uiHops.refreshUi();
		uiHops.displayUi();
		
		uiStrech.initUi("Time [s]", "Strech Factor active / all");
		uiStrech.refreshUi();
		uiStrech.displayUi();
	}

	@Override
	public void printStatistics() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToCSV(String outputFolder) {
		// TODO Auto-generated method stub

	}

}
