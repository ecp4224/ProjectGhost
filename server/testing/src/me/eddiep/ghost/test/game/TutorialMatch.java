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

    boolean didMove, didFire, hitOnce, spawnItem;
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
        setActive(false, "Hello and welcome to Project Ghost. Press *movement keys* to move.");

        startPosX = player.getX();
        startPosY = player.getY();
    }

    @Override
    public void onMatchEnded(){
        super.onMatchEnded();
        setActive(false, "Excellent work! You'll be a natural in no time.");
    }

    @Override
    public void tick() {
        if (!hasMatchStarted()) {
            start();
        }

        if(player.getLives() == 0){
            player.setLives((byte) 1);
        }

        super.tick();

        if ((player.getX() < startPosX - 200 || player.getX() > startPosX + 200 || player.getY() < startPosY - 200 || player.getY() > startPosY + 200) && !didMove) {
            setActive(true, "Good! Now, press *fire key* to fire your weapon. Firing a weapon reveals your position to your opponent. Try it out.");
            didMove = true;
        }

        if(player.didFire() && !didFire){
            bot.fire(player.getX(), player.getY());
            TimeUtils.executeIn(500, new Runnable() {
                @Override
                public void run() {
                    setActive(true, "Note that your opponent is now visible. Use this opportunity to strike! Be quick; visibility doesn't last long.");
                }
            });
            didFire = true;
        }

        if(bot.getLives() == 2 && !hitOnce){
            speedItem = new SpeedItem(TutorialMatch.this);
            setActive(true, "Nice shot! You'll need to land two more hits to win.");
            TimeUtils.executeIn(1000, new Runnable() {
                @Override
                public void run() {
                    spawnItem(speedItem);
                    setActive(true, "This may give you a little boost. Be wary though, as picking things up can blow your cover.");
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
