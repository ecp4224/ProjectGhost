package com.boxtrotstudio.ghost.game.match;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.Condition;
import com.boxtrotstudio.ghost.utils.PFunction;
import com.boxtrotstudio.ghost.utils.WhenAction;

import java.util.ArrayList;
import java.util.Iterator;


public abstract class StagedMatch extends LiveMatchImpl {
    private PFunction<PlayableEntity, Boolean> currentCondition = null;
    private ArrayList<WhenAction> actions = new ArrayList<>();
    private ArrayList<WhenAction> toadd = new ArrayList<>();
    private Thread stageThread;
    private boolean checking = false;

    public StagedMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    @Override
    public void setup() {
        super.setup();

        stageThread = new Thread(STAGE_RUNNABLE);
        stageThread.start();
    }

    @Override
    public void tick() {
        if (!hasMatchEnded()) {
            if (currentCondition != null) {
                if (currentCondition.run(getPlayer())) {
                    wakeUp();
                }
            }
        }

        checking = true;
        Iterator<WhenAction> actionIterator = actions.iterator();
        while (actionIterator.hasNext()) {
            WhenAction action = actionIterator.next();

            if (action.check()) {
                actionIterator.remove();
            }
        }
        checking = false;
        actions.addAll(toadd);
        toadd.clear();

        super.tick();
    }

    private synchronized void wakeUp() {
        currentCondition = null;
        super.notifyAll();
    }

    protected synchronized void waitFor(PFunction<PlayableEntity, Boolean> function) {
        this.currentCondition = function;

        while (true) {
            if (currentCondition == null)
                break;
            try {
                super.wait(0L);
            } catch (InterruptedException ignored) { }
        }
    }

    protected synchronized void waitFor(Condition condition) {
        waitFor(val -> condition.run());
    }

    protected synchronized void waitFor(long duration) {
        long startTime = System.currentTimeMillis();
        waitFor(val -> System.currentTimeMillis() - startTime >= duration);
    }

    protected WhenAction when(Condition condition) {
        WhenAction<Void> action = WhenAction.when(null, obj -> condition.run());

        if (!checking) {
            actions.add(action);
        } else {
            toadd.add(action);
        }

        return action;
    }

    protected <T> WhenAction<T> when(T object, PFunction<T, Boolean> condition) {
        WhenAction<T> action = WhenAction.when(object, condition);


        if (!checking) {
            actions.add(action);
        } else {
            toadd.add(action);
        }

        return action;
    }

    public PlayableEntity getPlayer() {
        return getTeam1().getTeamMembers()[0];
    }

    protected abstract void stage();

    private final Runnable STAGE_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            waitFor(player -> isMatchActive());

            stage();
        }
    };
}
