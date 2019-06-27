package wsnsimulation.core.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnsimulation.core.statistics.utils.DataPoint;
import wsnsimulation.core.statistics.utils.DoubleDataSeries;
import wsnsimulation.core.statistics.utils.IntegerDataSeries;
import wsnsimulation.core.ui.StatisticModuleUi;

public class LinkStatistics extends StatisticModule {
	
	protected IntegerDataSeries inactiveLinks = new IntegerDataSeries();
	protected IntegerDataSeries activeLinks = new IntegerDataSeries();
	protected IntegerDataSeries links = new IntegerDataSeries();
	
	protected DoubleDataSeries linkCost = new DoubleDataSeries();
	protected DoubleDataSeries activeVsAllLinks = new DoubleDataSeries();
	
	protected StatisticModuleUi linkStatusUi = new StatisticModuleUi("Link Status");
	protected StatisticModuleUi linkCostUi = new StatisticModuleUi("Link Cost");
	protected StatisticModuleUi linkActiveVsAllUi = new StatisticModuleUi("Active links vs. all links");

	@Override
	public void initialize() {
		linkStatusUi.addDataSeries("activeLinks", activeLinks);
		linkStatusUi.addDataSeries("inactiveLinks", inactiveLinks);
		linkStatusUi.addDataSeries("allLinks", links);
		linkCostUi.addDataSeries("linkCost", linkCost);
		linkActiveVsAllUi.addDataSeries("activeVsAllLinks", activeVsAllLinks);
	}

	@Override
	public void update() {
		List<Link> activeLinks = container.getNetworkcontainer().getLinks().stream()
				.filter(link -> link.getLinkState() == LinkState.ACTIVE)
				.collect(Collectors.toList());
		List<Link> inactiveLinks = container.getNetworkcontainer().getLinks().stream()
				.filter(link -> link.getLinkState() == LinkState.INACTIVE)
				.collect(Collectors.toList());
		
		DataPoint<? extends Number> dataInactive = this.inactiveLinks.addDataPoint(simulation.getTime(), inactiveLinks.size(), true);
		DataPoint<? extends Number> dataActive = this.activeLinks.addDataPoint(simulation.getTime(), activeLinks.size(), true);
		DataPoint<? extends Number> dataAll = this.links.addDataPoint(simulation.getTime(), activeLinks.size()+inactiveLinks.size(), true);
		DataPoint<? extends Number> dataCost = this.linkCost.addDataPoint(simulation.getTime(), 
				activeLinks.stream()
					.reduce(0.0, (sum, l) -> sum + l.getCost(), 
							(sum1, sum2) -> sum1 + sum2),
				true);
		DataPoint<? extends Number> dataVs = this.activeVsAllLinks.addDataPoint(simulation.getTime(), 
				activeLinks.size() / (double)(activeLinks.size()+inactiveLinks.size()), true);
		
		linkStatusUi.addDataPoint("activeLinks", dataActive);
		linkStatusUi.addDataPoint("inactiveLinks", dataInactive);
		linkStatusUi.addDataPoint("allLinks", dataAll);
		linkCostUi.addDataPoint("linkCost", dataCost);
		linkActiveVsAllUi.addDataPoint("activeVsAllLinks", dataVs);
		
		linkStatusUi.refreshUi();
		linkCostUi.refreshUi();
		linkActiveVsAllUi.refreshUi();
	}

	@Override
	public void displayGraph() {
		linkStatusUi.initUi("Time [s]", "#Links");
		linkStatusUi.refreshUi();
		linkStatusUi.displayUi();
		
		linkCostUi.initUi("Time [s]", "Cost");
		linkCostUi.refreshUi();
		linkCostUi.displayUi();
		
		linkActiveVsAllUi.initUi("Time [s]", "#active links / #all links");
		linkActiveVsAllUi.refreshUi();
		linkActiveVsAllUi.displayUi();
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
