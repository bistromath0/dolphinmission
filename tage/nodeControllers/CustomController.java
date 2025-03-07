package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
 * CustomController is a nodeContrller that performs an up and down motion on a
 * GameObject.
 */
public class CustomController extends NodeController {
	private Engine engine;
	private float movementRate = .003f;
	private float cycleTime = 10.0f;
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	private Vector3f curPos;
	private Vector4f upVector;

	public CustomController() {
		super();
	}

	public CustomController(Engine e) {
		super();
		engine = e;
	}

	/**
	 * This is called automatically by the RenderSystem (via SceneGraph) once per
	 * frame
	 * during display(). It is for engine use and should not be called by the
	 * application.
	 */
	public void apply(GameObject go) {
		// moves the game object up and down
		Vector3f newLocation;
		float elapsedTime = super.getElapsedTime();
		totalTime += elapsedTime / 1000.0f;
		if (totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0f;
		}
		curPos = go.getWorldLocation();
		upVector = new Vector4f(0f, 1f, 0f, 1f);
		upVector.mul(movementRate).mul(direction);
		newLocation = curPos.add(upVector.x(), upVector.y(), upVector.z());
		go.setLocalLocation(newLocation);

	}
}