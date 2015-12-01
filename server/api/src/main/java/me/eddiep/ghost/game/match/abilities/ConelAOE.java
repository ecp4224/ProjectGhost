package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.ConelEntity;
import me.eddiep.ghost.game.match.stats.BuffType;

public class ConelAOE implements Ability<PlayableEntity> {
    private PlayableEntity p;

    public ConelAOE(PlayableEntity playableEntity) {
        this.p = playableEntity;
    }

    @Override
    public String name() {
        return "ConelAOE"; //TODO Pick a better name
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int actionRequested) {
        final float x = p.getX();
        final float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        final float direction = (float) Math.atan2(asdy, asdx);

        ConelEntity entity = new ConelEntity(p, direction);
        entity.setPosition(p.getPosition());
        p.getWorld().spawnEntity(entity);
    }
}
