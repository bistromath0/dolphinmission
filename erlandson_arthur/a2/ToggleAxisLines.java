package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;

// ... existing code ...

public class ToggleAxisLines extends AbstractInputAction {
    private MyGame game;

    public ToggleAxisLines(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        game.setRenderAxis(!game.getRenderAxis());
    }
}