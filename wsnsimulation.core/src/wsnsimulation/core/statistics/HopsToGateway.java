package wsnsimulation.core.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import wsnsimulation.core.statistics.utils.DataPoint;
import wsnsimulation.core.statistics.utils.DoubleDataSeries;
import wsnsimulation.core.statistics.utils.IntegerDataSeries;
import wsnsimulation.core.ui.StatisticModuleUi;

public class HopsToGateway extends StatisticModule {
	
	protected ComplexWSNNode gateWay;
	protected Map<ComplexWSNNode, IntegerDataSeries> node2HopCount = new LinkedHashMap<>();
	protected DoubleDataSeries avgHops = new DoubleDataSeries();
	protected DoubleDataSeries maxHops = new DoubleDataSeries();
	protected DoubleDataSeries minHops = new DoubleDataSeries();
	
	protected StatisticModuleUi ui = new StatisticModuleUi("HopsToGateway");

	@Override
	public void initialize() {
		simulation.getNodes().forEach((wNode, cNode) -> {
			node2HopCount.put(cNode, new IntegerDataSeries());
			if(wNode.equals(container.getNetworkcontainer().getGateway())) {
				gateWay = cNode;
			}
			ui.addDataSeries(wNode.getName(), node2HopCount.get(cNode));
		});
		ui.addDataSeries("global", avgHops);
	}

	@Override
	public void update() {
		double avg = 0;
		double max = -Double.MAX_VALUE;
		double min = Double.MAX_VALUE;
		
		for(ComplexWSNNode node : node2HopCount.keySet()) {
			IntegerDataSeries series = node2HopCount.get(node);
			DataPoint<? extends Number> dataPoint = null;
			if(node.getRoutingTable().containsKey(gateWay)) {
				dataPoint = series.addDataPoint(simulation.getTime(), node.getHopTable().get(gateWay), true);
			} else {
				dataPoint = series.addDataPoint(simulation.getTime(), -1, false);
			}
			
			avg += series.getArithmeticMean();
			
			if(series.getMax() != null && series.getMax().data > max) {
				max = series.getMax().data;
			}
			
			if(series.getMin() != null && series.getMin().data < min) {
				min = series.getMin().data;
			}
			ui.addDataPoint(node.getWSNNode().getName(), dataPoint);
		}
		
		avg /= node2HopCount.size();
		
		DataPoint<? extends Number> dataPoint = avgHops.addDataPoint(simulation.getTime(), avg, true);
		ui.addDataPoint("global", dataPoint);
		maxHops.addDataPoint(simulation.getTime(), max, true);
		minHops.addDataPoint(simulation.getTime(), min, true);
		
		ui.refreshUi();
	}

	@Override
	public void displayGraph() {
		ui.initUi("Time [s]", "#Hops");
		ui.refreshUi();
		ui.displayUi();
	}

	@Override
	public void printStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append("### Print-out for statistic module: "+this.getClass().getSimpleName()+"\n");
		sb.append("\t avg hops to gateway: [" + avgHops.getArithmeticMean() + "]\n");
		sb.append("\t max hops to gateway: [" + maxHops.getArithmeticMean() + "]\n");
		sb.append("\t min hops to gateway: [" + minHops.getArithmeticMean() + "]\n");
		sb.append("\t <Nodes>\n");
		node2HopCount.forEach((node, series) -> {
			sb.append("\t\t Node: "+node.getWSNNode().getName()+(node.equals(gateWay)?" (Gateway)":"")+"\n");
			sb.append("\t\t\t avg hops to gateway: ["+series.getArithmeticMean()+"]\n");
			sb.append("\t\t\t max hops to gateway: "+series.getMax()+"\n");
			sb.append("\t\t\t min hops to gateway: "+series.getMin()+"\n");
		});
		sb.append("\t </Nodes>");
		System.out.println(sb.toString());
	}

	@Override
	public void saveToCSV(String outputFolder) {
		createFolderIfNotExist(outputFolder);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Time,Avg_Avg_Hops,Avg_Max_Hops,Avg_Min_Hops\n");
		
		Iterator<DataPoint<Double>> hops = avgHops.getDataIterator();
		Iterator<DataPoint<Double>> maxs = maxHops.getDataIterator();
		Iterator<DataPoint<Double>> mins = minHops.getDataIterator();
		
		while(hops.hasNext() && maxs.hasNext() && mins.hasNext()) {
			DataPoint<Double> hop = hops.next();
			DataPoint<Double> max = maxs.next();
			DataPoint<Double> min = mins.next();
			sb.append(hop.time+","+hop.data+",");
			sb.append(max.data+",");
			sb.append(min.data+"\n");
		}
		
		writeToFile(sb.toString(), outputFolder+"/Hops2Gateway_"+simulation.getContainer().getName()+"_global.csv");
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append("Time,");
		node2HopCount.keySet().forEach(node -> {
			sb2.append(",Hops@"+node.getWSNNode().getName());
		});
		sb2.append("\n");
		
		List<Iterator<DataPoint<Integer>>> hops2 = new LinkedList<>();
		node2HopCount.values().forEach(series -> hops2.add(series.getDataIterator()));
		
		while(allHaveNext(hops2)) {
			boolean timeSet = false;
			
			for(Iterator<DataPoint<Integer>> iterator : hops2) {
				DataPoint<Integer> hop = iterator.next();
				if(!timeSet) {
					sb2.append(hop.time);
					timeSet = true;
				}
				sb2.append(","+hop.data);
			}
			sb2.append("\n");
		}
		
		writeToFile(sb2.toString(), outputFolder+"/Hops2Gateway_"+simulation.getContainer().getName()+"_nodes.csv");
	}
	
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
