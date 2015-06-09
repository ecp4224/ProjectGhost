package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.Playable;

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

    public void damage() {

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
}
