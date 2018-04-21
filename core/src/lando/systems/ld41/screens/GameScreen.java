package lando.systems.ld41.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld41.LudumDare41;
import lando.systems.ld41.gameobjects.Level;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld41.gameobjects.Tank;
import lando.systems.ld41.ui.PowerMeter;

/**
 * Created by Brian Ploeckelman <brian.ploeckelman@wisc.edu> on 4/13/18.
 */
public class GameScreen extends BaseScreen {

    public Tank playerTank;
    public Level level;
    public PowerMeter powerMeter;
    public boolean showPowerMeter;

    public GameScreen() {
        Gdx.input.setInputProcessor(this);
        playerTank = new Tank(this);
        level = new Level("maps/test.tmx");
        showPowerMeter = false;
        powerMeter = new PowerMeter(2.5f, new Vector2(Gdx.graphics.getWidth() - 60, Gdx.graphics.getHeight() - 110));
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            game.screen = new TitleScreen();
        }

        if (Gdx.input.isButtonPressed(0))
        {
            showPowerMeter = true;
        }
        else
        {
            showPowerMeter = false;
            powerMeter.reset();
        }

        if (showPowerMeter) {
            powerMeter.update(dt);
        }

        playerTank.update(dt);

        cameraTargetPos.set(playerTank.position, 0f);
        updateCamera();
    }

    @Override
    public void render(SpriteBatch batch) {
        renderGame(batch);
        renderUI(batch);
    }

    private void renderGame(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        level.render(batch, worldCamera);

        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            batch.setColor(Color.WHITE);
            playerTank.render(batch);
        }
        batch.end();
    }

    private void renderUI(SpriteBatch batch) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            batch.setColor(Color.WHITE);
            if (showPowerMeter)
            {
                powerMeter.render(batch);
            }
//            Assets.drawString(batch, "Game Screen", 10f, hudCamera.viewportHeight - 20f, Color.CORAL, 1.25f, game.assets.font);
        }
        batch.end();
    }

}
