package me.eddiep.ghost.game.ranking;

import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.PFunction;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static me.eddiep.ghost.utils.Constants.*;

public class Rank {
    private double tau;
    private double rating;
    private double rd;
    private double vol;
    private long lastUpdate;
    private List<Double> advRanks = new ArrayList<>();
    private List<Double> advRds = new ArrayList<>();
    private List<Double> outcomes = new ArrayList<>();


    Rank(int rating, double rd, double vol) {
        setRating(rating);
        setRd(rd);
        setVol(vol);

        this.tau = Glicko2.getInstance().getTau();
    }

    private Rank() { }

    public int getRating() {
        return (int) (this.rating * SCALING_FACTOR + Glicko2.getInstance().getDefaultRating());
    }

    public void setRating(int rating) {
        this.rating = (int) ((rating - Glicko2.getInstance().getDefaultRating()) / SCALING_FACTOR);
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

    void addResult(BaseNetworkPlayer opponent, double outcome) {
        this.advRanks.add(opponent.getRanking().rating);
        this.advRds.add(opponent.getRanking().rd);
        this.outcomes.add(outcome);
    }

    public boolean hasPlayed() {
        return outcomes.size() > 0;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void update() {
        if (!this.hasPlayed()) {
            rd = Math.sqrt((rd * rd) + (vol * vol));
            lastUpdate = System.currentTimeMillis();
            return;
        }

        double v = variance();

        double delta = delta(v);

        vol = calculateVolatility(Glicko2.getInstance().getAlgorithm(), v, delta);

        rd = Math.sqrt((rd * rd) + (vol * vol));

        rd = 1.0 / Math.sqrt((1.0 / (rd * rd)) + (1.0 / v));

        double sum = 0.0;
        for (int i = 0; i < advRanks.size(); i++) {
            sum += G(advRds.get(i)) * (outcomes.get(i) - E(advRanks.get(i), advRds.get(i)));
        }

        rating += (rd * rd) * sum;
        lastUpdate = System.currentTimeMillis();
    }

    private double variance() {
        double sum = 0;
        for (int i = 0; i < advRanks.size(); i++) {
            double temp = E(advRanks.get(i), advRds.get(i));
            double temp2 = G(advRds.get(i));
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
        for (int i = 0; i < advRanks.size(); i++) {
            sum += G(advRds.get(i)) * (this.outcomes.get(i) - E(this.advRanks.get(i), this.advRds.get(i)));
        }

        return v * sum;
    }


    public double calculateVolatility(String algorithm, double v, double delta) {
        switch (algorithm) {
            case "oldprocedure":
                return oldprocedure(v, delta);
            case "newprocedure":
                return newprocedure(v, delta);
            case "newprocedure_mod":
                return newprocedure_mod(v, delta);
            case "oldprocedure_simple":
                return oldprocedure_simple(v, delta);
        }
        return 0.0;
    }

    private double oldprocedure(double v, double delta) {
        double sigma = vol;
        double phi = rd;
        double tau = this.tau;

        double a, x1, x2, x3, y1, y2, y3, upper;
        double result;

        upper = find_upper_falsep(phi, v, delta, tau);

        a = Math.log(Math.pow(sigma, 2));
        y1 = equation(phi, v, 0, a, tau, delta);
        if (y1 > 0 ){
            result = upper;
        } else {
            x1 = 0;
            x2 = x1;
            y2 = y1;
            x1 = x1 - 1;
            y1 = equation(phi, v, x1, a, tau, delta);
            while (y1 < 0){
                x2 = x1;
                y2 = y1;
                x1 = x1 - 1;
                y1 = equation(phi, v, x1, a, tau, delta);
            }
            for (int i = 0; i<21; i++){
                x3 = y1 * (x1 - x2) / (y2 - y1) + x1;
                y3 = equation(phi, v, x3, a, tau, delta);
                if (y3 > 0 ){
                    x1 = x3;
                    y1 = y3;
                } else {
                    x2 = x3;
                    y2 = y3;
                }
            }
            if (Math.exp((y1 * (x1 - x2) / (y2 - y1) + x1) / 2) > upper ){
                result = upper;
            } else {
                result = Math.exp((y1 * (x1 - x2) / (y2 - y1) + x1) / 2);
            }
        }
        return result;
    }

    private double find_upper_falsep(double phi, double v, double delta, double tau) {
        double x1, x2, x3, y1, y2, y3;
        y1 = Dequation(phi, v, 0, tau, delta);
        if (y1 < 0 ){
            return 1;
        } else {
            x1 = 0;
            x2 = x1;
            y2 = y1;
            x1 = x1 - 1.0;
            y1 = Dequation(phi, v, x1, tau, delta);
            while (y1 > 0){
                x2 = x1;
                y2 = y1;
                x1 = x1 - 1.0;
                y1 = Dequation(phi, v, x1, tau, delta);
            }
            for (int i = 0; i < 21 ; i++){
                x3 = y1 * (x1 - x2) / (y2 - y1) + x1;
                y3 = Dequation(phi, v, x3, tau, delta);
                if (y3 > 0 ){
                    x1 = x3;
                    y1 = y3;
                } else {
                    x2 = x3;
                    y2 = y3;
                }
            }
            return Math.exp((y1 * (x1 - x2) / (y2 - y1) + x1) / 2);
        }
    }

    private double Dequation(double phi, double v, double x, double tau, double delta) {
        double d = (phi * phi) + v + Math.exp(x);
        return -1.0 / (tau * tau) - 0.5 * Math.exp(x) / d + 0.5 * Math.exp(x) * (Math.exp(x) + (delta * delta)) / (d * d) - (Math.exp(x) * Math.exp(x)) * (delta * delta) / (d * d * d);
    }

    private double equation(double phi, double v, double x, double a, double tau, double delta) {
        double d = Math.pow(phi, 2) + v + Math.exp(x);
        return -(x - a) / (tau * tau) - 0.5 * Math.exp(x) / d + 0.5 * Math.exp(x) * ((delta / d) * (delta / d));
    }

    private PFunction<Double, Double> makef(final double delta, final double v, final double a) {
        return new PFunction<Double, Double>() {
            @Override
            public Double run(Double x) {
                return Math.exp(x) * ((delta * delta) - (rd * rd) - v - Math.exp(x)) / (2.0 * (((rd * rd) + v + Math.exp(x)) * ((rd * rd) + v + Math.exp(x)))) - (x - a) / (tau * tau);
            }
        };
    }

    private double newprocedure(double v, double delta) {
        double A = Math.log(Math.pow(vol, 2));
        PFunction<Double, Double> f = makef(delta, v, A);
        double epsilon = 0.0000001;

        double B, k;
        if ((delta * delta) >  (rd * rd) + v){
            B = Math.log((delta * delta) -  (rd * rd) - v);
        } else {
            k = 1;
            while (f.run(A - k * tau) < 0){
                k = k + 1;
            }
            B = A - k * this.tau;
        }

        double fA = f.run(A);
        double fB = f.run(B);

        double C, fC;
        while (Math.abs(B - A) > epsilon){
            C = A + (A - B) * fA /(fB - fA );
            fC = f.run(C);
            if (fC * fB < 0){
                A = B;
                fA = fB;
            } else {
                fA = fA / 2;
            }
            B = C;
            fB = fC;
        }
        return Math.exp(A/2);
    }

    private double newprocedure_mod(double v, double delta) {
        double A = Math.log(vol * vol);
        PFunction<Double, Double> f = this.makef(delta, v, A);
        double epsilon = 0.0000001;

        double B, k;
        if (delta >  (rd * rd) + v){
            B = Math.log(delta -  (rd * rd) - v);
        } else {
            k = 1;
            while (f.run(A - k * tau) < 0){
                k = k + 1;
            }
            B = A - k * tau;
        }

        double fA = f.run(A);
        double fB = f.run(B);

        double C, fC;
        while (Math.abs(B - A) > epsilon){
            C = A + (A - B) * fA /(fB - fA );
            fC = f.run(C);
            if (fC * fB < 0){
                A = B;
                fA = fB;
            } else {
                fA = fA / 2;
            }
            B = C;
            fB = fC;
        }
        return Math.exp(A/2);
    }

    private double oldprocedure_simple(double v, double delta) {
        double i = 0;
        double a = Math.log(vol * vol);
        double tau = this.tau;
        double x0 = a;
        double x1 = 0;
        double d,h1,h2;

        while (Math.abs(x0 - x1) > 0.00000001){
            // New iteration, so x(i) becomes x(i-1)
            x0 = x1;
            d = (rating * rating) + v + Math.exp(x0);
            h1 = -(x0 - a) / (tau * tau) - 0.5 * Math.exp(x0) / d + 0.5 * Math.exp(x0) * ((delta / d) * (delta / d));
            h2 = -1 / (tau * tau) - 0.5 * Math.exp(x0) * ((rating * rating) + v) / (d * d) + 0.5 * (delta * delta) * Math.exp(x0) * ((rating * rating) + v - Math.exp(x0)) / (d * d * d);
            x1 = x0 - (h1 / h2);
        }

        return Math.exp(x1 / 2);
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
                .append("tau", tau)
                .append("rating", rating)
                .append("rd", rd)
                .append("vol", vol)
                .append("advRanks", advRanks)
                .append("advRds", advRds)
                .append("outcomes", outcomes)
                .append("lastUpdate", lastUpdate);
    }

    public static Rank fromDocument(Document document) {
        Rank rank = new Rank();
        rank.tau = document.getDouble("tau");
        rank.rating = document.getDouble("rating");
        rank.rd = document.getDouble("rd");
        rank.vol = document.getDouble("vol");
        rank.lastUpdate = document.getLong("lastUpdate") == null ? 0 : document.getLong("lastUpdate");

        rank.advRanks = document.get("advRanks", List.class);
        rank.advRds = document.get("advRds", List.class);
        rank.outcomes = document.get("outcomes", List.class);

        return rank;
    }
}
