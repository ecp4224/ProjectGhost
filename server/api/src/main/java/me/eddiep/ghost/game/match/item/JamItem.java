package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.abilities.Ability;
import me.eddiep.ghost.game.match.abilities.JammedGun;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.entities.items.JamItemEntity;
import me.eddiep.ghost.utils.Global;

/**
 * An item that makes a random player on the opposing team unable to fire for 1 shot.
 */
public class JamItem extends Item {
    PlayableEntity target;
    Ability<PlayableEntity> ability;
    public JamItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return JamItemEntity.class;
    }

    @Override
    protected void onActivated() {
        target = activator.getOpponents()[Global.random(0, activator.getOpponents().length)];
        ability = target.currentAbility();
        target.setCurrentAbility(JammedGun.class);
    }

    @Override
    protected void handleLogic() {
        if(target.didFire()){
           target.setCurrentAbility(ability);
            deactivate();
        }
    }
}
//TODO: implement clientside