package me.eddiep.ghost.test.game;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.item.SpeedItem;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

public class TutorialMatch extends NetworkMatch {

    boolean isReady, didMove, didFire, hitOnce, spawnItem;
    float startPosX;
    float startPosY;
    PlayableEntity player;
    TutorialBot bot;
    SpeedItem speedItem;

    long setupTime;


    public TutorialMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
        player = team1.getTeamMembers()[0];
        bot = (TutorialBot) team2.getTeamMembers()[0];
        bot.setCurrentAbility(Gun.class);
    }

    @Override
    public void onSetup(){
        super.onSetup();

        startPosX = player.getX();
        startPosY = player.getY();
    }

    @Override
    public void onMatchEnded(){
        super.onMatchEnded();
    }

    @Override
    protected void end(Team winners) {
        super.end(winners);
        TimeUtils.executeIn(2000, new Runnable() {
            @Override
            public void run() {
                setActive(false, "Excellent work! You'll be a natural in no time.");
            }
        });

    }

    //TODO: fix end message
    @Override
    public void tick() {
        if (!hasMatchStarted()) {
            start();
        }

        if(player.getLives() == 0){
            player.setLives((byte) 1);
        }

        super.tick();

        if(player.isReady() && !isReady){
            setActive(true, "Hello and welcome to Project Ghost. \nTo get started, try to move around. \nClick where you want to go to direct your ship there.");
            isReady = true;
        }

        if ((player.getX() < startPosX - 300 || player.getX() > startPosX + 300 || player.getY() < startPosY - 300 || player.getY() > startPosY + 300) && !didMove) {
            TimeUtils.executeIn(500, new Runnable() {
                @Override
                public void run() {
                    setActive(true, "Good! Now, press *fire key* to fire your weapon. \nFiring a weapon reveals your position to your opponent. Try it out.");
                }
            });
            didMove = true;
        }

        if(player.didFire() && !didFire){
            bot.fire(player.getX(), player.getY());
            TimeUtils.executeIn(500, new Runnable() {
                @Override
                public void run() {
                    setActive(false, "Note that your opponent just revealed his position. \nUse this opportunity to adjust your aim.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setActive(true, "");
                }
            });
            didFire = true;
        }
        //TODO: test again after implementing spawning items
        if(bot.getLives() == 2 && !hitOnce){
            speedItem = new SpeedItem(TutorialMatch.this);
            setActive(true, "Nice shot! \nYou'll need to land two more hits to win.");
            TimeUtils.executeIn(5000, new Runnable() {
                @Override
                public void run() {
                    spawnItem(speedItem);
                    setActive(true, "This may give you a little boost. \nBe wary though, as picking things up can blow your cover.");
                }
            });
           hitOnce = true;
        }
        if(speedItem != null && speedItem.isActive() && !spawnItem){
           setActive(true, "Now, bring it on home!");
           spawnItem = true;
        }
    }
}
