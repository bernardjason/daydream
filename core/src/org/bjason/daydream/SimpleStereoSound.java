package org.bjason.daydream;

import org.bjason.daydream.VRDemo.PositionListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;

public class SimpleStereoSound extends PositionListener {

	Sound sound;
	long id;
	float pan=0;
	float volume=1;

	@Override
	public void onCreate(Camera camera) {
		super.onCreate(camera);
		sound = Gdx.audio.newSound(Gdx.files.internal("data/cube_sound.wav"));
		id = sound.play(volume);     
		sound.pause();
	}

	@Override
	public void stopPlaySound() {
		sound.pause();
		
	}
	
	@Override
	public void startPlaySound() {
		sound.setPan(id, pan,volume);    // sets the pan of the sound to the left side at full volume
		sound.setLooping(id, true); // keeps the sound looping
		sound.resume(id);

	}
	
	@Override
	public void soundListen() {
		Vector2 obj = new Vector2(spatialSoundPosition.x,spatialSoundPosition.z);
	
		Vector2 me = new Vector2(x,z);

		Vector2 d = new Vector2(camera.direction.x, camera.direction.z).nor();
		float da=d.angle()-180;
		
		float angle = (float) (180.0 / Math.PI * Math.atan2(obj.x - me.x, obj.y - me.y));
		
		float diff=0;
		diff = angle - da ;
		if ( diff < 0 ) diff = diff + 360;
		if ( diff > 180 ) diff = diff - 360;
		
		if ( me.y < obj.y ) diff=diff*-1;
		if ( me.x > obj.x ) diff=diff*-1;

		float oldpan=pan;
		float oldvolume=volume;
		if ( diff > 1 ) pan=  360 / diff ;
		else if  ( diff < -1  ) pan= 360 / diff;
		else pan=0;

		pan = ( (float)Math.round(diff / 180 * 10 ) )  / 10 / 2;
		volume = (float)Math.round( 1/me.dst(obj) * 100 ) / 10 ;

		if ( oldpan != pan  || oldvolume != volume) {
			stopPlaySound();
			startPlaySound();
		}
	}
	
	@Override
	public void listen() {
		
	}
}
