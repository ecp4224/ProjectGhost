package me.eddiep.ghost.test.game;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.game.stats.TemporaryStats;
import me.eddiep.ghost.game.stats.TrackingMatchStats;

public class TutorialBot extends BasePlayableEntity {
    public void fire(float targetX, float targetY){
        useAbility(targetX, targetY, 0);
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
    public TrackingMatchStats getTrackingStats() {
        return null;
    }

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return null;
    }

    @Override
    public void tick(){
        super.tick();
    }
}
