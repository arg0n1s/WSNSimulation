package wsnsimulation.core.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

import wsnSimulationModel.Link;
import wsnSimulationModel.LinkState;
import wsnsimulation.core.statistics.utils.DataPoint;
import wsnsimulation.core.statistics.utils.DoubleDataSeries;
import wsnsimulation.core.ui.StatisticModuleUi;

public class TransmissionRange extends StatisticModule {
	
	protected Map<ComplexWSNNode, DoubleDataSeries> node2avgRange = new LinkedHashMap<>();
	protected Map<ComplexWSNNode, DoubleDataSeries> node2maxRange = new LinkedHashMap<>();
	protected Map<ComplexWSNNode, DoubleDataSeries> node2minRange = new LinkedHashMap<>();
	protected DoubleDataSeries avgRange = new DoubleDataSeries();
	protected DoubleDataSeries maxRange = new DoubleDataSeries();
	protected DoubleDataSeries minRange = new DoubleDataSeries();
	
	protected StatisticModuleUi ui = new StatisticModuleUi("TransmissionRange");
	
	@Override
	public void initialize() {
		simulation.getNodes().forEach((wNode, cNode) -> {
			node2avgRange.put(cNode, new DoubleDataSeries());
			ui.addDataSeries(wNode.getName()+"_avg", node2avgRange.get(cNode));
			node2minRange.put(cNode, new DoubleDataSeries());
			ui.addDataSeries(wNode.getName()+"_max", node2minRange.get(cNode));
			node2maxRange.put(cNode, new DoubleDataSeries());
			ui.addDataSeries(wNode.getName()+"_min", node2maxRange.get(cNode));
		});
		
		ui.addDataSeries("global_avg", avgRange);
		ui.addDataSeries("global_max", maxRange);
		ui.addDataSeries("global_min", minRange);
	}

	@Override
	public void update() {
		double globalAvg = 0;
		double globalMax = 0;
		double globalMin = 0;
		int validMeasurements = node2avgRange.size();
		
		for(ComplexWSNNode node : node2avgRange.keySet()) {
			DoubleDataSeries avgSeries = node2avgRange.get(node);
			DoubleDataSeries maxSeries = node2maxRange.get(node);
			DoubleDataSeries minSeries = node2minRange.get(node);
			
			
			double avg = 0;
			double max = 0;
			double min = 0;
			
			boolean valid = false;
			
			for(Link link : node.getLinkTable().values()) {
				if(link.getLinkState() != LinkState.ACTIVE) {
					continue;
				}
				avg += link.getCost();
				if(link.getCost() > max) {
					max = link.getCost();
				}
				if(link.getCost() < min) {
					min = link.getCost();
				}
				valid = true;
			}
			avg /= node.getLinkTable().size();
			
			DataPoint<? extends Number> dataPointAvg = avgSeries.addDataPoint(simulation.getTime(), avg, valid);
			DataPoint<? extends Number> dataPointMax = maxSeries.addDataPoint(simulation.getTime(), max, valid);
			DataPoint<? extends Number> dataPointMin = minSeries.addDataPoint(simulation.getTime(), min, valid);
			
			if(valid) {
				globalAvg += avgSeries.getArithmeticMean();
				if(maxSeries.getMax() != null && maxSeries.getMax().data > globalMax) {
					globalMax = maxSeries.getMax().data;
				}
				if(minSeries.getMin() != null && minSeries.getMin().data < globalMin) {
					globalMin = minSeries.getMin().data;
				}
			}else {
				validMeasurements--;
			}
			
			ui.addDataPoint(node.getWSNNode().getName()+"_avg", dataPointAvg);
			ui.addDataPoint(node.getWSNNode().getName()+"_max", dataPointMax);
			ui.addDataPoint(node.getWSNNode().getName()+"_min", dataPointMin);
		}
		
		boolean valid = validMeasurements > 0;
		
		if(valid) {
			globalAvg /= node2avgRange.size();
		}
		
		DataPoint<? extends Number> dataPointAvg = avgRange.addDataPoint(simulation.getTime(), globalAvg, valid);
		DataPoint<? extends Number> dataPointMax = maxRange.addDataPoint(simulation.getTime(), globalMax, valid);
		DataPoint<? extends Number> dataPointMin = minRange.addDataPoint(simulation.getTime(), globalMin, valid);
		
		ui.addDataPoint("global_avg", dataPointAvg);
		ui.addDataPoint("global_max", dataPointMax);
		ui.addDataPoint("global_min", dataPointMin);
		
		ui.refreshUi();
	}

	@Override
	public void displayGraph() {
		ui.initUi("Time [s]", "Free-space path loss [dB]");
		ui.refreshUi();
		ui.displayUi();
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
