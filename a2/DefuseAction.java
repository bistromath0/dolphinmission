package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.GameObject; // Add this import
import net.java.games.input.Event;
import org.joml.*;
import java.util.Arrays;

// ... existing code ...

public class DefuseAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition, n;
    private Vector4f fwdDirection;
    private Camera cam;

    public DefuseAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        int[] satelliteStates = game.getSatelliteStates();
        boolean[] satelliteDefusable = game.getSatelliteDefusable();
        for (int i = 0; i < satelliteStates.length; i++) {
            if (satelliteStates[i] == 1) {
                satelliteDefusable[i] = true;
            }
        }

    }
}