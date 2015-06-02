package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.playable.impl.Player;

public abstract class PlayerAbility implements Ability<Player> {

    private Player p;

    public PlayerAbility(Player p) {
        this.p = p;
    }

    @Override
    public Player owner() {
        return p;
    }
}
