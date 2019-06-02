package wsnsimulation.core.runtime;


public abstract class ExternalActor {
	
	protected WSNSimulation simulation;
	
	public void setSimulation(WSNSimulation simulation) {
		this.simulation = simulation;
	}
	
	abstract public void initialize();
	
	abstract public void actOnModel();
}
