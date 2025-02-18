package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;

// ... existing code ...

public class FwdAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition, n;
    private Vector4f fwdDirection;
    private Camera cam;

    public FwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        if (game.getMounted()) {
            av = game.getAvatar();
            oldPosition = av.getWorldLocation();
            fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
            fwdDirection.mul(av.getWorldRotation());
            fwdDirection.mul(0.01f);
            newPosition = oldPosition.add(fwdDirection.x(),
                    fwdDirection.y(), fwdDirection.z());

            av.setLocalLocation(newPosition);

        } else {
            // move camera instead
            cam = game.getCamera();
            oldPosition = cam.getLocation();

            n = cam.getN();
            n.mul(0.01f);
            newPosition = oldPosition.add(n.x(),
                    n.y(), n.z());
            if (game.checkCameraDolphinDistance() < 2) {
                cam.setLocation(newPosition);
            }

        }
    }
}