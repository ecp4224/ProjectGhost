package com.boxtrotstudio.ghost.common.game.gamemodes.tutorial;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class TutorialBot extends BasePlayableEntity {

    boolean isReady = false;

    public TutorialBot() {
        setName("Alem");
        TimeUtils.executeIn(5000, new Runnable() {
            @Override
            public void run() {
                isReady = true;
            }
        });
    }

    public void fire(float targetX, float targetY){
        useAbility(targetX, targetY, 0);
    }

    @Override
    public boolean isReady() {
        return isReady; //The bot is always ready
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
