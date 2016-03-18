package com.dive.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;





public class DiveGame extends ApplicationAdapter implements ApplicationListener, InputProcessor {

	public boolean Android;
	private SpriteBatch batch;
	private ObjectGenerator newObjects;
	private World world;
	private GameState gameState;
	private float deltaTime, pauseCD;
	private BitmapFont font;
	private ArrayList<InputProcessor> processors;

	private float widthScale; //breite der black bars 
	private Sprite bb1, bb2; //blackBars für horizontales 16:9
	private EndScreen endscreen;
	private EndscreenProcessor processor;

	private Stage stage;
	private Touchpad joystick;
	private Drawable knob;
	private Drawable background;
	private TouchpadStyle joystickstyle;
	private Skin skin;
	
	private OrthographicCamera cam;

	public DiveGame(boolean Android) {
		this.Android = Android;
	}
	public DiveGame() {
		this.Android = false;
	}


	@Override
	public void create() {
		
		float h = Gdx.graphics.getHeight();
		float w = Gdx.graphics.getWidth();
		
		batch = new SpriteBatch();
		stage = new Stage();
		
		font = new BitmapFont();
		font.setColor(Color.RED);
		
		gameState = new GameState(1);
		newObjects = new ObjectGenerator(8,8,8,8, 0.1f);
		world = new World(newObjects,0.1f,gameState, font);
		pauseCD = 0;
		
		skin = new Skin();										//Ein Skin wird erzeugt um aus Texture Dateien Drawables zu machen
		skin.add("background",Assets.getInstance().joystickunder);
    		skin.add("knob",Assets.getInstance().joystickup);
		background = skin.getDrawable("background");
    		knob = skin.getDrawable("knob");
    		joystickstyle = new TouchpadStyle(background,knob);		//Joystickstyle wird erstellt bekommt seine Drawables
    	
		knob.setMinWidth(Gdx.graphics.getWidth()/8);						//Größe des Joysticks
		knob.setMinHeight(Gdx.graphics.getWidth()/8);
		
		joystick = new Touchpad(5,joystickstyle);	//Joystick wird erstellt mit Bewegungsradius des Knüppels = 1/10 des Bildschirms
		joystick.setBounds(3*Gdx.graphics.getWidth()/4,  0 , Gdx.graphics.getWidth()/4, Gdx.graphics.getWidth()/4);//Größe und Platzierung des Joystickpads
		if (Android){stage.addActor(joystick);}		
		
		cam = new OrthographicCamera(1920, 1920 * (h / w));
		cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
		cam.update();
        
		bb1 = new Sprite(Assets.getInstance().black);
		bb2 = new Sprite(Assets.getInstance().black);
        
		endscreen = new EndScreen(0);
		processor = new EndscreenProcessor(world, endscreen, gameState);
//		Gdx.input.setInputProcessor(stage);
		
		processors = new ArrayList<InputProcessor>();
		processors.add(processor);
		processors.add(stage);
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(processor);
		inputMultiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(this);
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		Assets.getInstance().dispose();
		stage.dispose();
	}

	@Override
	public void render() {
		
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		deltaTime = Gdx.graphics.getDeltaTime();
		
		//Spiellogik updaten und Welt bewegen
		if(gameState.isRunning()){
			world.update(deltaTime);
			world.move(deltaTime, Android, joystick.getKnobPercentX(),joystick.getKnobPercentY());
		}
		

		//Spiel pausieren
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && pauseCD <= 0){
			gameState.toggle();pauseCD=0.1f;
		}
		pauseCD-=deltaTime;
		

		//batch erstellen
		batch.begin();
			world.draw(batch,Android);
			bb1.draw(batch);
			bb2.draw(batch);
			if(gameState.isEndscreen()){
				endscreen.setScore(2);
				endscreen.draw(batch);
			}
		batch.end();
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		if(width/(float) height < 16.0/9){
			cam.viewportWidth = 1920f;
	        cam.viewportHeight = 1920f * height/width;
	        widthScale = 0;
		}
		else{
			cam.viewportHeight = 1080f;
	        cam.viewportWidth = 1080f * width/height;
	        widthScale = (cam.viewportWidth-1920)/(2*1920);
		}
		bb1.setBounds(-widthScale*1920, 0,widthScale*1920, 1080);
		bb2.setBounds(1920, 0,widthScale*1920, 1080);
        cam.update();
	}

	@Override
	public void pause() {
		gameState.pause();
	}

	@Override
	public void resume() {
		gameState.resume();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		for(InputProcessor p: processors){
			p.keyDown(keycode);
		}
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {
		for(InputProcessor p: processors){
			p.keyUp(keycode);
		}
		return false;
	}
	@Override
	public boolean keyTyped(char character) {
		for(InputProcessor p: processors){
			p.keyTyped(character);
		}
		return false;
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		for(InputProcessor p: processors){
			p.touchDown(screenX, screenY, pointer, button);
		}
		return false;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		for(InputProcessor p: processors){
			p.touchUp(screenX, screenY, pointer, button);
		}
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		for(InputProcessor p: processors){
			p.touchDragged(screenX, screenY, pointer);
		}
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		for(InputProcessor p: processors){
			p.mouseMoved(screenX, screenY);
		}
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		for(InputProcessor p: processors){
			p.scrolled(amount);
		}
		return false;
	}

}
