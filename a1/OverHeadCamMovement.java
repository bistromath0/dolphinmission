package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

public class OverHeadCamMovement extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition, n;
    private Vector4f fwdDirection;
    private Camera cam;

    public OverHeadCamMovement(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        cam = game.getOverHeadCamera();
        Component.Identifier id = e.getComponent().getIdentifier();

        if (id == Component.Identifier.Key.I) {
            oldPosition = cam.getLocation();
            fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
            fwdDirection.mul(-0.01f);
            newPosition = oldPosition.add(fwdDirection.x(),
                    fwdDirection.y(), fwdDirection.z());
            cam.setLocation(newPosition);

        } else if (id == Component.Identifier.Key.J) {
            oldPosition = cam.getLocation();
            fwdDirection = new Vector4f(1f, 0f, 0f, 1f);
            fwdDirection.mul(-0.01f);
            newPosition = oldPosition.add(fwdDirection.x(),
                    fwdDirection.y(), fwdDirection.z());
            cam.setLocation(newPosition);

        } else if (id == Component.Identifier.Key.K) {
            oldPosition = cam.getLocation();
            fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
            fwdDirection.mul(0.01f);
            newPosition = oldPosition.add(fwdDirection.x(),
                    fwdDirection.y(), fwdDirection.z());
            cam.setLocation(newPosition);

        } else if (id == Component.Identifier.Key.L) {
            oldPosition = cam.getLocation();
            fwdDirection = new Vector4f(1f, 0f, 0f, 1f);
            fwdDirection.mul(0.01f);
            newPosition = oldPosition.add(fwdDirection.x(),
                    fwdDirection.y(), fwdDirection.z());
            cam.setLocation(newPosition);

            // zoom in
        } else if (id == Component.Identifier.Key.U) {
            oldPosition = cam.getLocation();
            n = cam.getN();
            n.mul(0.01f);
            newPosition = oldPosition.add(n.x(),
                    n.y(), n.z());
            cam.setLocation(newPosition);
        }
        // zoom out
        else if (id == Component.Identifier.Key.O) {
            oldPosition = cam.getLocation();
            n = cam.getN();
            n.mul(-0.01f);
            newPosition = oldPosition.add(n.x(),
                    n.y(), n.z());
            cam.setLocation(newPosition);
        }

    }
}