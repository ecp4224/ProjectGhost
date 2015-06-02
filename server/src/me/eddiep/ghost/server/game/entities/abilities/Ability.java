package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.playable.Playable;

public interface Ability<T extends Playable> {

    /**
     * The name of this ability
     * @return The name as a String
     */
    public String name();

    /**
     * The owner of this ability.
     * @return The owner
     */
    public T owner();

    /**
     * The entity has executed this ability with the mouse position at <b>targetX</b> and <b>targetY</b>
     * @param targetX The X position this ability was used
     * @param targetY The Y position this ability was used
     */
    public void use(float targetX, float targetY);
}
