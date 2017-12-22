package com.boxtrotstudio.ghost.game.match.abilities;

        import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class JammedGun extends PlayerAbility {
    private static final long BASE_COOLDOWN = 555;
    private PlayableEntity p;

    public JammedGun(PlayableEntity p) {
        super(p);
        baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    protected void onUsePrimary(float targetX, float targetY) {
        final PlayableEntity p = owner();
        p.onFire(); //Indicate this player is done firing

        endPrimary();
    }

    @Override
    protected void onUseSecondary(float targetX, float targetY) {
        final PlayableEntity p = owner();
        p.onFire(); //Indicate this player is done firing

        endPrimary();
    }

    @Override
    public byte id() {
        return 5;
    }
}
