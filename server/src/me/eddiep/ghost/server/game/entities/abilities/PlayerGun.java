package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.Bullet;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.util.Vector2f;

import java.io.IOException;

public class PlayerGun implements Ability<Playable> {
    private static final float BULLET_SPEED = 12f;
    private Playable p;

    public PlayerGun(Playable p) {
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
    public void use(float targetX, float targetY) {
        Playable p = owner();

        float x = p.getEntity().getX();
        float y = p.getEntity().getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        Bullet b = new Bullet(p);
        b.setPosition(p.getEntity().getPosition().cloneVector());
        b.setVelocity(velocity);

        try {
            p.getMatch().spawnEntity(b);
            p.onFire();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
