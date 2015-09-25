package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.abilities.Ability;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.game.stats.TemporaryStats;
import me.eddiep.ghost.game.stats.TrackingMatchStats;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.VisibleFunction;
import me.eddiep.ghost.utils.Vector2f;

public interface PlayableEntity extends PhysicsEntity {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;

    boolean visibleToAllies();

    void isVisibleToAllies(boolean val);

    /**
     * Get the point this playable is currently moving towards
     * @return The point this playable is moving towards
     */
    Vector2f getTarget();

    /**
     * Whether or not this playable is currently moving towards a point
     * @return True if the playable is moving towards a point, otherwise false
     */
    boolean hasTarget();

    /**
     * Set the point this playable should be moving towards
     * @param target The point this playable should move towards
     */
    void setTarget(Vector2f target);

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
     * Set this playable's ready state <b>THIS DOES NOT UPDATE THE CLIENT. THIS METHOD SHOULD ONLY BE CALLED FROM A PACKET OR A BOT</b>
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
     * @return All {@link me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer} objects that are opponents to this playable
     */
    PlayableEntity[] getOpponents();

    /**
     * Get all allies of this playable
     * @return All {@link me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer} objects that are allies to this playable
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
     * This method is invoked when the stats of this playable update.
     */
    void onStatUpdate(Stat stat);

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
     * Set this Playable's current ability
     * @param ability The ability class to set
     */
    void setCurrentAbility(Ability<PlayableEntity> ability);
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
     * Calculate the firerate based on the {@link PlayableEntity#getFireRateStat()} stat.
     * @param base The base value of this fire rate
     * @return The firerate to use.
     */
    long calculateFireRate(long base);

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
     * Get the speed stat for this playable object <br>
     * This stat is measured in px/tick
     */
    Stat getSpeedStat();

    /**
     * Get how fast this {@link me.eddiep.ghost.game.match.entities.PlayableEntity} object can move
     * @return The speed this playable object can move
     */
    float getSpeed();

    /**
     * Set how fast this {@link me.eddiep.ghost.game.match.entities.PlayableEntity} object can move
     * @param speed The speed this playable object can move
     */
    void setSpeed(float speed);

    /**
     * Get the firerate stat for this playble object <br>
     * This stat is measured as a percent decrease, so it may be adapted easily to other weapons
     */
    Stat getFireRateStat();

    boolean didFire();

    /**
     * Get the visible length stat for this playable object <br>
     * This stat is measured in ms, or how long a playable object is visible before fading out
     */
    Stat getVisibleLengthStat();

    /**
     * Get the visible strength stat for this playable object <br>
     * This stat is measured in alpha, and has a max of 255. It determines how transparent the player becomes when becoming
     * visible. The smaller this value the better for the player.
     */
    Stat getVisibleStrengthStat();

    /**
     * This method is called when this {@link me.eddiep.ghost.game.match.entities.PlayableEntity} activates an item
     * @param item The item that was activated
     */
    void onItemActivated(Item item, PlayableEntity activator);

    /**
     * This method is called when an {@link me.eddiep.ghost.game.match.item.Item} this {@link me.eddiep.ghost.game.match.entities.PlayableEntity}
     * had is deactivated
     * @param item The item that was deactivated
     */
    void onItemDeactivated(Item item, PlayableEntity owner);

    /**
     * Return whether this playable is invincible or not. While this value is true, the call to {@link PlayableEntity#subtractLife()} is ignored.
     * Collision should still occur when a playable is invincible, even if that collision may cause damage.
     * @return True if this playable is invincible, otherwise false
     */
    boolean isInvincible();

    /**
     * Set whether this playable is invincible or not. While this value is true, the call to {@link PlayableEntity#subtractLife()} is ignored.
     * @param val True if this playable should be invincible, otherwise false
     */
    void isInvincible(boolean val);

}
