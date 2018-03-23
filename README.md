# daydream
libgdx and daydream vr

sample of using libgdx to produce dual display for VR headset, in this case Daydream View. Not a library so 
you can see camera displacement when rendering to offset one eye along with two render operations to a framebuffer
followed by use of shader to alter final frame.

This is essentially standard libgdx, using a shader to handle lens distort, otherwise straight lines would be warped. The android version 
uses the handheld controller to move the player around the small world along with spacial audio, via the Google VR SDK. 
The code for this is just in the bespoke Android module. 

While desktop version uses arrow keys and spacebar to move along with some basic stereo audio using libgdx apis. 

For the HTML version there is a very basic use of device orientation to move player view, but without allowing movement. It looks possible
to integrate Daydream handheld controller with HTML version, see https://github.com/mrdoob/daydream-controller.js
though I've not tired it. HTML version was really to check project is as close as possible to being able to work
on ios. Though like HTML version I don't see official support for handheld controller. Got around full screen problem when
viewed on browser but sound does not work on mobile browser as not driven by user interaction.

## android
to build and deploy

./gradlew android:installDebug

to make just the apk
./gradlew android:assembleDebug

[daydream apk debug build apk](https://bjason.org/wp-content/uploads/daydream/android-debug.apk)


Tilt controller to change direction. Touch the touchpad to move while click touchpad when close and looking directly at spinning cube to drop it.
Press App button (immediately below touchpad) will re-centre controller.

See [youtube short video](https://youtu.be/EO1vzoIGk9E)

## desktop deploy
./gradlew desktop:run

./gradlew desktop:dist

[daydream desktop jar](https://bjason.org/wp-content/uploads/daydream/desktop-1.0.jar)

## HTML deploy

./gradlew html:superDev
./gradlew html:dist

Try online
[daydream for browser](https://bjason.org/wp-content/uploads/daydream/index.html)
