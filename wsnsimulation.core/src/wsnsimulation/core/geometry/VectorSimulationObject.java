package wsnsimulation.core.geometry;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wsnSimulationModel.SimulationObject;

public class VectorSimulationObject extends VectorObject {
	
	protected SimulationObject object;

	public VectorSimulationObject(SimulationObject object) {
		this.object = object;
		position = GeometryUtils.realVec2Vec3D(object.getPose().getPosition());
		velocity = GeometryUtils.realVec2Vec3D(object.getPose().getVelocity());
		angularVelocity = GeometryUtils.realVec2Vec3D(object.getPose().getAngularVelocity());
		orientation = GeometryUtils.quaternion2Rotation(object.getPose().getOrientation());
	}
	
	public <T extends SimulationObject> T getSimulationObjectAs(Class<T> type) {
		return type.cast(object);
	}
	
	@Override
	public void setPosition(Vector3D position) {
		super.setPosition(position);
		eSetPosition();
	}
	
	@Override
	public void setVelocity(Vector3D velocity) {
		super.setVelocity(velocity);
		eSetVelocity();
	}
	
	@Override
	public void moveBy(Vector3D translation) {
		super.moveBy(translation);
		eSetPosition();
	}
	
	protected void eSetPosition() {
		GeometryUtils.vec3D2RealVec(position, object.getPose().getPosition());
	}
	
	protected void eSetVelocity() {
		GeometryUtils.vec3D2RealVec(velocity, object.getPose().getVelocity());
	}
}
