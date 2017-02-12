package com.boxtrotstudio.ghost.common.game.gamemodes.tutorial;

import com.boxtrotstudio.ghost.common.game.gamemodes.StagedMatch;
import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.item.SpeedItem;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class TutorialMatch extends StagedMatch {
    float startPosX;
    float startPosY;
    TutorialBot bot;
    private SpeedItem speedItem;

    public TutorialMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);

        bot = (TutorialBot) team2.getTeamMembers()[0];
        bot._packet_setCurrentAbility(Gun.class);
    }

    @Override
    public void setup() {
        super.setup();

        setActive(false, "Hello and welcome to Project Ghost", false);
        useCountdown = false;

        startPosX = getPlayer().getX();
        startPosY = getPlayer().getY();
        disableItems(); //Don't auto spawn items
    }

    @Override
    protected void stage() {
        PlayableEntity p = getPlayer();

        //Setup the match
        p.triggerEvent(Event.TutorialStart, 0);

        waitFor(player -> (player.getX() < startPosX - 300 || player.getX() > startPosX + 300 ||
                player.getY() < startPosY - 300 || player.getY() > startPosY + 300));

        p.triggerEvent(Event.DidMove, 0);
        p.setCanFire(true);

        waitFor(player -> player.didFire());

        if(bot.getLives() < 3){
            bot.setLives((byte) 3);
        }
        bot.fire(p.getX(), p.getY());

        p.triggerEvent(Event.DidFire, 0);

        waitFor(player -> bot.getLives() == 2);

        speedItem = new SpeedItem(this);

        p.triggerEvent(Event.HitOnce, 0);
        TimeUtils.executeInSync(3000, () -> {
            spawnItem(speedItem);
            p.triggerEvent(Event.SpawnSpeed, 0);
        }, super.world);

        when(p, player -> player.getInventory().hasItem(0)).execute(player -> player.triggerEvent(Event.ObtainSpeed, 0));
        when(bot, b -> b.getLives() == 1).execute(b -> p.triggerEvent(Event.HitTwice, 0));
    }

    @Override
    public void end(Team winners) {
        super.end(winners);
        TimeUtils.executeInSync(2000, () -> setActive(false, "Excellent work! You'll be a natural in no time."), world);

    }
}
