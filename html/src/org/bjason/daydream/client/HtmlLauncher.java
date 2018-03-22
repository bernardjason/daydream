package org.bjason.daydream.client;

import org.bjason.daydream.SimpleStereoSound;
import org.bjason.daydream.VRDemo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.GwtGraphics.OrientationLockType;
import com.badlogic.gdx.math.Vector3;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

public class HtmlLauncher extends GwtApplication {

	static VRDemo vrDemo;
	private GwtApplicationConfiguration cfg;

	// USE THIS CODE FOR A FIXED SIZE APPLICATION
	// @Override
	// public GwtApplicationConfiguration getConfig () {
	// return new GwtApplicationConfiguration(480, 320);
	// }
	// END CODE FOR FIXED SIZE APPLICATION

	@Override
	public GwtApplicationConfiguration getConfig() {
		int w = Window.getClientWidth() ;
		int h = Window.getClientHeight() ;
		cfg = new GwtApplicationConfiguration(w, h);
		Window.enableScrolling(false);
		Window.setMargin("0");
		Window.addResizeHandler(new ResizeListener());
		cfg.preferFlash = false;
		cfg.fullscreenOrientation = OrientationLockType.LANDSCAPE_PRIMARY;
		return cfg;
	}
	class ResizeListener implements ResizeHandler {

		@Override
		public void onResize(ResizeEvent event) {
			int width = event.getWidth() ;
			int height = event.getHeight() ;
			getRootPanel().setWidth("" + width + "px");
			getRootPanel().setHeight("" + height + "px");
			getApplicationListener().resize(width, height);
			Gdx.graphics.setWindowedMode(width, height);
		}
	}

	private static Vector3 tmp = new Vector3();
	public static void rotateCameraForBrowser(float a, float b, float g) {
		vrDemo.camera.direction.rotate(vrDemo.camera.up, a);

		tmp.set(vrDemo.camera.direction).crs(vrDemo.camera.up).nor();
		vrDemo.camera.direction.rotate(tmp, -g);

		Gdx.graphics.requestRendering();
	}

	public static native void initOrientationForHeadset() /*-{
		var alpha=0.0;
		var beta=0.0;
		var gamma= 90.0;
						
		if (typeof window.orientation == 'undefined')  {
			// desktop only
			gamma = 0;
		}
			
		function deviceOrientationListener(event) {
			//window.focus();
			var a = Math.round(event.alpha * 1000) / 1000;
			var b = Math.round(event.beta * 1000) / 1000;
			var g = Math.round(event.gamma * 1000) / 1000;
			@org.bjason.daydream.client.HtmlLauncher::rotateCameraForBrowser(FFF)(a-alpha,b-beta,g-gamma);
			alpha=a;
			beta=b;
			gamma=g;
		}
												
		if (window.DeviceOrientationEvent) {
			window.addEventListener("deviceorientation", deviceOrientationListener);
		}
	}-*/;

	@Override
	public ApplicationListener createApplicationListener() {
		/*
		 * an excellent chance sound wont work on mobile browser due to security.
		 */
		vrDemo = new HtmlVRDemo(false, true);
		vrDemo.addPositionListener(new SimpleStereoSound());

		return vrDemo;
	}

	class HtmlVRDemo extends VRDemo {

		public HtmlVRDemo(boolean vr, boolean flip) {
			super(vr, flip);
		}

		@Override
		public void create() {
			initOrientationForHeadset();
			super.create();
			Gdx.graphics.setContinuousRendering(false);
		}

		@Override
		public void render() {
			super.render();
		}
	}

}