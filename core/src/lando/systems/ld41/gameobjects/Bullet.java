package lando.systems.ld41.gameobjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld41.LudumDare41;

public class Bullet {

    public float bulletSpeed;
    public Vector2 velocity;
    public float radius;
    public Vector2 position;
    public GameObject owner;
    public boolean alive = true;
    public TextureRegion texture;

    public Bullet() {
        position = new Vector2();
        velocity = new Vector2();

    }

    public void init(Vector2 pos, Vector2 dir, GameObject owner, TextureRegion tex){
        position.set(pos);
        velocity.set(dir);
        velocity.nor();
        this.bulletSpeed = owner.bulletSpeed;
        this.owner = owner;
        this.texture = tex;
        this.radius = owner.bulletSize;
        alive = true;
    }

    public void update(float dt) {
        position.add(velocity.x * bulletSpeed * dt, velocity.y * bulletSpeed * dt);
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, position.x - radius, position.y - radius, radius, radius , radius*2f, radius*2f, 1, 1, 0);
    }

    public boolean checkCollision(Tank tank){
        if (position.dst(tank.position) < radius + tank.radius){
            return true;
        } else {
            return false;
        }
    }
}

