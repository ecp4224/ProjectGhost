package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.game.match.world.timeline.TimelineCursor;
import com.boxtrotstudio.ghost.game.match.world.timeline.WorldSnapshot;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.Main;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Trainer {
    static Queue<SmartAI> parents = new LinkedList<>();

    public static List<SmartAI> daBest = new ArrayList<>();

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

        List<SmartAI> best = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            best.add(new SmartAI());
        }

        for (int gen = 0; gen < 20; gen++) {
            List<NetworkMatch> matches = new ArrayList<>();

            while (!best.isEmpty()) {
                int index = Global.RANDOM.nextInt(best.size());
                SmartAI ai1 = best.get(index);
                best.remove(index);

                Team team1 = new Team(1, ai1);
                if (best.isEmpty())
                    break;

                int index2 = Global.RANDOM.nextInt(best.size());
                SmartAI ai2 = best.get(index2);
                best.remove(index2);

                Team team2 = new Team(1, ai2);

                try {
                    matches.add(createMatch(team1, team2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Generation #" + gen + " started!");
            System.out.println("Waiting one minute...");

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

                    m.end(bestTeam);
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
                SmartAI baby3 = parent1.mateWith(parent2);
                SmartAI baby4 = parent2.mateWith(parent1);

                best.add(baby1);
                best.add(baby2);
                best.add(baby3);
                best.add(baby4);
            }

            System.out.println(best.size() + " babies made!");

            matches.clear();
        }

        daBest = new ArrayList<>(best);
    }

    public static void matchEnded(NetworkMatch match) {
        Team winning = match.getWinningTeam();
        if (winning == null)
            winning = Global.RANDOM.nextBoolean() ? match.getTeam1() : match.getTeam2();

        parents.offer((SmartAI) winning.getTeamMembers()[0]);
    }

    static long id = 0;
    private static NetworkMatch createMatch(Team team1, Team team2) throws IOException {
        NetworkMatch match = new BotMatch(team1, team2, Main.TCP_UDP_SERVER);
        MatchFactory.getCreator().createMatchFor(match, id, Queues.WEAPONSELECT, "tutorial", Main.TCP_UDP_SERVER);
        id++;

        for (int i = 0; i < team1.getTeamLength(); i++) {
            team1.getTeamMembers()[i].setLives((byte) 3);
            team2.getTeamMembers()[i].setLives((byte) 3);
        }

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
