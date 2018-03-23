package org.bjason.daydream;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class VRDemo extends ApplicationAdapter {
	private static final float SPIN = 0.5f;
	public static final String TAG = "VR";
	private static final float MIN_HEIGHT = 2;
	private static final float MAX_HEIGHT = 20;

	public VRDemo(PositionListener listen) {
		super();
		positionListeners.add(listen);
	}

	public VRDemo(boolean vr, boolean flip) {
		super();
		this.vr = vr;
		this.flip = flip;
	}
	public void addPositionListener(PositionListener listener) {
		positionListeners.add(listener);
	}
	
	static public abstract class PositionListener {
		protected float x;
		protected float y;
		protected float z;
		protected Quaternion rotation = new Quaternion();
		protected Vector3 spatialSoundPosition = new Vector3();
		protected Camera camera;
		
		public void update() {

			this.x = camera.position.x;
			this.y = camera.position.y;
			this.z = camera.position.z;
			
			camera.combined.getRotation(rotation);
		}
		public void soundPosition(Vector3 position) {
			spatialSoundPosition.set(position);
		}
		abstract public void listen() ;
		abstract public void soundListen() ;
		abstract public void startPlaySound() ;
		abstract public void stopPlaySound() ;
		public void playSound(boolean playIt) {
			if ( playIt )  startPlaySound();
			else stopPlaySound();
		}
		public void onCreate(Camera camera)  {
			this.camera = camera;
		}
		
	}
	private List<PositionListener> positionListeners = new ArrayList<PositionListener>();

	Array<ModelInstance> modelInstances = new Array<ModelInstance>();
	Array<ModelInstance> cubes = new Array<ModelInstance>();
	boolean vr = true;
	boolean flip = true;

	ModelBatch batch;
	List<Model> cubeModel = new ArrayList<Model>();
	Model floorModel;
	ModelInstance floor;
	int floorSize = 64;

	FPSLogger logger = new FPSLogger();
	public PerspectiveCamera camera;
	public DaydreamController daydreamController;

	private Texture texture = null;

	public Shader shader;
	private FrameBuffer frameBuffer;
	private TextureRegion textureRegion;
	private SpriteBatch spriteBatch;
	static final int screens = 2;
	ModelInstance currentCube;

	@Override
	public void create() {
		//if ( Gdx.app.getType() == ApplicationType.WebGL) {
            		//Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        	//}

		frameBuffer = new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		Gdx.graphics.setVSync(true);

		String vertexShader = Gdx.files.internal("data/vertex.glsl").readString();
		String fragmentShader = Gdx.files.internal("data/fragment.glsl").readString();
		ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);

		spriteBatch = new SpriteBatch();
		spriteBatch.setShader(shaderProgram);

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1f;
		camera.far = 300f;
		camera.update();
		daydreamController = new DaydreamController(camera,vr);
		camera.position.y = MIN_HEIGHT;
		Gdx.input.setInputProcessor(daydreamController);

		camera.update();

		for(PositionListener l:positionListeners ) {
			l.onCreate(camera);
		}
		
		batch = new ModelBatch();

		ModelBuilder modelBuilder = new ModelBuilder();

		addCube(modelBuilder);
		createFloor(modelBuilder);

	}

	private void pickRandomCube() {
		boolean done = false;
		currentCube=null;
		for(PositionListener l:positionListeners ) {
			l.playSound(false);
		}
		int j = (int) (Math.random() * 1000) % cubes.size;
		j = 9;
		for (int i = 0; i < cubes.size; i++) {
			if (j >= cubes.size) j = 0;

			ModelInstance mi = cubes.get(j++);

			mi.transform.getTranslation(tmp3);
			if (!done && mi.userData == null && tmp3.y > 0) {
				mi.userData = "pickme";
				currentCube=mi;
				done = true;
				for(PositionListener l:positionListeners ) {
					l.soundPosition(tmp3);
					l.playSound(true);
				}
			}
		}
	}

	private void addCube(ModelBuilder modelBuilder) {

		makeCubeModel(modelBuilder, new Color[] { Color.RED, Color.YELLOW });
		makeCubeModel(modelBuilder, new Color[] { Color.ORANGE, Color.GREEN });
		makeCubeModel(modelBuilder, new Color[] { Color.CYAN, Color.BLUE });
		makeCubeModel(modelBuilder, new Color[] { Color.PINK, Color.PURPLE });
		makeCubeModel(modelBuilder, new Color[] { Color.CORAL, Color.MAGENTA });

		int i = 0;
		for (int x = -floorSize / 2; x < floorSize / 2; x = x + floorSize / 3) {
			i = Math.abs(x);
			for (int z = -floorSize / 2; z < floorSize / 2; z = z + floorSize / 3) {
				if (x != 0 && z != 0) {
					if (i >= cubeModel.size())
						i = i % cubeModel.size();
					;
					ModelInstance cube = new ModelInstance(cubeModel.get(i++));
					cube.transform.translate(x, 5f, z);
					modelInstances.add(cube);
					cubes.add(cube);
				}
			}
		}
	}

	private void makeCubeModel(ModelBuilder modelBuilder, Color[] colour) {
		int sideSize = 32;
		Pixmap pp = new Pixmap(sideSize * 3, sideSize * 3, Format.RGB888);
		int i = 0;
		int border = 5;
		for (int x = border; x < sideSize * 3; x = x + sideSize) {
			for (int y = border; y < sideSize * 3; y = y + sideSize) {
				pp.setColor(colour[i++]);
				if (i >= colour.length)
					i = 0;
				pp.fillRectangle(x, y, sideSize - border * 2, sideSize - border * 2);
			}
		}

		Texture cubeTexture = new Texture(pp);
		pp.dispose();

		cubeModel.add(modelBuilder.createBox(3, 3, 3, new Material(TextureAttribute.createDiffuse(cubeTexture)),
				Usage.Position | Usage.TextureCoordinates | Usage.Normal));
	}

	private void createFloor(ModelBuilder modelBuilder) {
		int pixSize = floorSize * 2;

		Pixmap p = new Pixmap(pixSize, pixSize, Format.RGB565);
		p.setColor(Color.FIREBRICK);
		float gap = 10;
		for (float xx = 0; xx < pixSize; xx = xx + gap) {
			p.drawLine((int) xx, 0, (int) xx, pixSize);
		}
		for (float y = 0; y < pixSize; y = y + gap) {
			p.drawLine(0, (int) y, pixSize, (int) y);
		}
		Texture floorTexture = new Texture(p);
		p.dispose();

		floorModel = modelBuilder.createRect(-floorSize, 0, -floorSize, -floorSize, 0, floorSize, floorSize, 0,
				floorSize, floorSize, 0, -floorSize, 1, 0, 0,
				new Material(TextureAttribute.createDiffuse(floorTexture)),
				Usage.Position | Usage.TextureCoordinates | Usage.Normal);

		floor = new ModelInstance(floorModel);

		floor.transform.translate(0, -1, 0);
		modelInstances.add(floor);
	}

	static float eyeOff = 0.15f;
	Vector3 eyeOffVec = new Vector3();

	Vector3 tmp3 = new Vector3();
	Vector3 intersection = new Vector3();

	@Override
	public void render() {
		logger.log();
		

		daydreamController.update();

		if (camera.position.y < MIN_HEIGHT) {
			camera.position.y = MIN_HEIGHT;
		}
		if (camera.position.y > MAX_HEIGHT) {
			camera.position.y = MAX_HEIGHT;
		}
		camera.update();

		for(PositionListener l:positionListeners ) {
			l.update();
			l.listen();
			l.soundListen();
		}

			if (currentCube != null) {
				currentCube.transform.getTranslation(tmp3);

				currentCube.transform.rotate(1, 0, 0, SPIN);
				currentCube.transform.rotate(0, 1, 0, SPIN);
				if (tmp3.dst(camera.position) < 10 && daydreamController.isTouchPadClicked()) {
					Ray ray = new Ray(camera.position, camera.direction);
					boolean lookat = Intersector.intersectRaySphere(ray, tmp3, 2, intersection);
					if (lookat) {
						tmp3.y = 0;
						currentCube.transform.setTranslation(tmp3);
						currentCube.userData = null;
						cubes.removeValue(currentCube, true);
						pickRandomCube();
					}
				}
			} else {
				pickRandomCube();
			}

		frameBuffer.begin();
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		renderScene(camera, 0);

		camera.position.x = camera.position.x + eyeOff;
		daydreamController.update();
		renderScene(camera, 1);
		camera.position.x = camera.position.x - eyeOff;
		daydreamController.update();

		frameBuffer.end();
		texture = frameBuffer.getColorBufferTexture();
		textureRegion = new TextureRegion(texture);
		if (flip) {
			textureRegion.flip(false, true);
		}

		spriteBatch.begin();
		spriteBatch.draw(textureRegion, 0, 0);
		spriteBatch.end();

	}

	private void renderScene(Camera camera, int offset) {

		int screenWidth = Gdx.graphics.getWidth();
		int wide = screenWidth / screens;

		Gdx.gl.glViewport(offset * screenWidth / 2, 0, wide, Gdx.graphics.getHeight());

		camera.update();
		batch.begin(camera);
		for (ModelInstance modelInstance : modelInstances) {
			batch.render(modelInstance);
		}
		batch.end();

	}

	@Override
	public void dispose() {
		batch.dispose();
		for (Model m : cubeModel)
			m.dispose();
		floorModel.dispose();
	}


}
