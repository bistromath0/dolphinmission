package a2;
import tage.input.action.AbstractInputAction;
import tage.GameObject;
import tage.*;
import net.java.games.input.Event;
import org.joml.*;

public class PitchDown extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Matrix4f oldRotation, rotAroundAvatarX, newRotation;
    private Vector4f oldX;

    public PitchDown(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {

        // Get reference to the avatar
        av = game.getAvatar();
        av.pitch(-1);

    }
}