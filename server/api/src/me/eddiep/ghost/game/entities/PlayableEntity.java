package me.eddiep.ghost.game.entities;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.entities.abilities.Ability;
import me.eddiep.ghost.game.stats.TemporaryStats;
import me.eddiep.ghost.game.stats.TrackingMatchStats;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.VisibleFunction;

public interface PlayableEntity extends Entity {

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

    /**
     * Subtract 1 life from this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void subtractLife();

    /**
     * Add 1 life to this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void addLife();

    /**
     * Reset this playable's lives and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void resetLives();

    /**
     * Set this playable's lives and update all other players
     * @param value
     */
    void setLives(byte value);

    /**
     * Kill this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void kill();

    /**
     * Freeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void freeze();

    /**
     * Unfreeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    void unfreeze();

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

    /**
     * This method is invoked when this playable object wins a match
     * @param match The match this playable object won
     */
    void onWin(Match match);

    /**
     * This method is invoked when this playable object loses a match
     * @param match The match this playable object lost
     */
    void onLose(Match match);

    /**
     * Determines whether or not the playable object <b>p</b> can receive updates about this playable object
     * @param p The playable update requesting the updates
     * @return True if they can receive updates, otherwise false
     */
    boolean shouldSendUpdatesTo(PlayableEntity p);

    /**
     * Prepare this Playable object for the match.
     */
    void prepareForMatch();

    /**
     * Get all the opponents of this playable.
     * @return All {@link me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer} objects that are opponents to this playable
     */
    PlayableEntity[] getOpponents();

    /**
     * Get all allies of this playable
     * @return All {@link me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer} objects that are allies to this playable
     */
    PlayableEntity[] getAllies();

    /**
     * This method should be invoked whenever a player fires a ability
     */
    void onFire();

    /**
     * This method is invoked when this playable is damaged by another playable
     * @param damager The playable that damaged this playable
     */
    void onDamage(PlayableEntity damager);

    /**
     * This method is invoked when this playable damages another playable
     * @param hit The playable that was damaged
     */
    void onDamagePlayable(PlayableEntity hit);

    /**
     * This method is invoked when this playable kills another playable
     * @param killed The playable that was killed
     */
    void onKilledPlayable(PlayableEntity killed);

    /**
     * This method is invoked when this playable missed a shot
     */
    void onShotMissed();

    /**
     * Get this Playable's current ability
     * @return The ability this player current has
     */
    Ability<PlayableEntity> currentAbility();

    /**
     * Set this Playable's current ability
     * @param class_ The ability class to set
     */
    void setCurrentAbility(Class<? extends Ability<PlayableEntity>> class_);

    /**
     * Get the current match tracking history for this Playable object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.game.stats.TrackingMatchStats} object
     */
    TrackingMatchStats getTrackingStats();

    /**
     * Get the current match stats for this Playable Object. It is ideal for all Playable objects
     * to implement this feature to allow full match history
     * @return The {@link me.eddiep.ghost.game.stats.TemporaryStats} object
     */
    TemporaryStats getCurrentMatchStats();

    /**
     * Whether this playable sprite should be able to use abilities
     * @return True if this sprite can use abilities, otherwise false
     */
    boolean canFire();

    /**
     * Set whether this playable sprite should be able to use abilities
     * @param value True if this sprite can use abilities, otherwise false
     */
    void setCanFire(boolean value);

    /**
     * Get the visible function for this playable object
     * @return The visible function currently being used
     */
    VisibleFunction getVisibleFunction();

    /**
     * Set the visible function for this playable object
     * @param function The visible function to use
     */
    void setVisibleFunction(VisibleFunction function);

    /**
     * Get how fast this {@link me.eddiep.ghost.game.entities.PlayableEntity} object can move
     * @return The speed this playable object can move
     */
    float getSpeed();

    /**
     * Set how fast this {@link me.eddiep.ghost.game.entities.PlayableEntity} object can move
     * @param speed The speed this playable object can move
     */
    void setSpeed(float speed);
}
