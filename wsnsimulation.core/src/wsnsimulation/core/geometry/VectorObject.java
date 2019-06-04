package wsnsimulation.core.geometry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class VectorObject {
	protected Vector3D position;
	protected Vector3D velocity;
	protected Vector3D angularVelocity;
	protected Rotation orientation;
	
	public Vector3D getPosition() {
		return position;
	}
	
	public void setPosition(Vector3D position) {
		this.position = position;
	}
	
	public Vector3D getVelocity() {
		return velocity;
	}
	
	public void setVelocity(Vector3D velocity) {
		this.velocity = velocity;
	}
	
	public Vector3D getAngularVelocity() {
		return angularVelocity;
	}
	
	public void setAngularVelocity(Vector3D angularVelocity) {
		this.angularVelocity = angularVelocity;
	}
	
	public Rotation getOrientation() {
		return orientation;
	}
	
	public void setOrientation(Rotation orientation) {
		this.orientation = orientation;
	}
	
	public void moveBy(Vector3D translation) {
		position = position.add(translation);
	}
	
}
