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
import me.eddiep.ghost.ai.dna.Generation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class DNATrainer {
    static Queue<DNAAI> parents = new LinkedList<>();
    static ArrayList<DNAAI> potentialParents = new ArrayList<>();

    public static List<DNAAI> daBest = new ArrayList<>();

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

        List<DNAAI> best = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            best.add(new DNAAI());
        }

        Generation current = new Generation(best);
        double lastScore = 0;
        for (int gen = 0; gen < 60; gen++) {
            current.start();
            Thread.sleep(60000);
            current.end();

            double score = current.getGenerationScore();
            if (score < lastScore)
                score = lastScore;

            current = current.createNextGeneration(score);
            lastScore = score;
        }

        daBest = new ArrayList<>(current.getBabies());
    }

    public static void matchEnded(NetworkMatch match) {
        Team winning = match.getWinningTeam();
        if (winning == null)
            winning = Global.RANDOM.nextBoolean() ? match.getTeam1() : match.getTeam2();

        potentialParents.add((DNAAI)winning.getTeamMembers()[0]);
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
