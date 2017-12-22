package com.boxtrotstudio.ghost.common.game.gamemodes.tutorial;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class TutorialBot extends BasePlayableEntity {

    boolean isReady;
    boolean firstFire;
    PlayableEntity player;

    public TutorialBot(PlayableEntity player) {
        setName("Alem");
        this.player = player;
        TimeUtils.executeIn(5000, () -> isReady = true);
    }

    public void fire(float targetX, float targetY){
        useAbility(targetX, targetY, 0);
        firstFire = true;
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
        if (firstFire) {
            if (player.didFire()) {
                fire(player.getX(), player.getY());
            }
        }

        super.tick();
    }
}
