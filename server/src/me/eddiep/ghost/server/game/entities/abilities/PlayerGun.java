package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.Bullet;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.util.Vector2f;

import java.io.IOException;

public class PlayerGun extends PlayerAbility {
    private static final float BULLET_SPEED = 12f;

    public PlayerGun(Player p) {
        super(p);
    }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    public void use(float targetX, float targetY) {
        Player p = owner();

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        Bullet b = new Bullet(p);
        b.setPosition(p.getPosition().cloneVector());
        b.setVelocity(velocity);

        try {
            p.getMatch().spawnEntity(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
