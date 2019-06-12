package wsnsimulation.core.statistics.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class DataSeries <T> {
	
	protected List<DataPoint<T>> data = new LinkedList<>();
	protected DataPoint<T> max = null;
	protected DataPoint<T> min = null;
	protected double arithmeticMean = 0;
	
	abstract public DataPoint<T> addDataPoint(double time, T data, boolean valid);
	
	protected void addDataPoint(DataPoint<T> dataPoint) {
		data.add(dataPoint);
	}

	public double getArithmeticMean() {
		return arithmeticMean;
	}
	
	public DataPoint<T> getMin() {
		return min;
	}

	public DataPoint<T> getMax() {
		return max;
	}
	
	public List<DataPoint<T>> getData() {
		return data;
	}
	
	public Iterator<DataPoint<T>> getDataIterator() {
		return data.iterator();
	}
}
