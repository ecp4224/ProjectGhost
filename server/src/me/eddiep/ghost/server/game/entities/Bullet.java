package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;

import java.io.IOException;

public class Bullet extends Entity implements TypeableEntity {
    private Playable parent;
    public Bullet(Playable parent) {
        super();
        setParent(parent.getEntity());
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BULLET");
        this.parent = parent;
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        Playable[] opponents = parent.getOpponents();
        for (Playable p : opponents) {
            Entity toHit = p.getEntity();
            if (isInside(toHit.getX() - (Player.WIDTH / 2f),
                    toHit.getY() - (Player.HEIGHT / 2f),
                    toHit.getX() + (Player.WIDTH / 2f),
                    toHit.getY() + (Player.HEIGHT / 2f))) {

                p.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                p.onDamage(parent); //p was damaged by the parent

                try {
                    getMatch().despawnEntity(this);
                    parent.onDamagePlayable(p); //the parent damaged p
                    if (p.isDead()) {
                        parent.onKilledPlayable(p);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.tick();

        if (position.x < ActiveMatch.MAP_XMIN ||
            position.x > ActiveMatch.MAP_XMAX ||
            position.y < ActiveMatch.MAP_YMIN ||
            position.y > ActiveMatch.MAP_YMAX) {
            try {
                getMatch().despawnEntity(this);
                parent.onShotMissed();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        return 2;
    }
}
