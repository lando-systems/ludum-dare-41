package lando.systems.ld41.gameobjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld41.LudumDare41;
import lando.systems.ld41.screens.GameScreen;


public class Catapult extends GameObject {
    public final float FIRE_RATE = 4;
    public final float TURRET_WIDTH = 50;
    public final float TURRET_HEIGHT = 50;

    public Array<Bullet> activeBullets = new Array<Bullet>();

    public Vector2 position;
    public Vector2 directionVector;
    public float rotation;
    public Bullet newBullet;
    public Tank playerTank;
    public boolean alive = true;
    private TextureRegion catapultFrame;
    private float timer = 0;
    private GameScreen screen;

    public Catapult(GameScreen screen, Tank playerTank, Vector2 startPosition){
        this.screen = screen;
        this.position = startPosition;
        this.playerTank = playerTank;
        directionVector = new Vector2();
        catapultFrame = LudumDare41.game.assets.catapultAnimation.getKeyFrame(0);

    }
    @Override
    public void update(float dt){
        timer+=dt;
        rotation = (float)(Math.atan2(
                playerTank.position.y - position.y,
                playerTank.position.x - position.x) * 180 / Math.PI);
        if (playerTank.isFirstBallFired && alive && timer > FIRE_RATE) {
            fireBullet();
            timer = 0;
        }
        if (timer > 2) {
            catapultFrame = LudumDare41.game.assets.catapultAnimation.getKeyFrame(0);
        }
        else if (timer > 1) {
            catapultFrame = LudumDare41.game.assets.catapultAnimation.getKeyFrame(1);
        }
        else {
            catapultFrame = LudumDare41.game.assets.catapultAnimation.getKeyFrame(2);
        }
        for (Bullet bullet : activeBullets) {
            if (bullet.alive) {
                bullet.update(dt);
            } else {
                activeBullets.removeValue(bullet, true);
            }
        }
    }
    private void fireBullet() {
        newBullet = new Bullet(screen, playerTank, position.x, position.y, playerTank.position.x, playerTank.position.y);
        activeBullets.add(newBullet);
    }
    @Override
    public void render(SpriteBatch batch){
        batch.draw(catapultFrame, position.x, position.y, TURRET_WIDTH/2, TURRET_HEIGHT/2 , TURRET_WIDTH, TURRET_HEIGHT, 1, 1, rotation - 90);
        for (Bullet bullet : activeBullets) {
            bullet.render(batch);
        }
    }
}
