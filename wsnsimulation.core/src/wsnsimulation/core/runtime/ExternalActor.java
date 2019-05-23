package wsnsimulation.core.runtime;

import wsnSimulationModel.WSNSimulationContainer;

public abstract class ExternalActor {
	
	protected WSNSimulationContainer container;
	
	public void setContainer(WSNSimulationContainer container) {
		this.container = container;
	}
	
	abstract public void initialize();
	
	abstract public void actOnModel();
}
