package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.LaserEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.utils.TimeUtils;

import java.io.IOException;

public class Laser implements Ability<Playable> {
    private static final long STALL_TIME = 900L;
    private Playable p;

    public Laser(Playable p) {
        this.p = p;
    }
    @Override
    public String name() {
        return "laser";
    }

    @Override
    public Playable owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
        p.freeze(); //Freeze the player
        p.getEntity().setVisible(true);
        p.setCanFire(false);


        final LaserEntity entity = new LaserEntity(p);
        entity.setVisible(false);
        entity.setPosition(p.getEntity().getPosition());

        float x = p.getEntity().getX();
        float y = p.getEntity().getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        entity.setRotation(inv);

        try {
            p.getMatch().spawnEntity(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimeUtils.executeIn(STALL_TIME, new Runnable() {
            @Override
            public void run() {
                entity.setVisible(true);

                p.unfreeze();
                p.onFire(); //Indicate this player is done firing
                p.setCanFire(true);
            }
        });
    }
}
