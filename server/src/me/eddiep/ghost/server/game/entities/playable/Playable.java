package me.eddiep.ghost.server.game.entities.playable;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.entities.abilities.Ability;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.stats.TemporaryStats;
import me.eddiep.ghost.server.game.stats.TrackingMatchStats;
import me.eddiep.ghost.server.network.Client;

import java.io.IOException;

public interface Playable {

    /**
     * Get the team this playable is on. If this playable is not in a match, then null is returned.
     * @return The team for this playable
     */
    Team getTeam();

    /**
     * Check whether or not this playable is currently in a match
     * @return True if the playable is in a match, otherwise false
     */
    boolean isInMatch();

    void spawnEntity(Entity entity) throws IOException;

    void despawnEntity(Entity e) throws IOException;

    Entity getEntity();

    /**
     * Subtract 1 life from this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void subtractLife();

    /**
     * Add 1 life to this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void addLife();

    /**
     * Reset this playable's lives and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void resetLives();

    void setLives(byte value);

    /**
     * Kill this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void kill();

    /**
     * Freeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void freeze();

    /**
     * Unfreeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    void unfreeze();

    boolean isConnected();

    void setMatch(ActiveMatch match);

    ActiveMatch getMatch();

    /**
     * Get the amount of lives this playable has
     * @return The amount of lives as a byte
     */
    byte getLives();

    /**
     * Check whether this playable is dead
     * @return True if the playable is dead, otherwise false
     */
    boolean isDead();

    /**
     * Check whether this playable is frozen or not.
     * @return True if the playable is frozen, otherwise false
     */
    boolean isFrozen();

    /**
     * Check to see if this playable is ready
     * @return True if the playable is ready, otherwise false
     */
    boolean isReady();

    /**
     * Set this playable's ready state <b>THIS DOES NOT UPDATE THE CLIENT. THIS METHOD SHOULD ONLY BE CALLED FROM {@link me.eddiep.ghost.server.network.packet.impl.ReadyPacket}</b>
     * @param isReady Whether this playable is ready
     */
    void setReady(boolean isReady);

    void onWin(Match match);

    void onLose(Match match);

    /**
     * Update this playable's state for all other players
     * @throws IOException If there was a problem updating other players
     */
    void updatePlayerState() throws IOException;

    /**
     * The Playable object <b>p</b> state has changed and this Playable object should be updated.
     * @param p The Playable object that got updated.
     * @throws IOException If there was a problem updating this Playable object
     */
    void playableUpdated(Playable p) throws IOException;


    /**
     * Update this Playable object's entity state for all other players
     * @throws IOException
     */
    void updateState() throws IOException;

    /**
     * Returns the client this Playable object is connected to, if this is a networking Playable object.
     * @return The remote client connected to this Playable object. If this an AI, this method should return null
     */
    Client getClient();

    /**
     * Prepare this Playable object for the match.
     */
    void prepareForMatch();

    /**
     * Get all the opponents of this playable.
     * @return All {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects that are opponents to this playable
     */
    Playable[] getOpponents();

    /**
     * Get all allies of this playable
     * @return All {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects that are allies to this playable
     */
    Playable[] getAllies();

    /**
     * This method should be invoked whenever a player fires a ability
     */
    void onFire();

    /**
     * This method is invoked when this playable is damaged by another playable
     * @param damager The playable that damaged this playable
     */
    void onDamage(Playable damager);

    /**
     * This method is invoked when this playable damages another playable
     * @param hit The playable that was damaged
     */
    void onDamagePlayable(Playable hit);

    /**
     * This method is invoked when this playable kills another playable
     * @param killed The playable that was killed
     */
    void onKilledPlayable(Playable killed);

    /**
     * This method is invoked when this playable missed a shot
     */
    void onShotMissed();

    /**
     * Get this Playable's current ability
     * @return The ability this player current has
     */
    Ability<Playable> currentAbility();

    /**
     * Set this Playable's current ability
     * @param class_ The ability class to set
     */
    void setCurrentAbility(Class<? extends Ability<Playable>> class_);

    /**
     * Get the current match tracking history for this Playable object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object
     */
    TrackingMatchStats getTrackingStats();

    /**
     * Get the current match stats for this Playable Object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.server.game.stats.TemporaryStats} object
     */
    TemporaryStats getCurrentMatchStats();
}
