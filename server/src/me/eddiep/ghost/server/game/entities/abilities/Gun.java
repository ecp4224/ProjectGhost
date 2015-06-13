package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.BulletEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.util.Vector2f;

import java.io.IOException;

public class Gun implements Ability<Playable> {
    private static final float BULLET_SPEED = 16f;
    private Playable p;

    public Gun(Playable p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    public Playable owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
        Playable p = owner();

        float x = p.getEntity().getX();
        float y = p.getEntity().getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        BulletEntity b = new BulletEntity(p);
        b.setPosition(p.getEntity().getPosition().cloneVector());
        b.setVelocity(velocity);

        try {
            p.getMatch().spawnEntity(b);
            p.onFire(); //Indicate this player is done firing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
