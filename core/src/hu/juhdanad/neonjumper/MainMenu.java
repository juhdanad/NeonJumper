package hu.juhdanad.neonjumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by danika on 2015.09.15..
 */
public class MainMenu implements Screen {
    /**
     * batch to draw
     */
    private SpriteBatch batch;
    /**
     * parent Launcher
     */
    private Launcher parent;
    /**
     * welcome text
     */
    private String str = "Neon Jumper\n\nYour goal is to reach the final flag.\nTap the screen or press a key to jump.\nSketch the screen to exit.\n\nTap or press a key to begin.";

    public MainMenu(Launcher parent) {
        this.parent = parent;
        batch = new SpriteBatch();
    }

    @Override
    public void show() {//see GameScreen
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            private boolean down = false;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                down = true;
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (down) {
                    parent.startGame(false);
                }
                down = false;
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                down = true;
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if(down){
                    parent.startGame(false);
                }
                return false;
            }
        }, parent.inAd));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);//clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        parent.textCamera.update();//draw text
        batch.setProjectionMatrix(parent.textCamera.combined);
        batch.begin();
        parent.font.draw(batch/*batch*/, str/*text*/, 0/*left coordinate*/, 140/*upper coordinate*/, 256/*width*/, 1/*center align*/, false/*do not wrap words*/);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
