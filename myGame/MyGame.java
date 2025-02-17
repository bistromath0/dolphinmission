package myGame;

import tage.*;
import tage.shapes.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private boolean paused = false;
	private int counter = 0;
	private double lastFrameTime, currFrameTime, elapsTime;

	private boolean mounted = true;

	private GameObject avatar, cube, torus, sphere, x, y, z;
	private ObjShape dolS, cubeS, torusS, sphereS, linxS, linyS, linzS;
	private TextureImage doltx, red, yellow, green;
	private Light light1;
	private Camera cam;

	private Vector3f loc, fwd, up, right, newLocation;
	private Matrix4f initialRotation;

	public MyGame() {
		super();
	}

	public static void main(String[] args) {
		MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		dolS = new ImportedModel("dolphinHighPoly.obj");
		cubeS = new Cube();
		torusS = new Torus();
		sphereS = new Sphere();
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		red = new TextureImage("red.png");
		green = new TextureImage("green.png");
		yellow = new TextureImage("yellow.png");

	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f, 0f, 1f);
		avatar.setLocalTranslation(initialTranslation);
		/*
		 * initialRotation = (new Matrix4f()).rotationY(
		 * (float) java.lang.Math.toRadians(135.0f));
		 * avatar.setLocalRotation(initialRotation);
		 */
		cube = new GameObject(GameObject.root(), cubeS, green);
		initialTranslation = (new Matrix4f()).translation(3, 0, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);

		torus = new GameObject(GameObject.root(), torusS, green);
		initialTranslation = (new Matrix4f()).translation(0, 1, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);

		sphere = new GameObject(GameObject.root(), sphereS, green);
		initialTranslation = (new Matrix4f()).translation(-3, 0, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		sphere.setLocalTranslation(initialTranslation);
		sphere.setLocalScale(initialScale);

		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0, 0, 5));

		positionCameraBehindAvatar();
		im = engine.getInputManager();

		FwdAction fwdAction = new FwdAction(this);
		TurnAction turnAction = new TurnAction(this);
		BackAction backAction = new BackAction(this);
		RotLeft rotLeft = new RotLeft(this);
		RotRight rotRight = new RotRight(this);

		PitchUp pitchUp = new PitchUp(this);
		PitchDown pitchDown = new PitchDown(this);

		ToggleMount toggleMount = new ToggleMount(this);
		YAxisAction yAxis = new YAxisAction(this);

		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._1, fwdAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Axis.X, turnAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.W, fwdAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.S, backAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.A, rotLeft,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.D, rotRight,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.UP, pitchUp,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.DOWN, pitchDown,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.SPACE, toggleMount,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button.Axis.Y, yAxis,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

	}

	@Override
	public void update() { // rotate dolphin if not paused
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if (!paused)
			elapsTime += (currFrameTime - lastFrameTime) / 1000.0;
		// avatar.setLocalRotation((new Matrix4f()).rotation((float) elapsTime, 0, 1,
		// 0));

		// build and set HUD
		int elapsTimeSec = Math.round((float) elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "Keyboard hits = " + counterStr;
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(0, 0, 1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		im.update((float) elapsTime);
		positionCamera();
		determineSatelliteColor();

	}

	public void positionCamera() {
		if (mounted) {
			// camera locked to avatar (mounted)
			cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
			loc = avatar.getWorldLocation();
			fwd = avatar.getWorldForwardVector();
			up = avatar.getWorldUpVector();
			right = avatar.getWorldRightVector();
			cam.setU(right);
			cam.setV(up);
			cam.setN(fwd);
			cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
		} else {
			//camera unlocked, dismounted
			
		}

	}

	public void determineSatelliteColor() {

		// cube, torus, sphere, manual shape

		// DISTANCE 1 GOOD FOR DISARM RANGE

		if (cube.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
			cube.setTextureImage(red);
		} else if (cube.getWorldLocation().distance(avatar.getWorldLocation()) < 2) {
			cube.setTextureImage(yellow);
		} else {
			cube.setTextureImage(green);
		}

		if (sphere.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
			sphere.setTextureImage(yellow);
		} else {
			sphere.setTextureImage(green);
		}

		if (torus.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
			torus.setTextureImage(yellow);
		} else {
			torus.setTextureImage(green);
		}

	}

	public GameObject getAvatar() {
		return avatar;
	}

	public boolean getMounted() {
		return mounted;
	}

}