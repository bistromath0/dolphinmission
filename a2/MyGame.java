package a2;

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
import tage.nodeControllers.CustomController;
import tage.nodeControllers.RotationController;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import java.util.Arrays;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private boolean renderAxis = true, paused = false;
	private int counter = 0;
	private double lastFrameTime, currFrameTime, elapsTime;
	private String dispStr1, dispStr2 = "Game start";
	private String scoreString = " Score = 0";

	private GameObject avatar, cube, torus, sphere, x, y, z, rom, floor, childCube, childSphere, childTorus;
	private ObjShape dolS, cubeS, torusS, sphereS, linxS, linyS, linzS, romS, floorS;
	private TextureImage doltx, red, yellow, green, defused, brick;
	private Light light1, cubeLight, sphereLight, torusLight;
	private Camera cam, overheadCam;
	private CameraOrbit3D orbitController;
	private int[] satelliteStates = { 0, 0, 0 }; // cube, sphere, torus
													// 0 = green, 1 = yellow, -2 = red
	private boolean[] satelliteDefusable = { false, false, false };

	private Vector3f loc, fwd, up, right, newLocation;
	private Matrix4f initialRotation;

	private boolean cubeChangeable = true, torusChangeable = true, sphereChangeable = true;

	private int score = 0;

	private NodeController rotationController, customController;

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
		floorS = new Plane();
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		red = new TextureImage("red.png");
		green = new TextureImage("green.png");
		yellow = new TextureImage("yellow.png");
		defused = new TextureImage("defused.png");
		brick = new TextureImage("brick.jpg");

	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale;
		floor = new GameObject(GameObject.root(), floorS, brick);
		floor.getRenderStates().setTiling(1);
		floor.getRenderStates().setTileFactor(100);
		initialScale = (new Matrix4f()).scaling(20f);
		floor.setLocalScale(initialScale);

		rom = new GameObject(GameObject.root(), romS);
		initialTranslation = (new Matrix4f()).translation(0, -3, 0);
		rom.setLocalTranslation(initialTranslation);
		rom.getRenderStates().hasLighting(true);

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f, 0.25f, 1f);
		avatar.setLocalTranslation(initialTranslation);

		cube = new GameObject(GameObject.root(), cubeS, green);
		initialTranslation = (new Matrix4f()).translation(5, 0.25f, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);

		torus = new GameObject(GameObject.root(), torusS, green);
		initialTranslation = (new Matrix4f()).translation(4, 0.25f, 4);
		initialScale = (new Matrix4f()).scaling(0.5f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);

		sphere = new GameObject(GameObject.root(), sphereS, green);
		initialTranslation = (new Matrix4f()).translation(-5, 0.25f, 0);
		initialScale = (new Matrix4f()).scaling(0.5f);
		sphere.setLocalTranslation(initialTranslation);
		sphere.setLocalScale(initialScale);

		childCube = new GameObject(GameObject.root(), cubeS, defused);
		initialTranslation = (new Matrix4f()).translation(-0.5f, 0.25f, 1f);
		initialScale = (new Matrix4f()).scaling(0.1f);
		childCube.setLocalTranslation(initialTranslation);
		childCube.setLocalScale(initialScale);
		childCube.setParent(avatar);
		childCube.propagateRotation(true);
		childCube.applyParentRotationToPosition(true);

		childSphere = new GameObject(GameObject.root(), sphereS, defused);
		initialTranslation = (new Matrix4f()).translation(0.5f, 0.25f, 1f);
		initialScale = (new Matrix4f()).scaling(0.1f);
		childSphere.setLocalTranslation(initialTranslation);
		childSphere.setLocalScale(initialScale);
		childSphere.setParent(avatar);
		childSphere.propagateRotation(true);
		childSphere.applyParentRotationToPosition(true);

		childTorus = new GameObject(GameObject.root(), torusS, defused);
		initialTranslation = (new Matrix4f()).translation(0f, 0.25f, 1f);
		initialScale = (new Matrix4f()).scaling(0.1f);
		childTorus.setLocalTranslation(initialTranslation);
		childTorus.setLocalScale(initialScale);
		childTorus.setParent(avatar);
		childTorus.propagateRotation(true);
		childTorus.applyParentRotationToPosition(true);

		(childCube.getRenderStates()).disableRendering();
		(childSphere.getRenderStates()).disableRendering();
		(childTorus.getRenderStates()).disableRendering();

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
	public void createViewports() {

		// ------------- positioning the camera -------------
		(engine.getRenderSystem()).addViewport("MAIN", 0, 0, 1, 1);
		engine.getRenderSystem().addViewport("OVERHEAD", 0, 0, 0.2f, 0.2f);
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0, 0, 5));
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		overheadCam = (engine.getRenderSystem().getViewport("OVERHEAD").getCamera());
		overheadCam.setLocation(new Vector3f(0, 5, 0));
		overheadCam.lookAt(0, 0, 0);
		System.out.println("viewports created");
	}

	@Override
	public void initializeGame() {

		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);
		im = engine.getInputManager();
		orbitController = new CameraOrbit3D(cam, avatar, engine);

		rotationController = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		customController = new CustomController(engine);
		customController.enable();

		rotationController.addTarget(childCube);
		rotationController.addTarget(childSphere);
		rotationController.addTarget(childTorus);

		rotationController.enable();
		(engine.getSceneGraph()).addNodeController(rotationController);
		(engine.getSceneGraph()).addNodeController(customController);

		FwdAction fwdAction = new FwdAction(this);
		TurnAction turnAction = new TurnAction(this);
		BackAction backAction = new BackAction(this);
		RotLeft rotLeft = new RotLeft(this);
		RotRight rotRight = new RotRight(this);
		PitchUp pitchUp = new PitchUp(this);
		PitchDown pitchDown = new PitchDown(this);
		YAxisAction yAxis = new YAxisAction(this);
		OverHeadCamMovement ohc = new OverHeadCamMovement(this);
		DefuseAction defuse = new DefuseAction(this);
		ToggleAxisLines tog = new ToggleAxisLines(this);

		// im.associateActionWithAllGamepads(
		// net.java.games.input.Component.Identifier.Button._1, fwdAction,
		// InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// im.associateActionWithAllGamepads(
		// net.java.games.input.Component.Identifier.Axis.X, turnAction,
		// InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
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
				net.java.games.input.Component.Identifier.Key.SPACE, defuse,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.TAB, tog,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// im.associateActionWithAllGamepads(
		// net.java.games.input.Component.Identifier.Button.Axis.Y, yAxis,
		// InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// I J K L to control overhead camera
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.I, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.J, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.K, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.L, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.U, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.O, ohc,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

	}

	@Override
	public void update() { // rotate dolphin if not paused
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if (!paused)
			elapsTime += (currFrameTime - lastFrameTime) / 1000.0;

		if (renderAxis == false) {
			(x.getRenderStates()).disableRendering();
			(y.getRenderStates()).disableRendering();
			(z.getRenderStates()).disableRendering();
		} else {
			(x.getRenderStates()).enableRendering();
			(y.getRenderStates()).enableRendering();
			(z.getRenderStates()).enableRendering();
		}

		// build and set HUD
		int elapsTimeSec = Math.round((float) elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		scoreString = " Score = " + score;
		int avatarX = (int) avatar.getWorldLocation().x();
		int avatarY = (int) avatar.getWorldLocation().y();
		int avatarZ = (int) avatar.getWorldLocation().z();
		dispStr1 = "X: " + avatarX + " Y: " + avatarY + " Z: " + avatarZ;
		// dispStr1 = avatar.getWorldLocation().toString();

		if (Arrays.stream(satelliteStates).sum() == 0) {
			// All elements are 0
			dispStr2 = "Go defuse a satellite";
		} else if (Arrays.stream(satelliteStates).sum() <= -1) {
			dispStr2 = "you lose";
		}
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(0, 0, 1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 0, 0);
		(engine.getHUDmanager()).setHUD2(dispStr2 + scoreString, hud2Color, 800, 0);

		im.update((float) elapsTime);
		// positionCamera();
		orbitController.updateCameraPosition();
		determineSatelliteColor();
		if (score == 3) {
			(engine.getHUDmanager()).setHUD2("You Win!", hud2Color, 500, 15);
			// System.exit(0);
			return;
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
				if (satelliteDefusable[0]) {
					customController.addTarget(cube);
					cube.setTextureImage(defused);
					dispStr2 = "Satellite defused";
					System.out.println("DEFUSED");
					cubeChangeable = false;
					(childCube.getRenderStates()).enableRendering();
					customController.addTarget(cube);
					score++;

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
				if (satelliteDefusable[1]) {
					customController.addTarget(sphere);
					sphere.setTextureImage(defused);
					dispStr2 = "Satellite defused";
					System.out.println("DEFUSED");
					sphereChangeable = false;
					score++;
					(childSphere.getRenderStates()).enableRendering();
					customController.addTarget(sphere);
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
				if (satelliteDefusable[2]) {
					customController.addTarget(torus);
					torus.setTextureImage(defused);
					dispStr2 = "Satellite defused";
					System.out.println("DEFUSED");
					torusChangeable = false;
					score++;
					(childTorus.getRenderStates()).enableRendering();
					customController.addTarget(torus);
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

	public Camera getCamera() {
		return cam;
	}

	public Camera getOverHeadCamera() {
		return overheadCam;
	}

	public float checkCameraDolphinDistance() {
		return (avatar.getWorldLocation().distance(cam.getLocation()));
	}

	public int[] getSatelliteStates() {
		return satelliteStates;
	}

	public boolean[] getSatelliteDefusable() {
		return satelliteDefusable;
	}

	public void setRenderAxis(Boolean b) {
		renderAxis = b;
	}

	public Boolean getRenderAxis() {
		return renderAxis;
	}

}