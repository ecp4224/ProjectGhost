package me.eddiep.ghost.server.game.entities.playable.impl;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.stats.TemporaryStats;
import me.eddiep.ghost.server.game.stats.TrackingMatchStats;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.utils.ArrayHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicAI extends BasePlayableEntity {
    Playable[] opponents;
    Playable[] allies;
    ArrayList<Entity> otherEntities = new ArrayList<>();
    ArrayList<Playable> visiblePlayables = new ArrayList<>();

    HashMap<Playable, Vector2f> lastSeen = new HashMap<>();

    long lastTryFired = System.currentTimeMillis();
    private long nextTry = Main.random(1000, 3000);

    public BasicAI() {
        setName("AI-" + Main.RANDOM.nextInt(255));
        setReady(true); //Bots are always ready :)
    }

    @Override
    public void onWin(Match match) { }

    @Override
    public void onLose(Match match) { }

    @Override
    public Client getClient() {
        return null;
    }

    @Override
    public void prepareForMatch() {
        super.prepareForMatch();

        opponents = getOpponents();
        allies = getAllies();
    }

    @Override
    public void onDamagePlayable(Playable hit) {

    }

    @Override
    public void onKilledPlayable(Playable killed) {

    }

    @Override
    public void onShotMissed() {

    }

    /*
    We want to remove the default behavior of sending this to the client
    This AI has no client, we just want to keep track of it locally
     */
    @Override
    public void spawnEntity(Entity e) {
        if (e instanceof Playable)
            return;

        otherEntities.remove(e);
    }

    /*
    We want to remove the default behavior of sending this to the client
    This AI has no client, we just want to keep track of it locally
     */
    @Override
    public void despawnEntity(Entity e) {
        if (e instanceof Playable)
            return;

        otherEntities.remove(e);
    }

    @Override
    public void updateEntity(Entity e) {
        if (!(e instanceof Playable))
            return;

        Playable p = (Playable)e;

        if (e.isVisible()) {
            lastSeen.put(p, p.getEntity().getPosition());
        }

        if (ArrayHelper.contains(allies, p))
            return; //We don't care about our allies

        if (p.getEntity().isVisible() && !visiblePlayables.contains(p))
            visiblePlayables.add(p);
        else if (!p.getEntity().isVisible() && visiblePlayables.contains(p))
            visiblePlayables.remove(p);
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
    public void tick() {
        super.tick();

        if (isDead())
            return;

        for (Playable p : visiblePlayables) {
            lastSeen.put(p, p.getEntity().getPosition());
        }

        if (visiblePlayables.size() == 0) {
            if (target != null) {

                if (System.currentTimeMillis() - lastTryFired > nextTry) {
                    if (Main.RANDOM.nextDouble() > 0.5) {
                        float x = Main.random(15, 1000);
                        float y = Main.random(15, 700);

                        fire(x, y);
                    }

                    lastTryFired = System.currentTimeMillis();
                    nextTry = Main.random(1500, 7000);
                }
            }

            wander();
        }
        else {
            seek();
        }

        goTowardsTarget();

        if (target == null && targetPlayable != null) {
            if (targetPlayable.getEntity().isVisible()) {
                fire(targetPlayable.getEntity().getX(), targetPlayable.getEntity().getY()); //We can see them, so fire at this location
            } else {
                Vector2f lastPos = lastSeen.get(targetPlayable);
                if (lastPos != null) {
                    fire(lastPos.x, lastPos.y); //Fire where we last saw them
                }
            }

            targetPlayable = null;
        }
    }


    private Vector2f target;
    private Playable targetPlayable;
    private void wander() {
        if (target == null) {
            float x = Main.random(15, 1000);
            float y = Main.random(15, 700);

            target = new Vector2f(x, y);
        }
    }

    private void seek() {
        if (targetPlayable == null) {
            targetPlayable = visiblePlayables.get(Main.RANDOM.nextInt(visiblePlayables.size()));
            target = targetPlayable.getEntity().getPosition(); //We can see him so lets target him!
        } else if (targetPlayable.getEntity().isVisible()) {
            if (Math.abs(target.sub(getPosition()).lengthSquared()) > 48) {
                target = targetPlayable.getEntity().getPosition();
            }
        }
    }

    private void goTowardsTarget() {
        if (target == null)
            return;
        if (Math.abs(target.sub(getPosition()).lengthSquared()) < 62.399998f) {
            target = null;
            return;
        }

        float x = getX();
        float y = getY();
        float targetX = target.x;
        float targetY = target.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);


        velocity.x = (float) (Math.cos(inv)*super.speed);
        velocity.y = (float) (Math.sin(inv)*super.speed);
    }

    private void fire(float x, float y) {
        if (System.currentTimeMillis() - lastFire < 300)
            return;
        useAbility(x, y, -1);
    }
}
