package me.eddiep.ghost.common.game.bots;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.game.match.stats.TemporaryStats;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

public class TestPlayableEntity extends BasePlayableEntity {

    public TestPlayableEntity(String name) {
        setName(name);
    }

    @Override
    public void onWin(Match match) {

    }

    @Override
    public void onLose(Match match) {

    }

    @Override
    public void onDamagePlayable(PlayableEntity hit) {
    }

    @Override
    public void onKilledPlayable(PlayableEntity killed) {

    }

    @Override
    public void onStatUpdate(Stat stat) {

    }

    @Override
    public void tick() {
        if (getTarget() == null) {
            float x = Global.random(100, 1000);
            float y = Global.random(100, 700);
            setTarget(new Vector2f(x, y));
        }

        if (Global.RANDOM.nextDouble() < 0.2 && canFire) {
            float x = Global.random(100, 1000);
            float y = Global.random(100, 700);
            useAbility(x, y, 1);
        }

        super.tick();
    }

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return null;
    }
}
