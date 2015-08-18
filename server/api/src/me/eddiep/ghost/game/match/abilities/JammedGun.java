package me.eddiep.ghost.game.match.abilities;

        import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class JammedGun implements Ability<PlayableEntity> {
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
        PlayableEntity p = owner();
        p.onFire(); //Indicate this player is done firing
    }
}
