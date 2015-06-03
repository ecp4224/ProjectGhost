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
    public Team getTeam();

    /**
     * Check whether or not this playable is currently in a match
     * @return True if the playable is in a match, otherwise false
     */
    public boolean isInMatch();

    public void spawnEntity(Entity entity) throws IOException;

    public void despawnEntity(Entity e) throws IOException;

    public Entity getEntity();

    /**
     * Subtract 1 life from this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void subtractLife();

    /**
     * Add 1 life to this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void addLife();

    /**
     * Reset this playable's lives and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void resetLives();

    public void setLives(byte value);

    /**
     * Kill this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void kill();

    /**
     * Freeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void freeze();

    /**
     * Unfreeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void unfreeze();

    public boolean isConnected();

    public void setMatch(ActiveMatch match);

    public ActiveMatch getMatch();

    /**
     * Get the amount of lives this playable has
     * @return The amount of lives as a byte
     */
    public byte getLives();

    /**
     * Check whether this playable is dead
     * @return True if the playable is dead, otherwise false
     */
    public boolean isDead();

    /**
     * Check whether this playable is frozen or not.
     * @return True if the playable is frozen, otherwise false
     */
    public boolean isFrozen();

    /**
     * Check to see if this playable is ready
     * @return True if the playable is ready, otherwise false
     */
    public boolean isReady();

    /**
     * Set this playable's ready state <b>THIS DOES NOT UPDATE THE CLIENT. THIS METHOD SHOULD ONLY BE CALLED FROM {@link me.eddiep.ghost.server.network.packet.impl.ReadyPacket}</b>
     * @param isReady Whether this playable is ready
     */
    public void setReady(boolean isReady);

    public void onWin(Match match);

    public void onLose(Match match);

    /**
     * Update this playable's state for all other players
     * @throws IOException If there was a problem updating other players
     */
    public void updatePlayerState() throws IOException;

    /**
     * The Playable object <b>p</b> state has changed and this Playable object should be updated.
     * @param p The Playable object that got updated.
     * @throws IOException If there was a problem updating this Playable object
     */
    public void playableUpdated(Playable p) throws IOException;


    /**
     * Update this Playable object's entity state for all other players
     * @throws IOException
     */
    public void updateState() throws IOException;

    /**
     * Returns the client this Playable object is connected to, if this is a networking Playable object.
     * @return The remote client connected to this Playable object. If this an AI, this method should return null
     */
    public Client getClient();

    /**
     * Prepare this Playable object for the match.
     */
    public void prepareForMatch();

    /**
     * Get all the opponents of this playable.
     * @return All {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects that are opponents to this playable
     */
    public Playable[] getOpponents();

    /**
     * Get all allies of this playable
     * @return All {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects that are allies to this playable
     */
    public Playable[] getAllies();

    /**
     * This method should be invoked whenever a player fires a ability
     */
    public void onFire();

    /**
     * This method is invoked when this playable is damaged by another playable
     * @param damager The playable that damaged this playable
     */
    public void onDamage(Playable damager);

    /**
     * This method is invoked when this playable damages another playable
     * @param hit The playable that was damaged
     */
    public void onDamagePlayable(Playable hit);

    /**
     * This method is invoked when this playable kills another playable
     * @param killed The playable that was killed
     */
    public void onKilledPlayable(Playable killed);

    /**
     * This method is invoked when this playable missed a shot
     */
    public void onShotMissed();

    /**
     * Get this Playable's current ability
     * @return The ability this player current has
     */
    public Ability<Playable> currentAbility();

    /**
     * Set this Playable's current ability
     * @param class_ The ability class to set
     */
    public void setCurrentAbility(Class<? extends Ability<Playable>> class_);

    /**
     * Get the current match tracking history for this Playable object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object
     */
    public TrackingMatchStats getTrackingStats();

    /**
     * Get the current match stats for this Playable Object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.server.game.stats.TemporaryStats} object
     */
    public TemporaryStats getCurrentMatchStats();

    /**
     * Whether this playable sprite should be able to use abilities
     * @return True if this sprite can use abilities, otherwise false
     */
    public boolean canFire();

    /**
     * Set whether this playable sprite should be able to use abilities
     * @param value True if this sprite can use abilities, otherwise false
     */
    public void setCanFire(boolean value);
}
