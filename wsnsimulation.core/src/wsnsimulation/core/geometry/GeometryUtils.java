package wsnsimulation.core.geometry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wsnSimulationModel.Quaternion;
import wsnSimulationModel.RealVector;

public class GeometryUtils {

	public static Vector3D realVec2Vec3D(RealVector vec) {
		return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
	}
	
	public static Rotation quaternion2Rotation(Quaternion quat) {
		return new Rotation(quat.getW(), quat.getX(), quat.getY(), quat.getZ(), false);
	}
	
	public static void vec3D2RealVec(Vector3D vec3d, RealVector vec) {
		vec.setX(vec3d.getX());
		vec.setY(vec3d.getY());
		vec.setZ(vec3d.getZ());
	}
	
	public static void rot2Quat(Rotation rot, Quaternion quat) {
		quat.setW(rot.getQ0());
		quat.setX(rot.getQ1());
		quat.setY(rot.getQ2());
		quat.setZ(rot.getQ3());
	}
	
}
