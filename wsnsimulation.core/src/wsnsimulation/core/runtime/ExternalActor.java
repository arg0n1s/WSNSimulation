package wsnsimulation.core.runtime;


public abstract class ExternalActor {
	
	protected WSNSimulation simulation;
	protected boolean periodic = false;
	protected double period = 0.0;
	protected double lastActed; 
	
	public void setSimulation(WSNSimulation simulation) {
		this.simulation = simulation;
		lastActed = simulation.getTime();
	}
	
	abstract public void initialize();
	
	abstract public void actOnModel();
	
	public void actPeriodic() {
		if(simulation.getTime() >= lastActed+period) {
			lastActed = simulation.getTime();
			double tic = System.currentTimeMillis();
			actOnModel();
			double toc = System.currentTimeMillis();
			System.out.println("TC algorithm took: "+(toc-tic)+"ms");
		}
	}
	
	public void setPeriodic(boolean periodic, double period) {
		this.period = period;
		this.periodic = periodic;
		lastActed -= period;
	}
	
	public boolean isPeriodic() {
		return periodic;
	}
	
	public double getTimePeriod() {
		return period;
	}
}
