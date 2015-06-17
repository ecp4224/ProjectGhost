package me.eddiep.ghost.server.game.entities;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;

import java.io.IOException;

public class Bullet extends Entity implements TypeableEntity {
    private Player parent;
    public Bullet(Player parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BULLET");
        this.parent = parent;
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        Player[] opponents = parent.getOpponents();
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

                if (toHit.visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    toHit.visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }

                toHit.lastHit = System.currentTimeMillis();
                toHit.hatTrickCount = 0; //If you get hit, then reset hit hatTrickCount

                try {
                    getMatch().despawnEntity(this);
                    parent.shotsHit++;
                    parent.hatTrickCount++;
                    if (parent.hatTrickCount > 0 && parent.hatTrickCount % 3 == 0) { //If the shooter's hatTrickCount is a multiple of 3
                        parent.hatTricks++; //They got a hat trick
                    }
                    if (toHit.isDead()) {
                        parent.playersKilled.add(toHit.getPlayerID());
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
