package lando.systems.ld41.gameobjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.ld41.LudumDare41;
import lando.systems.ld41.screens.GameScreen;
import lando.systems.ld41.utils.Assets;

public class Ball {

    public boolean onTank;
    public Vector2 position;
    private Vector2 oldPosition;
    private Vector2 newPosition;
    private Vector2 collisionPoint;
    private Vector2 normal;
    private Vector2 tempVector;
    private Vector3 tempVector3;
    public Vector2 velocity;
    public float radius;
    public float pickupDelay;
    private GameScreen screen;

    private ShapeRenderer shapeRenderer;
    private int radiusCount = 0;

    private float indicatorRadius = 0f;
    private Interpolation indicatorInterp;
    private float accum;

    public TextureRegion image;

    public Ball(GameScreen screen, String ballImage){
        this.screen = screen;
        setImage(ballImage);

        onTank = true;
        position = new Vector2();
        velocity = new Vector2();

        shapeRenderer = new ShapeRenderer();

        oldPosition = new Vector2();
        newPosition = new Vector2();
        collisionPoint = new Vector2();
        normal = new Vector2();
        tempVector = new Vector2();
        tempVector3 = new Vector3();

        radius = 5;
        indicatorInterp = Interpolation.pow2;
        accum = 0;
    }

    public void setImage(String ballImage) {
        this.image = Assets.getImage(ballImage);
    }

    public void update(float dt){
        if (onTank) return;
        pickupDelay = Math.max(pickupDelay - dt, 0);

        oldPosition.set(position);
        newPosition.set(position);
        newPosition.add(velocity.x * dt, velocity.y * dt);

        Level.CollisionType collision = screen.level.checkCollision(oldPosition, newPosition, radius, collisionPoint, normal);
        if (collision != Level.CollisionType.None || checkCollisionWithEnemies()){
            if (collision == Level.CollisionType.Bumper) velocity.scl(1.3f);
            float currentSpeed = velocity.len();
            tempVector.set(velocity);
            // r=d−2(d⋅n)n
            float dot = 2f * tempVector.dot(normal);
            tempVector.sub(dot * normal.x, dot * normal.y);
            velocity.set(tempVector).nor().scl(currentSpeed);
            newPosition.set(collisionPoint);
        }

        if (isNotMoving())
        {
            accum += dt;
            indicatorRadius = 10 + 10 * indicatorInterp.apply((accum/.5f)%1f);
        }

        position.set(newPosition);

        velocity.scl(.99f);
    }

    public boolean isInWorldView()
    {
        tempVector3.set(position.x, position.y, 0);
        screen.worldCamera.project(tempVector3);

        return (onTank || (tempVector3.x > 0 && tempVector3.x < screen.hudCamera.viewportWidth && tempVector3.y > 0 && tempVector3.y < screen.hudCamera.viewportHeight));
    }

    private boolean isNotMoving()
    {
        return !onTank && velocity.len() < 30;
    }

    public void render(SpriteBatch batch){
        if (onTank) return;

        batch.draw(image, position.x -radius, position.y-radius, radius*2, radius*2);

        if (isNotMoving())
        {
            batch.draw(LudumDare41.game.assets.indicator, position.x - indicatorRadius, position.y - indicatorRadius, indicatorRadius * 2f, indicatorRadius * 2f);
        }
    }

    public void shootBall(Vector2 position, Vector2 velocity){
        this.position.set(position);
        this.velocity.set(velocity);
        pickupDelay = 1f;
        onTank = false;
    }

    public void checkCollision(Tank tank){
        if (onTank || pickupDelay > 0) return;
        if (position.dst(tank.position) < radius + tank.radius){
            onTank = true;
        }
    }


    public boolean checkCollisionWithEnemies() {
        if (velocity.len() < 30) return false;
        for(Catapult catapult : screen.catapults) {
            if (catapult.alive && !catapult.killingIt && catapult.position.dst(newPosition) < catapult.radius + radius) {
                normal.set(newPosition);
                normal.sub(catapult.position);
                normal.nor();
                collisionPoint.set(catapult.position);
                normal.scl(catapult.radius + radius);
                collisionPoint.add(normal);
                normal.nor();
                catapult.kill();
                return true;
            }
        }

        for(EnemyTurret turret : screen.enemyTurrets) {
            if (turret.alive && !turret.killingIt && turret.position.dst(newPosition) < turret.radius + radius) {
                normal.set(newPosition);
                normal.sub(turret.position);
                normal.nor();
                collisionPoint.set(turret.position);
                normal.scl(turret.radius + radius);
                collisionPoint.add(normal);
                normal.nor();
                turret.kill();
                return true;
            }
        }

        for (EnemyTank tank : screen.enemyTanks){
            if (!tank.dead && tank.position.dst(newPosition) < tank.radius + radius ) {
                normal.set(newPosition);
                normal.sub(tank.position);
                normal.nor();
                collisionPoint.set(tank.position);
                normal.scl(tank.radius + radius);
                collisionPoint.add(normal);
                normal.nor();
                tank.kill();
                return true;
            }
        }
        return false;
    }
}
