package me.eddiep.ghost.common.game;

import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.item.SpeedItem;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.TimeUtils;

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
    public void setup() {
        setWorld(new NetworkWorld("tutorial", this)); //set the world to the tutorial level

        super.setup();

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

        /*if(player.isReady() && !isReady){
            setActive(true, "Hello and welcome to Project Ghost. \nTo get started, try to move around. \nClick where you want to go to direct your ship there.");
            isReady = true;
            player.setCanFire(false);
        }*/

        super.tick();

        /*
        Here we'll check if the match is active and we haven't displayed this message yet.
        The match will only ever become active when the player ready's up, so it has the same effect as the
        if statement above (commented out).
        However, we want LiveMatchImpl to start the match naturally, so we simply only check if the match has already
        started and change the status message.
         */
        if (isMatchActive() && !isReady) {
            setActive(true, "Hello and welcome to Project Ghost. \nTo get started, try to move around. \nClick where you want to go to direct your player there.");
            isReady = true;
            player.setCanFire(false);
        }

        if ((player.getX() < startPosX - 300 || player.getX() > startPosX + 300 || player.getY() < startPosY - 300 || player.getY() > startPosY + 300) && !didMove) {
            TimeUtils.executeInSync(500, new Runnable() {
                @Override
                public void run() {
                    setActive(true, "Good! Now, press the Right Mouse Button to fire your weapon. \nFiring a weapon reveals your position to your opponent. Try it out.");
                }
            }, super.world);
            didMove = true;
            player.setCanFire(true);
        }

        if(player.didFire() && !didFire){
            if(bot.getLives() < 3){
                bot.setLives((byte) 3);
            }
            bot.fire(player.getX(), player.getY());
            TimeUtils.executeInSync(500, new Runnable() {
                @Override
                public void run() {
                    /*We freeze the player here rather than set the match to inactive because
                      setting the match to inactive will cause the timeline to pause, so everything
                      that will happen in the next 2 seconds won't get recorded if the match is inactive.
                      Freezing the player has the same effect as it prevents the player from moving, but
                      it also keeps the timeline recording
                    */
                    player.freeze();
                    setActive(true, "Note that your opponent just revealed his position. \nUse this opportunity to adjust your aim.");
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.unfreeze();
                    setActive(true, "");
                }
            }, super.world);
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
