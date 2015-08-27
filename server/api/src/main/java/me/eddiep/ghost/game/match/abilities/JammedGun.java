package me.eddiep.ghost.game.match.abilities;

        import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.utils.TimeUtils;

public class JammedGun implements Ability<PlayableEntity> {
    private static final long BASE_COOLDOWN = 315;
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
    public void use(float targetX, float targetY, int action) {
        final PlayableEntity p = owner();
        p.setCanFire(false);
        p.onFire(); //Indicate this player is done firing

        long wait = p.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
        TimeUtils.executeInSync(wait, new Runnable() {
            @Override
            public void run() {
                p.setCanFire(true);
            }
        }, p.getWorld());
    }
}
