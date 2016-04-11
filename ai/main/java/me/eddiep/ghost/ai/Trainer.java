package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.game.match.world.timeline.EntitySnapshot;
import com.boxtrotstudio.ghost.game.match.world.timeline.PlayableSnapshot;
import com.boxtrotstudio.ghost.game.match.world.timeline.TimelineCursor;
import com.boxtrotstudio.ghost.game.match.world.timeline.WorldSnapshot;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.Main;
import com.boxtrotstudio.ghost.test.game.TestPlayer;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PFunction;
import com.boxtrotstudio.ghost.utils.Vector2f;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationLOG;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.JordanPattern;
import org.encog.plugin.EncogPluginLogging1;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Trainer {
    static Queue<SmartAI> parents = new LinkedList<>();

    public static void main(String[] args) throws InterruptedException {


        Main.TO_INIT = new Class[]

                {
                        BotQueue.class
                };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.main(new String[]{"--offline"});
            }
        }).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Queue<SmartAI> best = new LinkedList<>();
        for (int i = 0; i < 200; i++) {
            best.offer(new SmartAI());
        }

        while (true) {
            List<NetworkMatch> matches = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                Team team1 = new Team(1, best.poll());
                Team team2 = new Team(1, best.poll());

                try {
                    matches.add(createMatch(team1, team2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Thread.sleep(60000);

            System.out.println("Choosing parents");

            best.clear();

            for (NetworkMatch m : matches) {
                if (!m.hasMatchEnded()) {
                    Team bestTeam;
                    int one = m.getTeam1().totalLives();
                    int two = m.getTeam2().totalLives();

                    if (one > two) {
                        bestTeam = m.getTeam1();
                    } else if (one < two) {
                        bestTeam = m.getTeam2();
                    } else {
                        bestTeam = Global.RANDOM.nextBoolean() ? m.getTeam1() : m.getTeam2();
                    }

                    parents.offer((SmartAI) bestTeam.getTeamMembers()[0]);
                }
            }

            System.out.println("Making babies");

            while (!parents.isEmpty()) {
                SmartAI parent1 = parents.poll();
                if (parents.isEmpty())
                    break;
                SmartAI parent2 = parents.poll();

                SmartAI baby1 = parent1.mateWith(parent2);
                SmartAI baby2 = parent2.mateWith(parent1);

                best.offer(baby1);
                best.offer(baby2);
            }

            System.out.println(best.size() + " babies made!");

            matches.clear();
        }
    }

    public static void matchEnded(NetworkMatch match) {
        Team winning = match.getWinningTeam();
        if (winning == null)
            winning = Global.RANDOM.nextBoolean() ? match.getTeam1() : match.getTeam2();

        parents.offer((SmartAI) winning.getTeamMembers()[0]);

        System.out.println(match.getID() + " ended");
    }

    static long id = 0;
    private static NetworkMatch createMatch(Team team1, Team team2) throws IOException {
        NetworkMatch match = new BotMatch(team1, team2, Main.TCP_UDP_SERVER);
        NetworkWorld world = new NetworkWorld("test", match);
        match.setQueueType(Queues.WEAPONSELECT);
        match.setWorld(world);
        match.setup();
        match.setID(id);
        id++;

        for (int i = 0; i < team1.getTeamLength(); i++) {
            team1.getTeamMembers()[i].setLives((byte) 3);
            team2.getTeamMembers()[i].setLives((byte) 3);
        }

        System.out.println("Created match with ID " + id);

        return match;
    }

    public static WorldSnapshot getNextTick(TimelineCursor cursor) {
        cursor.forwardOneTick();
        WorldSnapshot toReturn = cursor.get();
        cursor.backwardsOneTick();
        return toReturn;
    }

    public static List<MatchHistory> getMatches() {
        final Scanner scanner = new Scanner(System.in);
        File file;
        do {
            System.out.print("Please enter the directory of all the replay data: ");
            String directory = scanner.nextLine();
            file = new File(directory);
        } while (!file.exists() || !file.isDirectory());

        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("mdata");
            }
        });

        List<MatchHistory> matches = new LinkedList<>();
        for (File f : files) {
            String json = null;
            try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(f))) {
                ByteArrayOutputStream tempData = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer, 0, 1024)) != -1) {
                    tempData.write(buffer, 0, read);
                }

                json = new String(tempData.toByteArray(), Charset.forName("ASCII"));
                tempData.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (json == null)
                continue;

            MatchHistory matchData = Global.GSON.fromJson(json, MatchHistory.class);
            if (matchData.getTimeline().size() < 1800)
                continue;
            matches.add(matchData);
        }

        File[] directories = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });

        for (File dir : directories) {
            File[] f = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("mdata");
                }
            });

            for (File ff : f) {
                try {
                    String json = new String(Files.readAllBytes(ff.toPath()));

                    MatchHistory matchData = Global.GSON.fromJson(json, MatchHistory.class);
                    if (matchData.getTimeline().size() < 1800)
                        continue;
                    matches.add(matchData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return matches;
    }
}
