package lando.systems.ld41.gameobjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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


        if (screen.level.checkCollision(oldPosition, newPosition, radius, collisionPoint, normal) || checkCollisionWithCatapults()){
            float currentSpeed = velocity.len();
            tempVector.set(newPosition.x - oldPosition.x, newPosition.y - oldPosition.y);
            // r=d−2(d⋅n)n
            float dot = 2f * tempVector.dot(normal);
            tempVector.sub(dot * normal.x, dot * normal.y);
            velocity.set(tempVector).nor().scl(currentSpeed);
            newPosition.set(collisionPoint);
        }

        if (isNotMoving())
        {
            if (indicatorRadius == 30f)
            {
                indicatorRadius = 5f;
            } else {
                if (radiusCount == 0) {
                    indicatorRadius += 5f;
                }
                radiusCount = radiusCount == 10 ? 0 : radiusCount + 1;
            }
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
        return !onTank;
    }

    public void render(SpriteBatch batch){
        if (onTank) return;

        batch.draw(image, position.x -radius, position.y-radius, radius*2, radius*2);

        if (isNotMoving())
        {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(position.x, position.y, indicatorRadius);
            shapeRenderer.end();
            batch.begin();
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

    public boolean checkCollisionWithCatapults() {
        for(Catapult catapult : screen.catapults) {
            if (catapult.position.dst(newPosition) < catapult.radius + radius) {
                normal.set(newPosition);
                normal.sub(catapult.position);
                normal.nor();
                collisionPoint.set(catapult.position);
                normal.scl(catapult.radius + radius);
                collisionPoint.add(normal);
                normal.nor();
                catapult.alive = false;
                return true;
            }
        }
        return false;
    }
}
