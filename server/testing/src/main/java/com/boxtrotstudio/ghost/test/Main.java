package com.boxtrotstudio.ghost.test;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.game.bots.TestPlayableEntity;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.packet.PlayerPacketFactory;
import com.boxtrotstudio.ghost.game.match.abilities.Dash;
import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.abilities.Laser;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.Text;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;
import com.boxtrotstudio.ghost.network.sql.SQL;
import com.boxtrotstudio.ghost.network.sql.impl.OfflineDB;
import com.boxtrotstudio.ghost.test.game.TestMatchCreator;
import com.boxtrotstudio.ghost.test.game.TestPlayerCreator;
import com.boxtrotstudio.ghost.test.game.queue.PlayerQueue;
import com.boxtrotstudio.ghost.test.game.queue.impl.*;
import com.boxtrotstudio.ghost.test.network.HttpServer;
import com.boxtrotstudio.ghost.test.network.TestServer;
import com.boxtrotstudio.ghost.test.network.packets.LeaveQueuePacket;
import com.boxtrotstudio.ghost.test.network.packets.QueueRequestPacket;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PRunnable;
import com.boxtrotstudio.ghost.utils.Scheduler;
import me.eddiep.jconfig.JConfig;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class Main {
    public static final HttpServer HTTP_SERVER = new HttpServer();
    public static BaseServer TCP_UDP_SERVER;
    public static boolean OFFLINE;
    public static String[] args;
    public static String DEFAULT_MAP = "test";
    public static HashMap<Queues, PlayerQueue> playerQueueHashMap = new HashMap<>();

    public static Class[] TO_INIT = {
            OriginalQueue.class,
            LaserQueue.class,
            //ChooseWeaponQueue.class,
            TwoVTwoQueue.class,
            TutorialQueue.class,
            DashQueue.class,
            BoomQueue.class
    };
    private static boolean stressTest;

    public static void main(String[] args) {
        double time = System.currentTimeMillis();
        double r = Double.MAX_VALUE;
        while (r > 360) {
            r -= 360;
        }
        System.out.println(System.currentTimeMillis() - time);

        Text text = Text.create()
                .text("Test")
                .position(1, 1)
                .color(Color.WHITE)
                .build();

        System.out.println(text);

        ServerConfig conf = readConfig();
        TCP_UDP_SERVER = new TestServer(conf);

        Main.args = args;
        if (ArrayHelper.contains(args, "--offline")) {
            Global.SQL = new OfflineDB();
            Global.SQL.loadAndSetup();
            OFFLINE = true;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--default-map")) {
                DEFAULT_MAP = args[i + 1];
            }
        }


        Scheduler.init();

        MatchFactory.setMatchCreator(new TestMatchCreator());
        PlayerFactory.setPlayerCreator(new TestPlayerCreator());

        PlayerPacketFactory.addPacket((byte) 0x05, 1, new QueueRequestPacket());
        PlayerPacketFactory.addPacket((byte) 0x20, 0, new LeaveQueuePacket());

        if (!OFFLINE) {
            TCP_UDP_SERVER.getLogger().debug("Connecting to SQL");

            setupSQL(conf);
        }

        TCP_UDP_SERVER.getLogger().debug("Starting http test..");

        HTTP_SERVER.start();

        TCP_UDP_SERVER.getLogger().debug("Starting tcp/udp test..");

        TCP_UDP_SERVER.start();

        Global.DEFAULT_SERVER = TCP_UDP_SERVER;

        TCP_UDP_SERVER.setDebugMode(ArrayHelper.contains(args, "--debug"));

        TCP_UDP_SERVER.getLogger().debug("Setting up Queue System");

        final PlayerQueue[] queues = initQueue();

        if (ArrayHelper.contains(args, "--stress")) {
            stressTest = true;
            System.err.println("Stress mode active!");
            int MAX_MATCHES = 1000;
            int TEAM_SIZE = 1;
            System.err.println("Creating " + MAX_MATCHES + " matches!");

            final Class[]  class_ = new Class[] {
                    Gun.class,
                    Laser.class,
                    Dash.class
            };

            for (int i = 0; i < MAX_MATCHES; i++) {
                NetworkMatch match = createTestMatch(TEAM_SIZE, i);
                ArrayHelper.forEach(ArrayHelper.combine(match.getTeam1().getTeamMembers(), match.getTeam2().getTeamMembers()), new PRunnable<PlayableEntity>() {
                    @Override
                    public void run(PlayableEntity p) {
                        p.setLives((byte) 3);
                        int t = Global.random(0, 3);
                        switch (t) {
                            case 0:
                                p.setCurrentAbility(new Gun(p));
                                break;
                            case 1:
                                p.setCurrentAbility(new Laser(p));
                                break;
                            case 2:
                                p.setCurrentAbility(new Dash(p));
                                break;
                        }
                        p.setVisibleFunction(VisibleFunction.ORIGINAL);
                    }
                });

                match.start();
            }

        }

        TCP_UDP_SERVER.getLogger().debug("Processing queues every " + (Global.QUEUE_MS_DELAY / 1000) + " seconds..");

        Scheduler.scheduleRepeatingTask(() -> processQueues(queues), Global.QUEUE_MS_DELAY);
    }

    private static NetworkMatch createTestMatch(int TEAM_SIZE, int id) {

        PlayableEntity[] p1 = new PlayableEntity[TEAM_SIZE];
        PlayableEntity[] p2 = new PlayableEntity[TEAM_SIZE];

        for (int i = 0; i < TEAM_SIZE; i++) {
            p1[i] = new TestPlayableEntity("1_" + i);
            p2[i] = new TestPlayableEntity("2_" + i);
        }

        Team team1 = new Team(1, p1);
        Team team2 = new Team(2, p2);

        try {
            return MatchFactory.getCreator().createMatchFor(team1, team2, id, Queues.TEST, "test", TCP_UDP_SERVER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; //wat
    }

    private static ServerConfig readConfig() {
        ServerConfig config = JConfig.newConfigObject(ServerConfig.class);
        File file = new File("test.conf");
        if (!file.exists()) {
            config.save(file);
        } else {
            config.load(file);
        }
        return config;
    }

    private static void setupSQL(ServerConfig config) {
        try {
            Global.SQL = (SQL) Class.forName(config.getSQLDriver()).newInstance();
            Global.SQL.loadAndSetup();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static PlayerQueue[] initQueue() {
        PlayerQueue[] queues = new PlayerQueue[TO_INIT.length];
        for (int i = 0; i < queues.length; i++) {
            try {
                Class class_ = TO_INIT[i];
                PlayerQueue queue = (PlayerQueue) class_.newInstance();
                TCP_UDP_SERVER.getLogger().debug("Init " + queue.queue().name());
                queues[i] = queue;
                playerQueueHashMap.put(queue.queue(), queue);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return queues;
    }

    private static void processQueues(PlayerQueue[] queues) {
        for (PlayerQueue queue : queues) {
            if (queue == null) continue;

            queue.processQueue();
        }
    }
}
