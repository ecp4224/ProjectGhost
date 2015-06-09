package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.playable.Playable;

public class Circle implements Ability<Playable> {
    private Playable p;

    public Circle(Playable p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "circle";
    }

    @Override
    public Playable owner() {
        return null;
    }

    @Override
    public void use(float targetX, float targetY, int actionRequested) {

    }
}
