package tage;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import java.lang.Math;
import tage.input.*;
import tage.input.action.*;

//must be possible to 1) orbit the camera without altering the avatar’s heading, (2) adjust the
//camera elevation angle, (3) zoom the camera in and out on the avatar, and (4) move and turn the
//dolphin while maintaining the camera’s relative position and orientation relative to the dolphin.
public class CameraOrbit3D {
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target

    public CameraOrbit3D(Camera cam, GameObject av, Engine e) {
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 2.0f; // distance from camera to avatar
        setupInputs();
        updateCameraPosition();
    }

    private void setupInputs() {
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        CameraElevationAction elevAction = new CameraElevationAction();
        CameraZoomAction zoomAction = new CameraZoomAction();
        InputManager im = engine.getInputManager();
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.X, azmAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.Y, elevAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.RZ, zoomAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        System.out.println("input set up");
    }

    // Compute the camera’s azimuth, elevation, and distance, relative to
    // the target in spherical coordinates, then convert to world Cartesian
    // coordinates and set the camera position from that.
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math
                .toDegrees((double) avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    private class OrbitAzimuthAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float rotAmount;
            float keyValue = event.getValue();
            if (keyValue > -.2 && keyValue < .2) {
                return;
            }
            if (event.getValue() < -0.2) {
                rotAmount = -0.2f;
            } else {
                if (event.getValue() > 0.2) {
                    rotAmount = 0.2f;
                } else {
                    rotAmount = 0.0f;
                }
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class CameraElevationAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float rotAmount;
            float keyValue = event.getValue();
            if (keyValue > -.2 && keyValue < .2) {
                return;
            }
            if (event.getValue() < -0.2) {
                rotAmount = -0.2f;
            } else {
                if (event.getValue() > 0.2) {
                    rotAmount = 0.2f;
                } else {
                    rotAmount = 0.0f;
                }
            }
            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    private class CameraZoomAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float zoomAmount;
            float keyValue = event.getValue();
            if (keyValue > -.2 && keyValue < .2) {
                return;
            }
            if (event.getValue() < -0.2) {
                zoomAmount = -0.02f;
            } else {
                if (event.getValue() > 0.2) {
                    zoomAmount = 0.02f;
                } else {
                    zoomAmount = 0.0f;
                }
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }
}
