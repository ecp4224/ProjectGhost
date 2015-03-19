package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;

import java.io.IOException;

public class Bullet extends Entity implements TypeableEntity {

    public Bullet(Player parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BULLET");
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        Player[] opponents = ((Player)getParent()).getOpponents();
        for (Player toHit : opponents) {
            if (isInside(toHit.getX() - (Player.WIDTH / 2f),
                    toHit.getY() - (Player.HEIGHT / 2f),
                    toHit.getX() + (Player.WIDTH / 2f),
                    toHit.getY() + (Player.HEIGHT / 2f))) {

                toHit.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }
                toHit.wasHit = true;
                toHit.lastHit = System.currentTimeMillis();

                try {
                    getMatch().despawnEntity(this);
                    ((Player)getParent()).shotsHit++;
                    if (toHit.isDead()) {
                        ((Player)getParent()).playersKilled.add(toHit.getPlayerID());
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
                ((Player)getParent()).shotsMissed++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateState() throws IOException {
        Player[] temp = ((Player)getParent()).getOpponents();
        for (Player p : temp) {
            updateStateFor(p);
        }

        temp = ((Player)getParent()).getAllies();
        for (Player p : temp) {
            updateStateFor(p);
        }
    }

    @Override
    public byte getType() {
        return 2;
    }
}
