package org.bjason.daydream;

import org.bjason.daydream.VRDemo.PositionListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Controller.ConnectionStates;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vr.sdk.controller.ControllerManager.ApiStatus;

import android.os.Bundle;
import android.os.Handler;

public class AndroidLauncher extends AndroidApplication {

	private ControllerManager controllerManager;
	private Controller controller;
	VRDemo basicVR;
	private GvrAudioEngine gvrAudioEngine;
	protected int soundId;
	private static final String SOUND_FILE = "cube_sound.wav";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGyroscope = true;
		config.useImmersiveMode = true;
		config.useWakelock = true;
		config.useRotationVectorSensor = true;
		AndroidCompat.setVrModeEnabled(this, true);

		basicVR = new VRDemo(new VRDemo.PositionListener() {
			@Override
			public void listen() {
				gvrAudioEngine.setHeadPosition(x, y, z);
				gvrAudioEngine.setHeadRotation(rotation.x, rotation.y, rotation.z, rotation.w);
			}

			@Override
			public void soundListen() {
				gvrAudioEngine.setSoundObjectPosition(soundId, this.spatialSoundPosition.x, spatialSoundPosition.y,
						spatialSoundPosition.z);
			}

			@Override
			public void startPlaySound() {
				gvrAudioEngine.resumeSound(soundId);

				Gdx.app.log(VRDemo.TAG, "SOUND play"+soundId);
			}

			@Override
			public void stopPlaySound() {
				gvrAudioEngine.pauseSound(soundId);
				Gdx.app.log(VRDemo.TAG, "SOUND stop");

			}
		});

		initialize(basicVR, config);

		EventListener listener = new EventListener();
		controllerManager = new ControllerManager(this, listener);
		controller = controllerManager.getController();
		controller.setEventListener(listener);

		gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
		// Avoid any delays during start-up due to decoding of sound files.
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Start spatial audio playback of SOUND_FILE at the model postion. The returned
				// soundId handle is stored and allows for repositioning the sound object
				// whenever
				// the cube position changes.
				gvrAudioEngine.preloadSoundFile(SOUND_FILE);
				soundId = gvrAudioEngine.createSoundObject(SOUND_FILE);
				gvrAudioEngine.playSound(soundId, true /* looped playback */);
				gvrAudioEngine.pauseSound(soundId);
				Gdx.app.log(VRDemo.TAG, "SOUND loaded"+soundId);

			}
		}).start();

	}

	@Override
	protected void onStart() {
		super.onStart();
		controllerManager.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		gvrAudioEngine.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		gvrAudioEngine.pause();
	}

	@Override
	protected void onStop() {
		controllerManager.stop();
		super.onStop();
	}

	private Handler uiHandler = new Handler();

	static long logTime = 0L;
	static long nextLog = 0L;

	private class EventListener extends Controller.EventListener implements ControllerManager.EventListener, Runnable {

		private String apiStatus;
		float joystickSensitive = 25f;

		private int controllerState = ConnectionStates.DISCONNECTED;

		@Override
		public void onApiStatusChanged(int state) {
			apiStatus = ApiStatus.toString(state);
			Gdx.app.log(VRDemo.TAG, "Controller api status " + apiStatus);

			uiHandler.post(this);
		}

		@Override
		public void onConnectionStateChanged(int state) {
			controllerState = state;
			if (state == ConnectionStates.CONNECTED)
				centredController = false;
			Gdx.app.log(VRDemo.TAG, "Controller state changed " + controllerState);
			uiHandler.post(this);

		}

		@Override
		public void onUpdate() {
			uiHandler.post(this);
		}

		boolean log() {
			if (logTime >= nextLog)
				return true;
			return false;
		}

		boolean centredController = true;
		float[] offset = new float[3];

		@Override
		public void run() {

			controller.update();
			Gdx.app.log(VRDemo.TAG, "Controller state " + controllerState);

			logTime = System.currentTimeMillis();

			centreJoystick();

			if (controller.isTouching) {
				DaydreamController.keys.put(DaydreamController.DM_TOUCHPAD_TOUCHED,
						DaydreamController.DM_TOUCHPAD_TOUCHED);
			}
			if (controller.appButtonState) {
				// pressed the app button below touchpad so lets reset
				centredController = true;
			}
			if (controller.clickButtonState) {
				DaydreamController.keys.put(DaydreamController.DM_CLICK_PRESSED, DaydreamController.DM_CLICK_PRESSED);
			}

			sampleControllerJoystick();

		}

		private void sampleControllerJoystick() {
			float[] angles = new float[3];
			controller.orientation.toYawPitchRollDegrees(angles);
			if (centredController) {
				offset[0] = angles[0];
				centredController = false;
			}
			float x = angles[0] - offset[0];
			float y = angles[1] - offset[1];
			float z = angles[2] - offset[2];

			if (z > joystickSensitive || x > joystickSensitive) {
				DaydreamController.keys.put(DaydreamController.DM_LEFT, DaydreamController.DM_LEFT);
				if (log()) {
					Gdx.app.log(VRDemo.TAG, String.format("LEFT joystick %f %f %s", x, y, z));
				}
			}
			if (z < -joystickSensitive || x < -joystickSensitive) {
				DaydreamController.keys.put(DaydreamController.DM_RIGHT, DaydreamController.DM_RIGHT);
				if (log()) {
					Gdx.app.log(VRDemo.TAG, String.format("RIGHT joystick %f %f %s", x, y, z));
				}
			}

			if (y < -joystickSensitive) {
				DaydreamController.keys.put(DaydreamController.DM_DOWN, DaydreamController.DM_DOWN);
				if (log()) {
					Gdx.app.log(VRDemo.TAG, String.format("DOWN joystick %f %f %s", x, y, z));
				}
			}
			if (y > joystickSensitive) {
				DaydreamController.keys.put(DaydreamController.DM_UP, DaydreamController.DM_UP);
				if (log()) {
					Gdx.app.log(VRDemo.TAG, String.format("UP joystick %f %f %s", x, y, z));
				}
			}

			if (log()) {
				nextLog = logTime + 1000;
			}
		}

		private void centreJoystick() {
			DaydreamController.keys.remove(DaydreamController.DM_CLICK_PRESSED, 0);
			DaydreamController.keys.remove(DaydreamController.DM_TOUCHPAD_TOUCHED, 0);
			DaydreamController.keys.remove(DaydreamController.DM_DOWN, 0);
			DaydreamController.keys.remove(DaydreamController.DM_UP, 0);
			DaydreamController.keys.remove(DaydreamController.DM_LEFT, 0);
			DaydreamController.keys.remove(DaydreamController.DM_RIGHT, 0);
		}

		@Override
		public void onRecentered() {
			centredController = false;
		}
	}
}
