package a1;

import tage.input.action.AbstractInputAction;
import tage.GameObject;
import tage.*;
import net.java.games.input.Event;
import org.joml.*;

public class PitchUp extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Matrix4f oldRotation, rotAroundAvatarX, newRotation;
    private Vector4f oldX;

    public PitchUp(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {

        if (game.getMounted()) {
            // Get reference to the avatar
            av = game.getAvatar();
            av.pitch(1);
        } else {
            Camera cam = game.getCamera();
            cam.pitch(1);
        }
    }
}