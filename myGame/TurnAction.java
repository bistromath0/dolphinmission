package myGame;

import tage.input.action.AbstractInputAction;
import tage.GameObject;
import tage.*;
import net.java.games.input.Event;
import org.joml.*;

public class TurnAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Matrix4f oldRotation, rotAroundAvatarUp, newRotation;
    private Vector4f oldUp;

    public TurnAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // Get the input value (-1.0 to 1.0 from joystick axis)
        float keyValue = e.getValue();

        // Deadzone check
        if (keyValue > -.2 && keyValue < .2)
            return;

        // Get reference to the avatar
        av = game.getAvatar();

        // Get current rotation
        oldRotation = new Matrix4f(av.getWorldRotation());
        oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);

        float rotationAmount = -0.005f * keyValue; // Scale the rotation by input value

        rotAroundAvatarUp = new Matrix4f().rotation(rotationAmount,
                new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));

        newRotation = oldRotation;
        newRotation.mul(rotAroundAvatarUp);
        av.setLocalRotation(newRotation);
    }
}