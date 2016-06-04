package com.boxtrotstudio.ghost.common.game;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;

public class TutorialBot extends BasePlayableEntity {

    public TutorialBot() {
        setName("Alem");
    }

    public void fire(float targetX, float targetY){
        useAbility(targetX, targetY, 0);
    }

    @Override
    public boolean isReady() {
        return true; //The bot is always ready
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
    public TemporaryStats getCurrentMatchStats() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
    }
}
