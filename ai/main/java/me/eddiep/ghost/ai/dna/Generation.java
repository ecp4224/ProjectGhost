package me.eddiep.ghost.ai.dna;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.Main;
import com.boxtrotstudio.ghost.utils.Global;
import me.eddiep.ghost.ai.BotMatch;
import me.eddiep.ghost.ai.DNAAI;

import java.io.IOException;
import java.util.*;

public class Generation {
    private List<DNAAI> babies;
    private List<NetworkMatch> matches = new ArrayList<>();
    private ArrayList<DNAAI> potentialParents = new ArrayList<>();
    private boolean running;
    private int genNumber;
    private static int number = 0;

    public Generation(List<DNAAI> babies) {
        this.babies = babies;
        genNumber = ++number;
    }

    public List<DNAAI> getBabies() {
        return babies;
    }

    public int getGenNumber() {
        return genNumber;
    }

    public void start() {
        System.out.println("Creating matches for generation " + genNumber);
        running = true;
        while (!babies.isEmpty()) {
            int index = Global.RANDOM.nextInt(babies.size());
            DNAAI ai1 = babies.get(index);
            ai1.setGeneration(this);
            babies.remove(index);

            Team team1 = new Team(1, ai1);
            if (babies.isEmpty())
                break;

            int index2 = Global.RANDOM.nextInt(babies.size());
            DNAAI ai2 = babies.get(index2);
            ai2.setGeneration(this);
            babies.remove(index2);

            Team team2 = new Team(1, ai2);

            try {
                matches.add(createMatch(team1, team2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void end() {
        running = false;
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

        Collections.sort(potentialParents, new Comparator<DNAAI>() {
            @Override
            public int compare(DNAAI o1, DNAAI o2) {
                if (o2.getFitnessScore() < o1.getFitnessScore())
                    return -1;
                else if (o2.getFitnessScore() > o1.getFitnessScore())
                    return 1;
                else
                    return 0;
            }
        });
    }

    public double getGenerationScore() {
        if (running)
            throw new IllegalAccessError("Cannot get score while running");

        double avg = 0;
        double count = 0;
        for (DNAAI p : potentialParents) {
            if (p.getFitnessScore() > 0) {
                avg += p.getFitnessScore();
                count++;
            }
        }

        avg /= count;

        return avg;
    }

    public Generation createNextGeneration(double score) {
        Queue<DNAAI> parents = new LinkedList<>();


        for (DNAAI p : potentialParents) {
            if (p.getFitnessScore() > score)
                parents.offer(p);
        }

        int count = parents.size();

        potentialParents.clear();

        System.out.println("Average fitness score: " + score);

        System.out.println("Making babies");

        List<DNAAI> best = new ArrayList<>();

        while (!parents.isEmpty()) {
            DNAAI parent1 = parents.poll();
            if (parents.isEmpty())
                break;
            DNAAI parent2 = parents.poll();

            int toMake = (int)(100 / count) + 1;
            for (int i = 0; i < toMake; i++) {
                DNAAI baby1 = parent1.mateWith(parent2);
                DNAAI baby2 = parent2.mateWith(parent1);

                best.add(baby1);
                best.add(baby2);
            }
        }

        System.out.println(best.size() + " babies made!");

        matches.clear();

        return new Generation(best);
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

    public void onWin(DNAAI dnaai) {
        potentialParents.add(dnaai);
    }
}
