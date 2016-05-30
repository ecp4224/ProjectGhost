package com.boxtrotstudio.ghost.common.game;

import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.Text;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.TimeUtils;
import com.boxtrotstudio.ghost.game.match.item.SpeedItem;

public class TutorialMatch extends NetworkMatch {

    boolean isReady, didMove, didFire, hitOnce, spawnItem;
    float startPosX;
    float startPosY;
    PlayableEntity player;
    TutorialBot bot;
    SpeedItem speedItem;
    Text text;

    long setupTime;


    public TutorialMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
        player = team1.getTeamMembers()[0];
        bot = (TutorialBot) team2.getTeamMembers()[0];
        bot.setCurrentAbility(Gun.class);
    }

    @Override
    public void setup() {
        setWorld(new NetworkWorld("tutorial", this)); //set the world to the tutorial level

        super.setup();

        setActive(false, "Hello and welcome to Project Ghost", false);
        useCountdown = false;

        startPosX = player.getX();
        startPosY = player.getY();
        disableItems(); //Don't auto spawn items
    }

    @Override
    public void onMatchEnded(){
        super.onMatchEnded();
    }

    @Override
    public void end(Team winners) {
        super.end(winners);
        TimeUtils.executeInSync(2000, new Runnable() {
            @Override
            public void run() {
                setActive(false, "Excellent work! You'll be a natural in no time.");
            }
        }, world);

    }

    @Override
    public void tick() {
        if(player.getLives() == 0){
            player.setLives((byte) 1);
        }

        super.tick();

        /*
        Here we'll check if the match is active and we haven't displayed this message yet.
        The match will only ever become active when the player ready's up, so it has the same effect as the
        if statement above (commented out).
        However, we want LiveMatchImpl to start the match naturally, so we simply only check if the match has already
        started and change the status message.
         */
        if (isMatchActive() && !isReady) {
            player.triggerEvent(Event.TutorialStart, 0);
            isReady = true;
        }

        if ((player.getX() < startPosX - 300 || player.getX() > startPosX + 300 || player.getY() < startPosY - 300 || player.getY() > startPosY + 300) && !didMove) {

            player.triggerEvent(Event.DidMove, 0);
            didMove = true;
            player.setCanFire(true);
        }

        if(player.didFire() && !didFire){
            if(bot.getLives() < 3){
                bot.setLives((byte) 3);
            }
            bot.fire(player.getX(), player.getY());

            player.triggerEvent(Event.DidFire, 0);
            TimeUtils.executeIn(500, new Runnable() {
                @Override
                public void run() {
                    /*We freeze the player here rather than set the match to inactive because
                      setting the match to inactive will cause the timeline to pause, so everything
                      that will happen in the next 2 seconds won't get recorded if the match is inactive.
                      Freezing the player has the same effect as it prevents the player from moving, but
                      it also keeps the timeline recording
                    */
                    player.freeze();
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.unfreeze();
                    setActive(true, "");
                }
            });
            didFire = true;
        }

        if(bot.getLives() == 2 && !hitOnce){
            speedItem = new SpeedItem(TutorialMatch.this);
            setActive(true, "Nice shot! \nYou'll need to land two more hits to win.");
            TimeUtils.executeInSync(3000, new Runnable() {
                @Override
                public void run() {
                    spawnItem(speedItem);
                    setActive(true, "This may give you a little boost. \nBe wary though, as picking things up can blow your cover.");
                }
            }, super.world);
           hitOnce = true;
        }

        if(speedItem != null && speedItem.isActive() && !spawnItem){
           setActive(true, "Now, bring it on home!");
           spawnItem = true;
        }
    }
}
