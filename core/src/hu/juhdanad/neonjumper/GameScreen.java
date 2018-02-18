package hu.juhdanad.neonjumper;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by danika on 2015.09.18..
 */
public class GameScreen implements Screen {
    /**
     * Impossible mode on/off
     */
    public boolean impossible;
    /**
     * counts the time until respawn
     */
    private float deathCooldown;
    /**
     * parent Game object
     */
    private Launcher parent;
    /**
     * the game's texture
     */
    private Texture sprites;
    /**
     * the ball's position on the texture
     */
    private TextureRegion ball;
    /**
     * the wall's position on the texture
     */
    private TextureRegion wall;
    /**
     * the spike's position on the texture
     */
    private TextureRegion spike;
    /**
     * the checkpoint's position on the texture
     */
    private TextureRegion checkpoint;
    /**
     * the slower's position on the texture
     */
    private TextureRegion slower;
    /**
     * the jumper's position on the texture
     */
    private TextureRegion jumper;
    /**
     * the finish's position on the texture
     */
    private TextureRegion finish;
    /**
     * the sprite batch to draw
     */
    private SpriteBatch batch;

    enum Tile {
        EMPTY, WALL, SPIKE, CHECKPOINT, CHECKPOINT_ACTIVE, SLOW, UP, FINISH
    }

    /**
     * the array storing the level.
     */
    private Tile[][] level;
    /**
     * ball's x coordinate
     */
    private float bobX;
    /**
     * ball's y coordinate
     */
    private float bobY;
    /**
     * ball's y velocity
     */
    private float bobYVel;
    /**
     * is the game over?
     */
    private boolean gameOver;
    /**
     * Does the player jumping?
     */
    private boolean jumpIntent;
    /**
     * can the player jump?
     */
    private boolean canJump;
    /**
     * the ball's current direction
     */
    private boolean leftDirection;
    /**
     * leftDirection after death
     */
    private boolean startLeftDirection;
    /**
     * the respawn x coordinate
     */
    private float startX;
    /**
     * the respawn y coordinate
     */
    private float startY;
    /**
     * the level start x coordinate
     */
    private float originalStartX = 5;
    /**
     * the level start y coordinate
     */
    private float originalStartY = 5;
    /**
     * the ball's x velocity
     */
    private float bobXVel;
    /**
     * the death counter
     */
    private int deathCount;
    /**
     * the x coordinate of the camera's bottom left corner
     */
    private float cameraX;
    /**
     * the y coordinate of the camera's bottom left corner
     */
    private float cameraY;
    /**
     * is respawn counter running?
     */
    private boolean dying;
    /**
     * slower effect countdown
     */
    private float slowTime;
    /**
     * time multiplier, used by slowdown effect
     */
    private float timeMulti;
    /**
     * is the player winning?
     */
    private boolean win;

    public float light;

    public GameScreen(Launcher parent) {
        this.parent = parent;// set parent
        FileHandle leveltext = Gdx.files.internal("level.txt");
        String levelstr = leveltext.readString();
        batch = new SpriteBatch();// create batch
        sprites = new Texture(Gdx.files.internal("GameSprites.png"));// load texture
        TextureRegion[][] textures = TextureRegion.split(sprites, 128, 128);// create texture regions
        ball = textures[0][0];
        wall = textures[1][0];
        spike = textures[0][1];
        checkpoint = textures[1][1];
        slower = textures[0][2];
        jumper = textures[1][2];
        finish = textures[0][3];
        level = new Tile[300][30];// create level array
        for(int x=0;x<level.length;x++){
            for(int y=0;y<level[x].length;y++){
                level[x][y]=Tile.EMPTY;
            }
        }
        int x = 0;
        int y = 29;
        for (int i = 0; i < levelstr.length(); i++) {
            switch (levelstr.charAt(i)) {
                case '*':
                    level[x][y] = Tile.SPIKE;
                    break;
                case 'O':
                    level[x][y] = Tile.WALL;
                    break;
                case 'C':
                    level[x][y] = Tile.CHECKPOINT;
                    break;
                case '8':
                    level[x][y] = Tile.SLOW;
                    break;
                case 'S':
                    // start
                    originalStartX = x + 0.5f;
                    originalStartY = y + 0.5f;
                    break;
                case '^':
                    level[x][y] = Tile.UP;
                    break;
                case 'F':
                    level[x][y] = Tile.FINISH;
                    break;
                case '\n':
                    x = -1;
                    y--;
                    break;
            }
            x++;
        }
    }

