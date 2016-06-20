package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.abilities.JammedGun;
import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;
import com.boxtrotstudio.ghost.game.match.entities.items.JamItemEntity;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.abilities.Ability;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.Global;

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
        JammedGun ability = new JammedGun(target);
        target.setCurrentAbility(ability);
    }

    @Override
    protected void handleLogic() {
        if(target.didFire()){
            if (System.currentTimeMillis() - activationTime >= 2_000) {
                target.setCurrentAbility(ability);
                deactivate();
            }
        }
    }
}
