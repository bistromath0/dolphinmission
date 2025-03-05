package a1;

import tage.input.action.AbstractInputAction;
import tage.GameObject;
import tage.*;
import net.java.games.input.Event;
import org.joml.*;

public class RotRight extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Matrix4f oldRotation, rotAroundAvatarUp, newRotation;
    private Vector4f oldUp;
    private Matrix3f rotAroundCamUp;
    private Camera cam;

    public RotRight(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {

        if (game.getMounted()) {
            // Get reference to the avatar
            av = game.getAvatar();
            av.globalYaw(-1);
        } else {
            // rotates camera left about its up axis. U and N rotated, V stays the same
            cam = game.getCamera();
            cam.yaw(-1);

        }
    }
}