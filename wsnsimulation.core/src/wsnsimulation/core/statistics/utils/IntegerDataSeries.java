package wsnsimulation.core.statistics.utils;

public class IntegerDataSeries extends DataSeries<Integer> {

	protected int sum = 0;
	
	@Override
	public DataPoint<Integer> addDataPoint(double time, Integer data, boolean valid) {
		DataPoint<Integer> dp = new IntegerDataPoint(time, data, valid);
		addDataPoint(dp);
		
		if(!valid)
			return dp;
		
		sum += data;
		arithmeticMean = ((double)sum) / super.data.size();
		
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
