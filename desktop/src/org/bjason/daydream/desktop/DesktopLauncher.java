package org.bjason.daydream.desktop;

import org.bjason.daydream.SimpleStereoSound;
import org.bjason.daydream.VRDemo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
	
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=1280;

		final VRDemo vrDemo = new VRDemo(false,true)  ;
		vrDemo.addPositionListener( new SimpleStereoSound() );
		new LwjglApplication(vrDemo,config);
	}
}
