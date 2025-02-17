package myGame;

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

        // Get reference to the avatar
        av = game.getAvatar();

        // Get current rotation
        oldRotation = new Matrix4f(av.getWorldRotation());
        oldX = new Vector4f(1f, 0f, 0f, 1f).mul(oldRotation);

        float rotationAmount = 0.005f;

        rotAroundAvatarX = new Matrix4f().rotation(rotationAmount,
                new Vector3f(oldX.x(), oldX.y(), oldX.z()));

        newRotation = oldRotation;
        newRotation.mul(rotAroundAvatarX);
        av.setLocalRotation(newRotation);
    }
}