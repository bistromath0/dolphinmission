package a1;

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
import java.util.Arrays;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private boolean paused = false;
	private int counter = 0;
	private double lastFrameTime, currFrameTime, elapsTime;
	private String dispStr2 = "Game start";
	private boolean mounted = true;

	private GameObject avatar, cube, torus, sphere, x, y, z, rom;
	private ObjShape dolS, cubeS, torusS, sphereS, linxS, linyS, linzS, romS;
	private TextureImage doltx, red, yellow, green, defused;
	private Light light1, cubeLight, sphereLight, torusLight;
	private Camera cam;
	private int[] satelliteStates = { 0, 0, 0 }; // cube, sphere, torus
													// 0 = green, 1 = yellow, -2 = red

	private Vector3f loc, fwd, up, right, newLocation;
	private Matrix4f initialRotation;

	private boolean cubeChangeable = true, torusChangeable = true, sphereChangeable = true;

	private int score = 0;

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
		romS = new ManualPyramid();
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		red = new TextureImage("red.png");
		green = new TextureImage("green.png");
		yellow = new TextureImage("yellow.png");
		defused = new TextureImage("defused.png");

	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale;
		rom = new GameObject(GameObject.root(), romS);
		initialTranslation = (new Matrix4f()).translation(0, -3, 0);
		rom.setLocalTranslation(initialTranslation);
		rom.getRenderStates().hasLighting(true);

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f, 0f, 1f);
		avatar.setLocalTranslation(initialTranslation);
		cube = new GameObject(GameObject.root(), cubeS, green);
		initialTranslation = (new Matrix4f()).translation(5, 0, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);

		torus = new GameObject(GameObject.root(), torusS, green);
		initialTranslation = (new Matrix4f()).translation(0, 5, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);

		sphere = new GameObject(GameObject.root(), sphereS, green);
		initialTranslation = (new Matrix4f()).translation(-5, 0, 0);
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
		cubeLight = new Light();
		sphereLight = new Light();
		torusLight = new Light();

		Vector3f bumper = new Vector3f(0f, 1f, 0f);
		cubeLight.setLocation(cube.getWorldLocation().add(bumper));
		sphereLight.setLocation(sphere.getWorldLocation().add(bumper));
		torusLight.setLocation(torus.getWorldLocation().add(bumper));
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		cubeLight.setAmbient(0, 1, 0);
		sphereLight.setAmbient(0, 0, 1);
		torusLight.setAmbient(1, 0, 0);

		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(cubeLight);
		(engine.getSceneGraph()).addLight(torusLight);
		(engine.getSceneGraph()).addLight(sphereLight);
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0, 0, 5));

		positionCamera();
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
				net.java.games.input.Component.Identifier.Key.UP, pitchDown,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.DOWN, pitchUp,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.SPACE, toggleMount,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
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

		// build and set HUD
		int elapsTimeSec = Math.round((float) elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Score = " + score;
		if (Arrays.stream(satelliteStates).sum() == 0) {
			// All elements are 0
			dispStr2 = "Go defuse a satellite";
		} else if (Arrays.stream(satelliteStates).sum() <= -1) {
			dispStr2 = "you lose";
		}
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(0, 0, 1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		im.update((float) elapsTime);
		positionCamera();
		determineSatelliteColor();
		if (score == 3) {
			(engine.getHUDmanager()).setHUD2("You Win!", hud2Color, 500, 15);
			System.exit(0);
		}
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
			// camera unlocked, dismounted

		}

	}

	public void determineSatelliteColor() {

		// cube, torus, sphere, manual shape
		if (cubeChangeable) {
			if (cube.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
				cube.setTextureImage(red); // trigger explosion - deactivate color changing
				cubeChangeable = false;
				dispStr2 = "you lose!";
				satelliteStates[0] = -2;
				// insert code for console displaying explosion, end game
			} else if (cube.getWorldLocation().distance(avatar.getWorldLocation()) < 2) {
				cube.setTextureImage(yellow);
				satelliteStates[0] = 1;
				dispStr2 = "close enough";
				if (!getMounted()) {
					if (cube.getWorldLocation().distance(cam.getLocation()) < 1) {
						cube.setTextureImage(defused);
						dispStr2 = "Satellite defused";
						System.out.println("DEFUSED");
						cubeChangeable = false;
						score++;
					}
				}
			} else {
				satelliteStates[0] = 0;
				cube.setTextureImage(green);
			}
		}

		if (sphereChangeable) {
			if (sphere.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
				sphere.setTextureImage(red); // trigger explosion - deactivate color changing
				sphereChangeable = false;
				dispStr2 = "you lose!";
				satelliteStates[1] = -2;
				// insert code for console displaying explosion, end game
			} else if (sphere.getWorldLocation().distance(avatar.getWorldLocation()) < 2) {
				sphere.setTextureImage(yellow);
				satelliteStates[1] = 1;
				dispStr2 = "close enough";
				if (!getMounted()) {
					if (sphere.getWorldLocation().distance(cam.getLocation()) < 1) {
						sphere.setTextureImage(defused);
						dispStr2 = "Satellite defused";
						System.out.println("DEFUSED");
						sphereChangeable = false;
						score++;
					}
				}
			} else {
				satelliteStates[1] = 0;
				sphere.setTextureImage(green);
			}
		}
		if (torusChangeable) {
			if (torus.getWorldLocation().distance(avatar.getWorldLocation()) < 1) {
				torus.setTextureImage(red); // trigger explosion - deactivate color changing
				dispStr2 = "you lose!";
				satelliteStates[2] = -2;
				torusChangeable = false;
				// insert code for console displaying explosion, end game
			} else if (torus.getWorldLocation().distance(avatar.getWorldLocation()) < 2) {
				torus.setTextureImage(yellow);
				satelliteStates[2] = 1;
				dispStr2 = "close enough";
				if (!getMounted()) {
					if (torus.getWorldLocation().distance(cam.getLocation()) < 1) {
						torus.setTextureImage(defused);
						dispStr2 = "Satellite defused";
						System.out.println("DEFUSED");
						torusChangeable = false;
						score++;
					}
				}
			} else {
				satelliteStates[2] = 0;
				torus.setTextureImage(green);
			}
		}

	}

	public GameObject getAvatar() {
		return avatar;
	}

	public boolean getMounted() {
		return mounted;
	}

	public void setMounted(boolean b) {
		mounted = b;
	}

	public void dismount() {
		// offsets the camera from the dolphin
		cam.setLocation(loc.add(right.mul(0.3f)).add(fwd.mul(-0.7f)).add(up.mul(-0.5f)));
		setMounted(!getMounted());

	}

	public Camera getCamera() {
		return cam;
	}

	public void remount() {
		loc = avatar.getWorldLocation();
		fwd = avatar.getWorldForwardVector();
		up = avatar.getWorldUpVector();
		right = avatar.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
		setMounted(!getMounted());
	}

	public float checkCameraDolphinDistance() {
		return (avatar.getWorldLocation().distance(cam.getLocation()));
	}

}