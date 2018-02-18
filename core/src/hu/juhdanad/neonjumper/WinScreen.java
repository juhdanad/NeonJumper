package hu.juhdanad.neonjumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by danika on 2015.09.19..
 */
public class WinScreen implements Screen {

    /**
     * parent Launcher
     */
    private Launcher parent;
    /**
     * batch to draw
     */
    private SpriteBatch batch = new SpriteBatch();
    /**
     * shown text
     */
    private String text;
    /**
     * which game mode has the player just won
     */
    private boolean impossible;

    public WinScreen(Launcher parent) {
        setImpossible(false);
        this.parent = parent;
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
                    if (impossible) {
                        parent.showMenu();
                    } else {
                        parent.startGame(true);
                    }
                }
                down = false;
                return false;
            }
        }, parent.inAd));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);//clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        parent.textCamera.update();//write text
        batch.setProjectionMatrix(parent.textCamera.combined);
        batch.begin();
        parent.font.setColor(1, 1, 1, 1);
        parent.font.draw(batch, text, 0, 120, 256, 1, false);//see WinScreen
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

    }

    /**
     * set text
     * @param impossible which game mode has the player just won
     */
    public void setImpossible(boolean impossible) {
        this.impossible = impossible;
        if (impossible) {
            text = "Well...\n\n\nYou're a machine.";
        } else {
            text = "Congratulations!\nYou won!\n\nTry the impossible mode now!";
        }
    }
}
