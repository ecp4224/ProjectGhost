package me.eddiep.ghost.test.game.queue.impl;


import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.VisibleFunction;
import me.eddiep.ghost.test.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChooseWeaponQueue extends DemoQueue {
    @Override
    public void setupPlayer(PlayableEntity p) {
        p.setLives((byte) 3);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
    }

    @Override
    public String description() {
        return "Pick a weapon";
    }

    @Override
    public Queues queue() {
        return Queues.WEAPONSELECT;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }
}
