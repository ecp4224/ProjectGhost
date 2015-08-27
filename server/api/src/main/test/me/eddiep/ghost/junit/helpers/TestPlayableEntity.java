package me.eddiep.ghost.junit.helpers;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.game.stats.TemporaryStats;
import me.eddiep.ghost.game.stats.TrackingMatchStats;

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
    public void onShotMissed() {

    }

    @Override
    public void onStatUpdate(Stat stat) {

    }

    @Override
    public TrackingMatchStats getTrackingStats() {
        return null;
    }

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return null;
    }
}
