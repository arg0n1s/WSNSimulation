package wsnsimulation.core.statistics.utils;

public class DoubleDataSeries extends DataSeries<Double> {
	
	protected double sum = 0;
	
	@Override
	public DataPoint<Double> addDataPoint(double time, Double data, boolean valid) {
		DataPoint<Double> dp = new DoubleDataPoint(time, data, valid);
		addDataPoint(dp);
		
		if(!valid)
			return dp;
		
		sum += data;
		arithmeticMean = sum / super.data.size();
		
		if(max != null) {
			if(dp.compareTo(max)>0) {
				max = dp;
			}
		} else {
			max = dp; 
		}
		
		if(min != null) {
			if(dp.compareTo(min)<0) {
				min = dp;
			}
		} else {
			min = dp;
		}
		
		return dp;
		
	}

}