    /**
     * reset original values
     */
    public void reset() {
        light = 0;
        startX = originalStartX;
        startY = originalStartY;
        bobX = startX;
        bobY = startY;
        bobXVel = 0f;
        bobYVel = 0f;
        startLeftDirection = false;
        leftDirection = false;
        jumpIntent = false;
        cameraX = 0;
        cameraY = 0;
        deathCooldown = 1;
        deathCount = 0;
        timeMulti = 1;
        win = false;
        dying = false;
    }

    /**
     * set input when active
     */

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                jumpIntent = true;
                return false;// pass event to parent's touchDown
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                jumpIntent = false;
                return false;// pass event to input's touchUp
            }

            @Override
            public boolean keyDown(int keycode) {
                jumpIntent = true;
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                jumpIntent = false;
                return false;
            }
        }, parent.inAd));
    }

    /**
     * render to screen
     *
     * @param delta time in seconds since last render
     */
    @Override
    public void render(float delta) {
        if (slowTime > 0) {// if should slow down
            timeMulti += (0.25f - timeMulti) * delta * 4;// let time flow a bit slower if it is too fast
            slowTime -= delta;// decease the slowdown duration
            if (slowTime < 0) {
                slowTime = 0;
            }
        } else {
            timeMulti += (1 - timeMulti) * delta;// let the time flow faster if it is slow
        }
        delta *= timeMulti;// modify delta time
        gameStep(Math.min(delta, 0.1f));// game stuff
        Gdx.gl.glClearColor(0, 0, 0, 1);// clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        parent.camera.update();// update cameras
        parent.textCamera.update();
        batch.setProjectionMatrix(parent.camera.combined);// activate normal camera
        batch.begin();// activate batch
        float goalCameraX = bobX + bobXVel * 0.6f - 6.5f;// where should the camera look at
        float goalCameraY = bobY + bobYVel * 0.1f - 4;
        cameraX += (goalCameraX - cameraX) * delta * 6;// follow camera's goal
        cameraY += (goalCameraY - cameraY) * delta * 6;
        if (cameraY < 0) {// don't let the camera go under the level's border
            cameraY = 0;
        }
        int minDrawX = Math.max((int) (cameraX), 0);// which tiles to draw
        int maxDrawX = Math.min((int) (cameraX + 13), level.length - 1);
        int minDrawY = Math.max((int) (cameraY), 0);
        int maxDrawY = Math.min((int) (cameraY + 8), level[0].length - 1);
        for (int tilex = minDrawX; tilex <= maxDrawX; tilex++) {// for every drawn tile
            for (int tiley = minDrawY; tiley <= maxDrawY; tiley++) {
                if (level[tilex][tiley] != Tile.EMPTY) {// if not air, draw
                    float alpha = 1;// calculate alpha
                    if (tilex - cameraX > 11) {
                        alpha *= Math.max(1 - (tilex - cameraX - 11) / 2f, 0);
                    } else if (tilex - cameraX < 1) {
                        alpha *= Math.max(1 - (1 - tilex + cameraX) / 2f, 0);
                    }
                    if (tiley - cameraY > 7) {
                        alpha *= Math.max(8 - tiley + cameraY, 0);
                    } else if (tiley - cameraY < 0) {
                        alpha *= Math.max(1 + tiley - cameraY, 0);
                    }
                    batch.setColor(1, 1, 1, alpha);
                    switch (level[tilex][tiley]) {// draw tile
                        case WALL:
                            batch.draw(wall, tilex - cameraX, tiley - cameraY, 1, 1);
                            break;
                        case SPIKE:
                            batch.draw(spike, tilex - cameraX, tiley - cameraY, 1, 1);
                            break;
                        case CHECKPOINT:
                            batch.setColor(1, 1, 1, alpha / 2f);
                            batch.draw(checkpoint, tilex - cameraX, tiley - cameraY, 1, 1);
                            batch.setColor(1, 1, 1, alpha);
                            break;
                        case CHECKPOINT_ACTIVE:
                            batch.draw(checkpoint, tilex - cameraX, tiley - cameraY, 1, 1);
                            break;
                        case SLOW:
                            if (!impossible) {// if impossible, there are nop slowers
                                batch.draw(slower, tilex - cameraX, tiley - cameraY, 1, 1);
                            }
                            break;
                        case UP:
                            batch.draw(jumper, tilex - cameraX, tiley - cameraY, 1, 1);
                            break;
                        case FINISH:
                            batch.draw(finish, tilex - cameraX, tiley - cameraY, 1, 1);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (dying) {
            if (deathCooldown < 0.5f) {// draw popping ball
                batch.setColor(1, 1, 1, 1 - 2 * deathCooldown);
                float size = deathCooldown;
                batch.draw(ball, bobX - 0.5f - cameraX - size, bobY - 0.5f - cameraY - size, 1 + 2 * size,
                        1 + 2 * size);
                batch.setColor(1, 1, 1, 1);
            }
        } else {// draw normal ball
            batch.setColor(1, 1, 1, 1);
            batch.draw(ball, bobX - 0.5f - cameraX, bobY - 0.5f - cameraY, 1, 1);
        }
        batch.setProjectionMatrix(parent.textCamera.combined);// change to text rendering
        parent.font.draw(batch, "Deaths: " + deathCount, 10, 150);// draw death count
        batch.end();// draw everything
    }

    /**
     * te game step
     *
     * @param delta elapsed time since last call in seconds
     */
    private void gameStep(float delta) {
        if (win) {// if player won, show the win screen and tell it if the game impossible was
            parent.showWin(impossible);
            return;
        }
        if (gameOver) {// if game is over
            gameOver = false;
            dying = true;// set death cooldown
            deathCooldown = 0;
            deathCount++;
            bobYVel = 0;
            bobXVel = 0;
            slowTime = 0;
        }
        if (dying) {
            if (deathCooldown < 1) {
                deathCooldown += delta;
            } else {// if end of dying
                dying = false;
                deathCooldown = 1;
                bobY = startY;// reset position
                bobX = startX;
                leftDirection = startLeftDirection;
                if (impossible) {// if impossible, reset checkpoints
                    for (int x = 0; x < 300; x++) {
                        for (int y = 0; y < 30; y++) {
                            if (level[x][y] == Tile.CHECKPOINT_ACTIVE) {
                                level[x][y] = Tile.CHECKPOINT;
                            }
                        }
                    }
                }
            }
            return;// do not run game step
        }
        canJump = false;
        bobYVel -= 100f * delta;// gravity
        if (leftDirection) {// horizontal acceleration to 8 tile/sec
            if (bobXVel > -8) {
                bobXVel -= 10f * delta;
                if (bobXVel < -8) {
                    bobXVel = -8;
                }
            }
        } else {
            if (bobXVel < 8) {
                bobXVel += 10f * delta;
                if (bobXVel > 8) {
                    bobXVel = 8;
                }
            }
        }
        float bobNewX = bobX + bobXVel * delta;// calculate new position
        float bobNewY = bobY + bobYVel * delta;
        int minInteractX = Math.max((int) (bobNewX - 1), 0);// calculate tiles which the ball interact with
        int maxInteractX = Math.min((int) (bobNewX + 1), level.length - 1);
        int minInteractY = Math.max((int) (bobNewY - 1), 0);
        int maxInteractY = Math.min((int) (bobNewY + 1), level[0].length - 1);
        for (int interactX = minInteractX; interactX <= maxInteractX; interactX++) {// for every tile
            for (int interactY = minInteractY; interactY <= maxInteractY; interactY++) {
                float deltaX = bobNewX - interactX - 0.5f;
                float deltaY = bobNewY - interactY - 0.5f;
                switch (level[interactX][interactY]) {
                    case WALL:
                        if (bobY - interactY >= 1.3f) {// if the ball was above
                            if (deltaX <= 0.5f && deltaX >= -0.5f) {
                                if (deltaY < 1) {// keep the ball above the tile
                                    bobNewY = interactY + 1.5f;
                                    bobYVel = Math.max(0, bobYVel);
                                    canJump = true;// can jump from this
                                }
                            } else if (deltaX < -0.5f) {// let the ball roll onto the tile
                                float dx2 = deltaX + 0.5f;
                                float minY = 0.5f + (float) Math.sqrt(Math.max(0, 0.25f - dx2 * dx2));
                                if (deltaY < minY) {
                                    bobNewY = interactY + 0.5f + minY;
                                    bobYVel = Math.max(1.5f, bobYVel);
                                    canJump = true;
                                }
                            } else if (deltaX > 0.5f) {// let the ball roll down from the tile
                                float dx2 = deltaX - 0.5f;
                                float minY = 0.5f + (float) Math.sqrt(Math.max(0, 0.25f - dx2 * dx2));
                                if (deltaY < minY) {
                                    bobNewY = interactY + 0.5f + minY;
                                    bobYVel = Math.max(0, bobYVel);
                                    canJump = true;
                                }
                            }
                        } else if (bobY - interactY <= -0.2f) {// if the ball was under the tile
                            if (deltaX <= 0.5f && deltaX >= -0.5f) {// keep it under the tile
                                if (deltaY > -1) {
                                    bobNewY = interactY - 0.5f;
                                    bobYVel = Math.min(0, bobYVel);
                                }
                            } else if (deltaX < -0.5f) {// roll stuff
                                float dx2 = deltaX - 0.5f;
                                float minY = -0.5f - (float) Math.sqrt(Math.max(0, 0.25f - dx2 * dx2));
                                if (deltaY > minY) {
                                    bobNewY = interactY + 0.5f + minY;
                                    bobYVel = Math.min(-1.5f, bobYVel);
                                }
                            } else if (deltaX > 0.5f) {// roll stuff
                                float dx2 = deltaX - 0.5f;
                                float minY = -0.5f - (float) Math.sqrt(Math.max(0, 0.25f - dx2 * dx2));
                                if (deltaY > minY) {
                                    bobNewY = interactY + 0.5f + minY;
                                }
                            }
                        } else {// frontal collision
                            if (deltaX < 0.5f && deltaX > -0.5f) {// if the ball collides
                                gameOver = true;// die
                            }
                        }
                        break;
                    case SPIKE:
                        if (deltaX * deltaX + deltaY * deltaY < 0.8f) {// if ball is in radius
                            gameOver = true;// die
                        }
                        break;
                    case CHECKPOINT:
                        if (deltaX * deltaX + deltaY * deltaY < 0.8f) {// if ball is in radius
                            leftDirection = !leftDirection;// change direction
                            level[interactX][interactY] = Tile.CHECKPOINT_ACTIVE;// activate
                            if (!impossible) {// if checkpoints work
                                startLeftDirection = leftDirection;// set respawn
                                startX = interactX + 0.5f;
                                startY = interactY + 0.5f;
                            }
                        }
                        break;
                    case SLOW:
                        if (!impossible) {
                            if (deltaX * deltaX + deltaY * deltaY < 0.8f) {
                                slowTime = 20;// set slowdown effect to 20 sec
                            }
                        }
                        break;
                    case UP:
                        if (deltaX * deltaX + deltaY * deltaY < 0.8f) {// if in radius
                            canJump = true;// can jump
                        }
                        break;
                    case FINISH:
                        if (deltaX * deltaX + deltaY * deltaY < 0.8f) {
                            win = true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (jumpIntent && canJump) {// if jump is successful
            bobYVel = 22;
        }
        bobX = bobNewX;// finalize movement
        bobY = bobNewY;
        if (bobY < -5) {// if the ball fell out of the level, the game ends
            gameOver = true;
        }
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
        batch.dispose();// delete resources
        sprites.dispose();
    }
}
