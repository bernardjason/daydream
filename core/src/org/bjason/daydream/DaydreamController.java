package org.bjason.daydream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

public class DaydreamController extends InputAdapter {
	private Camera camera;
	public static final IntIntMap keys = new IntIntMap();

	public static boolean left=false;
	public static boolean right=false;
	
	private float velocity = 2;
	private  float degreesPerPixel = 0.25f;
	private final Vector3 tmp = new Vector3();
	boolean touchPadTouched = false;
	public static int DM_LEFT = Keys.LEFT;
	public static int DM_RIGHT = Keys.RIGHT;
	public static int DM_UP = Keys.UP;
	public static int DM_DOWN = Keys.DOWN;
	public static int DM_TOUCHPAD_TOUCHED = Keys.SHIFT_LEFT;
	private static int DM_TOUCHPAD_TOUCHED1 = Keys.SHIFT_LEFT;
	private static int DM_TOUCHPAD_TOUCHED2 = Keys.SHIFT_RIGHT;
	public static int DM_CLICK_PRESSED = Keys.SPACE;
	private boolean touchPadClicked=false;
	private boolean vr;

	public boolean isTouchPadClicked() {
		return touchPadClicked;
	}
	public DaydreamController(Camera camera, boolean vr) {
		this.camera = camera;
		this.vr=vr;
	}

	@Override
	public boolean keyDown(int keycode) {
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys.remove(keycode, 0);
		return true;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	public void setDegreesPerPixel(float degreesPerPixel) {
		this.degreesPerPixel = degreesPerPixel;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (!vr && !touchPadTouched) {
			float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
			float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
			camera.direction.rotate(camera.up, deltaX);
			tmp.set(camera.direction).crs(camera.up).nor();
			camera.direction.rotate(tmp, deltaY);
			return true;
		}
		return false;
	}

	public void update() {
		update(Gdx.graphics.getDeltaTime());
	}

	public void update(float deltaTime) {

		if (keys.containsKey(DM_TOUCHPAD_TOUCHED) || keys.containsKey(DM_TOUCHPAD_TOUCHED1)
				|| keys.containsKey(DM_TOUCHPAD_TOUCHED2)) {
			tmp.set(camera.direction).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
			touchPadTouched = true;
		} else {
			touchPadTouched = false;
		}
		if (keys.containsKey(DM_LEFT) || left) {
			camera.direction.rotate(camera.up, degreesPerPixel);
		}
		if (keys.containsKey(DM_RIGHT) || right) {
			camera.direction.rotate(camera.up, -degreesPerPixel);
		}
		if (keys.containsKey(DM_UP)) {
			tmp.set(camera.direction).crs(camera.up).nor();
			camera.direction.rotate(tmp, degreesPerPixel);
		}
		if (keys.containsKey(DM_DOWN)) {
			tmp.set(camera.direction).crs(camera.up).nor();
			camera.direction.rotate(tmp, -degreesPerPixel);
		}

		if (keys.containsKey(DM_CLICK_PRESSED)) {
			touchPadClicked=true;
		} else {
			touchPadClicked=false;
		}
		camera.update();
	}

}