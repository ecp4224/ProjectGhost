package me.eddiep.ghost.server.game.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.TypeableEntity;

import java.io.IOException;

public class Bullet extends Entity implements TypeableEntity {

    public Bullet(Player parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BULLET");

        register("onHit");
        register("despawn");
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
                System.out.println("Kill");
                try {
                    getMatch().despawnEntity(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.tick();

        if (position.x < Match.MAP_XMIN ||
            position.x > Match.MAP_XMAX ||
            position.y < Match.MAP_YMIN ||
            position.y > Match.MAP_YMAX) {
            try {
                getMatch().despawnEntity(this);
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
