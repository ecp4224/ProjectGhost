package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.entities.items.EmpItemEntity;
import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.utils.Constants;

/**
 * An item that makes all opponents partially visible for 2 seconds.
 */
public class EmpItem extends Item {
    public EmpItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return EmpItemEntity.class;
    }

    @Override
    protected void onActivated() {
        for(int i = 0; i < activator.getOpponents().length; i++){
            activator.getOpponents()[i].setAlpha(160);
        }
    }

    @Override
    protected void handleLogic() {
        for(int i = 0; i < activator.getOpponents().length; i++){
            if(activator.getOpponents()[i].getAlpha() < 160){
                activator.getOpponents()[i].setAlpha(160);
            }
        }
        if (System.currentTimeMillis() - activationTime >= 2_000) {
            deactivate();
            for(int j = 0; j < activator.getOpponents().length; j++){
                activator.getOpponents()[j].fadeOut(Constants.FADE_SPEED);
            }
        }
    }
}