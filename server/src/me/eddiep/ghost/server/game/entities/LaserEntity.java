package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.utils.TimeUtils;

import java.io.IOException;

public class LaserEntity extends Entity implements TypeableEntity {
    private Playable parent;
    public LaserEntity(Playable parent) {
        super();
        setParent(parent.getEntity());
        setMatch(parent.getMatch());
        setVisible(true);
        setName("LAZERS");
        this.parent = parent;
    }

    @Override
    public void tick() {
        super.tick();

        if (check) {
            float currentWidth = TimeUtils.ease(0f, 1040f, 300f, System.currentTimeMillis() - start);

            float bx = (float) (parent.getEntity().getX() + Math.cos(rotation) * currentWidth);
            float by = (float) (parent.getEntity().getY() + Math.sin(rotation) * currentWidth);

            
        }
    }

    @Override
    public void updateState() throws IOException {
        Playable[] temp = parent.getOpponents();
        for (Playable p : temp) {
            updateStateFor(p);
        }

        temp = parent.getAllies();
        for (Playable p : temp) {
            updateStateFor(p);
        }
    }

    @Override
    public byte getType() {
        return 3;
    }

    private boolean check;
    private long start;
    public void startChecking() {
        check = true;
        start = System.currentTimeMillis();
    }
}
