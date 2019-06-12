package wsnsimulation.core.statistics.utils;

public abstract class DataPoint <D> implements Comparable<DataPoint <D>>{
	public final double time;
	public final D data;
	public final boolean valid;
	
	public DataPoint(final double time, final D data, final boolean valid) {
		this.time = time;
		this.data = data;
		this.valid = valid;
	}
	
	@Override
	public String toString() {
		return "["+data+"] @ "+time+"s";
	}
}
