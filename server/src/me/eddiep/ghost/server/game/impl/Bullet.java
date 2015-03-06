package me.eddiep.ghost.server.game.impl;

import me.eddiep.ghost.server.game.Entity;

public class Bullet extends Entity {

    public Bullet(Player parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        getMatch().setID(this);

        register("onHit");
        register("despawn");
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        Player toHit = ((Player)getParent()).getOpponent();
        if (isInside(toHit.getX() - (Player.WIDTH / 2),
                    toHit.getY() - (Player.HEIGHT / 2),
                    toHit.getX() + (Player.WIDTH / 2),
                    toHit.getY() + (Player.HEIGHT / 2))) {
            System.out.println("Kill");
        }
    }
}
