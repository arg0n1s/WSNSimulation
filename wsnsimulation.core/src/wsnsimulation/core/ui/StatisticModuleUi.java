package wsnsimulation.core.ui;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import wsnsimulation.core.statistics.ComplexWSNNode;
import wsnsimulation.core.statistics.utils.DataPoint;
import wsnsimulation.core.statistics.utils.DataSeries;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import java.util.HashMap;
import java.util.Map;

import javax.swing.border.StrokeBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

public class StatisticModuleUi extends ApplicationFrame {

	private static final long serialVersionUID = 1L;
	protected XYSeriesCollection dataset = new XYSeriesCollection();
	protected Map<String, XYSeries> seriesMap = new HashMap<>();
	
	protected JFreeChart xylineChart;
	protected ChartFrame frame;

	public StatisticModuleUi(String title) {
		super(title);
	}
	
	public <T extends Number> void addDataSeries(String id, DataSeries<T> dataSeries) {
		XYSeries series = new XYSeries(id);
		dataset.addSeries(series);
		dataSeries.getData().forEach(data -> series.add(data.time, data.data));
		seriesMap.put(id, series);
	}
	
	public <T extends Number> void addDataPoint(String id, DataPoint<T> dataPoint) {
		seriesMap.get(id).add(dataPoint.time, dataPoint.data);
	}

	public void initUi(String xAxis, String yAxis) {
		xylineChart = ChartFactory.createXYLineChart(
		         super.getName() ,
		         xAxis ,
		         yAxis ,
		         dataset ,
		         PlotOrientation.VERTICAL ,
		         true , true , false);
		frame = new ChartFrame(super.getName(), xylineChart);
		setContentPane(frame.getContentPane());
		
		frame.setPreferredSize( new java.awt.Dimension( 800 , 600 ) );
		this.setPreferredSize(frame.getPreferredSize());
		
		this.setSize(frame.getPreferredSize());
		RefineryUtilities.centerFrameOnScreen( this );
	}
	
	public void refreshUi() {
		this.repaint();
		frame.repaint();
	}
	
	public void displayUi() {
		this.setVisible( true );
	}

}
