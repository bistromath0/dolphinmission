package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;

// ... existing code ...

public class ToggleMount extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition;
    private Vector4f fwdDirection;

    public ToggleMount(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // if mounted, render camera slightly to the right and front of dolphin
        if (game.getMounted()) {
            game.dismount();

        } else {
            game.remount();
        }
        // game.setMounted(!game.getMounted());
        // if dismounted, position camera in base position
    }

}