package hu.juhdanad.neonjumper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Launcher extends Game {
    /**
     * The MainMenu screen.
     */
    public MainMenu mmn;
    /**
     * This InputAdapter measures the distance the user's finger moved on the screen
     */
    public final InputAdapter inAd = new InputAdapter() {
        /**
         * The finger's latest x coordinate.
         */
        private int x;
        /**
         * The finger's latest y coordinate.
         */
        private int y;
        /**
         * The distance since the last touch.
         */
        private float d;
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            x = screenX;//set finger position
            y = screenY;
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            d = 0;//set distance to zero
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            int dx = x - screenX;//calculate the change in position
            int dy = y - screenY;
            d += Math.sqrt(dx * dx + dy * dy);//update the distance
            x = screenX;//update finger position
            y = screenY;
            if (d > 1000) {//distance is enough
                d = 0;//reset counting
                if (getScreen().equals(mmn)) {//Is the menu visible?
                    Gdx.app.exit();//exit
                } else {
                    showMenu();//exit to main menu
                }
            }
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            if(keycode== Input.Keys.Q||keycode==Input.Keys.ESCAPE){
                Gdx.app.exit();
            }
            return true;
        }
    };
    /**
     * The game's font
     */
    public BitmapFont font;
    /**
     * The default 12.8x8 camera
     */
    public OrthographicCamera camera;
    /**
     * 256*160 view to render text
     */
    public OrthographicCamera textCamera;
    /**
     * The game itself
     */
    private GameScreen gamescreen;
    /**
     * The win screen
     */
    private WinScreen win;

    @Override
    public void create() {
        Pixmap pm=new Pixmap(16,16, Pixmap.Format.RGBA8888);
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm,0,0));
        pm.dispose();
        font = new BitmapFont();//set font to default Arial 15pt
        camera = new OrthographicCamera();//Initialize cameras
        textCamera = new OrthographicCamera();
        mmn = new MainMenu(this);//Create game screens and set their parents
        gamescreen = new GameScreen(this);
        win = new WinScreen(this);
        setScreen(mmn);//show main menu
    }

    @Override
    public void dispose() {
        super.dispose();//let Game to dispose
        mmn.dispose();//delete textures and other stuff from memory
        gamescreen.dispose();
        win.dispose();
        font.dispose();
    }

    public void startGame(boolean impossible) {
        gamescreen.reset();//reset the game
        gamescreen.impossible = impossible;//set whether the game is impossible
        setScreen(gamescreen);//show the game
    }

    public void showMenu() {
        setScreen(mmn);//show the menu
    }

    public void showWin(boolean impossible) {
        win.setImpossible(impossible);//pass the impossible field
        setScreen(win);//show win screen
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);//call parent object's method
        if (width / 1280f >= height / 800f) {//if the width is greater or equal to the optimal value
            float ratio = 800f / height * width / 1280f;//calculate width ratio
            float w = 12.8f * ratio;//the camera's real width
            camera.setToOrtho(false, w, 8);//set camera
            camera.translate((12.8f - w) / 2, 0);//translate camera so content will be centered
            w = 256f * ratio;//set the text camera
            textCamera.setToOrtho(false, w, 160f);
            textCamera.translate((256f - w) / 2, 0);
        } else {
            float ratio = 1280f / width * height / 800f;//set height ratio
            float h = 8f * ratio;//set real camera
            camera.setToOrtho(false, 12.8f, h);
            camera.translate(0, (8f - h) / 2);
            h = 160f * ratio;//set text camera
            textCamera.setToOrtho(false, 256f, h);
            textCamera.translate(0, (160f - h) / 2);
        }
    }
}
