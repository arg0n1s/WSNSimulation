package wsnsimulation.core.statistics.utils;

public class IntegerDataPoint extends DataPoint<Integer> {

	public IntegerDataPoint(double time, Integer data, boolean valid) {
		super(time, data, valid);
	}

	@Override
	public int compareTo(DataPoint<Integer> o) {
		if(data > o.data) {
			return 1;
		} else if(data == o.data) {
			return 0;
		} else {
			return -1;
		}
	}

}
