package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.utils.TimeUtils;

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

        TimeUtils.executeIn(STALL_TIME, new Runnable() {
            @Override
            public void run() {
                //TODO Show laser
                //TODO This requires rotation and stuff...

                p.unfreeze();
                p.onFire(); //Indicate this player is done firing
                p.setCanFire(true);
            }
        });
    }
}
