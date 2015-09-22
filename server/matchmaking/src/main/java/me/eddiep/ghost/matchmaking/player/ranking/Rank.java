package me.eddiep.ghost.matchmaking.player.ranking;


import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.utils.PFunction;
import org.bson.Document;

import java.util.List;

import static me.eddiep.ghost.utils.Constants.SCALING_FACTOR;

public class Rank {
    private double tau;

    //rating
    private double rating;
    //rating deviation
    private double rd;
    //volatillity
    private double vol;

    private List<RankedGame> games;
    
    private long lastUpdate;
    private RankingPeriod season;


    Rank(int rating, double rd, double vol) {
        setRating(rating);
        setRd(rd);
        setVol(vol);

        this.tau = me.eddiep.ghost.matchmaking.player.ranking.Glicko2.getInstance().getTau();
    }

    private Rank() { }

    public double getRawRating() {
        return rating;
    }

    public double getRawRd() {
        return rd;
    }

    public int getRating() {
        return (int) (this.rating * SCALING_FACTOR + me.eddiep.ghost.matchmaking.player.ranking.Glicko2.getInstance().getDefaultRating());
    }

    public void setRating(int rating) {
        this.rating = (int) ((rating - me.eddiep.ghost.matchmaking.player.ranking.Glicko2.getInstance().getDefaultRating()) / SCALING_FACTOR);
    }

    public double getRd() {
        return rd * SCALING_FACTOR;
    }

    public void setRd(double rd) {
        this.rd = rd / SCALING_FACTOR;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public void addResult(long player, Rankable opponent, double outcome) {
        Database.pushGameOutcome(player, opponent, outcome);
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    void update(RankingPeriod season) {
        if (!season.hasPlayed()) {
            rd = Math.sqrt((rd * rd) + (vol * vol));
            lastUpdate = System.currentTimeMillis();
            return;
        }

        games = season.getGames();

        double v = variance();

        double delta = delta(v);

        vol = calculateVolatility(v, delta);

        rd = Math.sqrt((rd * rd) + (vol * vol));

        rd = 1.0 / Math.sqrt((1.0 / (rd * rd)) + (1.0 / v));

        double sum = 0.0;
        for (int i = 0; i < games.size(); i++) {
            sum += G(games.get(i).rd) * (games.get(i).outcome - E(games.get(i).rank, games.get(i).rd));
        }

        rating += (rd * rd) * sum;
        lastUpdate = System.currentTimeMillis();

        games = null;
    }

    private double variance() {
        double sum = 0;
        for (int i = 0; i < games.size(); i++) {
            double temp = E(games.get(i).rank, games.get(i).rd);
            double temp2 = G(games.get(i).rd);
            temp2 = temp2 * temp2;
            sum += temp2 * temp * (1 - temp);
        }

        return sum;
    }

    private double E(double rating, double rds) {
        return 1.0 / (1.0 + Math.exp(-1 * G(rds) * (this.rating - rating)));
    }

    private double G(double rd) {
        return 1.0 / Math.sqrt(1 + 3 * (rd * rd) / (Math.PI * Math.PI));
    }

    private double delta(double v) {
        double sum = 0;
        for (int i = 0; i < games.size(); i++) {
            sum += G(games.get(i).rd) * (games.get(i).outcome - E(games.get(i).rank, games.get(i).rd));
        }

        return v * sum;
    }

    private PFunction<Double, Double> makef(final double delta, final double v, final double a) {
        return new PFunction<Double, Double>() {
            @Override
            public Double run(Double x) {
                return ( Math.exp(x) * ( (delta * delta) - (rd * rd) - v - Math.exp(x) ) /
                        (2.0 * Math.pow( (rd * rd) + v + Math.exp(x), 2) )) -
                        ( ( x - a ) / (tau * tau) );
            }
        };
    }

    private double calculateVolatility(double v, double delta) {
        double A = Math.log(vol * vol);
        PFunction<Double, Double> f = makef(delta, v, A);
        double epsilon = 0.000001;

        double B, k;
        if ((delta * delta) >  (rd * rd) + v){
            B = Math.log((delta * delta) -  (rd * rd) - v);
        } else {
            k = 1;
            while (f.run(A - (k * tau)) < 0){
                k = k + 1;
            }
            B = A - (k * this.tau);
        }

        double fA = f.run(A);
        double fB = f.run(B);

        double C, fC;
        while (Math.abs(B - A) > epsilon){
            C = A + (( (A-B)*fA ) / (fB - fA));
            //C = (A + ((A - B) * fA)) / (fB - fA);
            fC = f.run(C);
            if (fC * fB < 0){
                A = B;
                fA = fB;
            } else {
                fA = fA / 2.0;
            }
            B = C;
            fB = fC;
        }
        return Math.exp(A/2.0);
    }

    /*private double tau;
    private int ranking;
    private double rd;
    private double vol;
    private List<Integer> advRanks = new ArrayList<>();
    private List<Double> advRds = new ArrayList<>();
    private List<Double> outcomes = new ArrayList<>();

     */
    public Document asDocument() {
        return new Document()
                .append("rating", rating)
                .append("rd", rd)
                .append("vol", vol)
                .append("lastUpdate", lastUpdate);
    }

    public static Rank fromDocument(Document document) {
        Rank rank = new Rank();
        rank.rating = document.getDouble("rating");
        rank.rd = document.getDouble("rd");
        rank.vol = document.getDouble("vol");
        rank.lastUpdate = document.getLong("lastUpdate") == null ? 0 : document.getLong("lastUpdate");
        rank.tau = Glicko2.getInstance().getTau();

        return rank;
    }
}
