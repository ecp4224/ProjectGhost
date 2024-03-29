package com.boxtrotstudio.ghost.game.match.abilities;

        import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class JammedGun implements Ability<PlayableEntity> {
    private static final long BASE_COOLDOWN = 555;
    private PlayableEntity p;

    public JammedGun(PlayableEntity p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY) {
        final PlayableEntity p = owner();
        p.setCanFire(false);
        p.onFire(); //Indicate this player is done firing

        long wait = p.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
        TimeUtils.executeInSync(wait, () -> p.setCanFire(true), p.getWorld());
    }

    @Override
    public byte id() {
        return 5;
    }
}
