package wsnsimulation.core.statistics.utils;

public class DoubleDataPoint extends DataPoint<Double> {

	public DoubleDataPoint(double time, Double data, boolean valid) {
		super(time, data, valid);
	}

	@Override
	public int compareTo(DataPoint<Double> o) {
		if(data > o.data) {
			return 1;
		} else if(data == o.data) {
			return 0;
		} else {
			return -1;
		}
	}

}
