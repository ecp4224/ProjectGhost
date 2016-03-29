package me.eddiep.ghost.ai;

import me.eddiep.ghost.game.match.world.timeline.EntitySnapshot;
import me.eddiep.ghost.game.match.world.timeline.PlayableSnapshot;
import me.eddiep.ghost.game.match.world.timeline.TimelineCursor;
import me.eddiep.ghost.game.match.world.timeline.WorldSnapshot;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;
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

    public static void main(String[] args) {
        JordanPattern pattern = new JordanPattern();
        pattern.setActivationFunction(new ActivationLOG());
        pattern.setInputNeurons(8);
        pattern.addHiddenLayer(20);
        pattern.setOutputNeurons(2);
        BasicNetwork networkX = (BasicNetwork) pattern.generate();

        //Load data
        List<MatchHistory> matches = getMatches();

        //Generate inputs
        List<Double[]> inputs_ = new ArrayList<>();
        List<Double[]> outputs_ = new ArrayList<>();
        //double[][] inputs = new double[matches.size() * 30 * 60][8];
        //double[][] outputs = new double[matches.size() * 30 * 60][2];

        /*
        0 = currentX
        1 = currentY
        2 = theirX
        3 = theirY
        4 = myLives
        5 = theirLives
        6 = isVisible
        7 = theirVisible
        8 = theirDirection
         */

        double min = Double.MAX_VALUE;
        double max = 0;
        for (MatchHistory match : matches) {
            TimelineCursor cursor = match.getTimeline().createCursor();
            cursor.reset();
            cursor.forwardOneTick();

            HashMap<Short, Double> lives = new HashMap<>();
            HashMap<Short, Vector2f> lastSeen = new HashMap<>();
            HashMap<Short, Vector2f> lastVel = new HashMap<>();
            while (cursor.position() < cursor.getTimeline().size() - 1) {
                WorldSnapshot snapshot = cursor.get();

                if (snapshot.getPlayableChanges() != null) {
                    for (PlayableSnapshot snapshot1 : snapshot.getPlayableChanges()) {
                        lives.put(snapshot1.getID(), (double) snapshot1.getLives());
                    }
                }

                for (EntitySnapshot entitySnapshot : snapshot.getEntitySnapshots()) {
                    if (entitySnapshot == null)
                        continue;
                    if (entitySnapshot.isPlayer()) {
                        Double myLife = lives.get(entitySnapshot.getID());

                        if (entitySnapshot.getAlpha() > 0) {
                            lastSeen.put(entitySnapshot.getID(), new Vector2f(entitySnapshot.getX(), entitySnapshot.getY()));
                            lastVel.put(entitySnapshot.getID(), new Vector2f(entitySnapshot.getVelX(), entitySnapshot.getVelY()));
                        }

                        for (EntitySnapshot entitySnapshot1 : snapshot.getEntitySnapshots()) {
                            if (entitySnapshot1 == null)
                                continue;

                            if (entitySnapshot1.isPlayer() && entitySnapshot1.getID() != entitySnapshot.getID()) {
                                Vector2f pos;
                                Vector2f vel;
                                if (entitySnapshot1.getAlpha() == 0f) {
                                    //The opponent is invisible
                                    //But I'm visible
                                    if (!lastSeen.containsKey(entitySnapshot1.getID()))
                                        break;

                                    pos = lastSeen.get(entitySnapshot1.getID());
                                    vel = lastVel.get(entitySnapshot1.getID());
                                } else {
                                    pos = new Vector2f(entitySnapshot1.getX(), entitySnapshot1.getY());
                                    vel = new Vector2f(entitySnapshot1.getVelX(), entitySnapshot1.getVelY());
                                }

                                double angle = Math.atan2(vel.y, vel.x);

                                Double[] input = new Double[] {
                                        Double.valueOf(entitySnapshot.getX()),
                                        Double.valueOf(entitySnapshot.getY()),
                                        Double.valueOf(pos.x),
                                        Double.valueOf(pos.y),
                                        myLife,
                                        lives.get(entitySnapshot1.getID()),
                                        Double.valueOf(entitySnapshot.getAlpha()),
                                        Double.valueOf(entitySnapshot1.getAlpha()),
                                        Double.valueOf(angle)
                                };

                                WorldSnapshot future = getNextTick(cursor);

                                for (EntitySnapshot entitySnapshot2 : future.getEntitySnapshots()) {
                                    if (entitySnapshot2 == null)
                                        continue;

                                    if (entitySnapshot2.getID() == entitySnapshot.getID()) {
                                        Double[] output;
                                        if (entitySnapshot2.hasTarget()) {
                                            Vector2f vel2 = new Vector2f(entitySnapshot2.getTargetX(), entitySnapshot2.getTargetY());

                                            vel2.x /= 1024f;
                                            vel2.y /= 720f;

                                            output = new Double[]{
                                                    Double.valueOf(vel.x),
                                                    Double.valueOf(vel.y)
                                            };
                                            inputs_.add(input);
                                            outputs_.add(output);
                                            break;
                                        } /*else {
                                            output = new Double[] {
                                                    0.0,
                                                    0.0
                                            };
                                        }*/


                                    }
                                }
                            }
                        }
                    }
                }

                cursor.forwardOneTick();
            }
        }

        System.out.println(min + " - " + max);

        Random random = new Random();

        double[][] inputs = new double[inputs_.size()][10];
        double[][] outputsX = new double[outputs_.size()][1];
        //double[][] outputsY = new double[outputs_.size()][1];

        for (int i = 0; i < inputs_.size(); i++) {
            if (inputs_.get(i) == null)
                continue;

            Double[] temp = inputs_.get(i);
            double[] array = new double[10];
            double[] array2 = new double[] { outputs_.get(i)[0] };
            //double[] array3 = new double[] { outputs_.get(i)[1] };
            //gay shit
            for (int z = 0; z < temp.length; z++) {
                if (temp[z] == null)
                    array[z] = 0.0;
                else
                    array[z] = temp[z];
            }

            System.arraycopy(array, 0, inputs[i], 0, array.length);
            System.arraycopy(array2, 0, outputsX[i], 0, array2.length);
            //System.arraycopy(array3, 0, outputsY[i], 0, array3.length);
        }

        MLDataSet trainingSetX = new BasicMLDataSet(inputs, outputsX);
        //MLDataSet trainingSetY = new BasicMLDataSet(inputs, outputsY);

        /*final ResilientPropagation trainX = new ResilientPropagation(networkX, trainingSetX);
        //final ResilientPropagation trainY = new ResilientPropagation(networkY, trainingSetY);

        int epoch = 1;

        do {
            trainX.iteration();
            System.out.println("Epoch #" + epoch + " Error: " + trainX.getError());
            epoch++;
        } while (trainX.getError() > 0.3);
        trainX.finishTraining();*/

        EncogPluginLogging1 plugin = new EncogPluginLogging1() {
            @Override
            public int getLogLevel() {
                return 0;
            }

            @Override
            public void log(int i, String s) {
                System.out.println(s);
            }

            @Override
            public void log(int i, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public int getPluginType() {
                return 1;
            }

            @Override
            public int getPluginServiceType() {
                return 1;
            }

            @Override
            public String getPluginName() {
                return "Log shit";
            }

            @Override
            public String getPluginDescription() {
                return "Logs shit";
            }
        };

        Encog.getInstance().registerPlugin(plugin);

        MLDataSet trainingSet = new BasicMLDataSet(inputs, outputsX);

        CalculateScore score = new TrainingSetScore(trainingSet);
        MLTrain trainAlt = new NeuralSimulatedAnnealing(networkX, score, 10, 2, 100);
        MLTrain trainMain = new ResilientPropagation(networkX, trainingSet);

        StopTrainingStrategy stop = new StopTrainingStrategy();
        trainMain.addStrategy(new Greedy());
        trainMain.addStrategy(new HybridStrategy(trainAlt));
        trainMain.addStrategy(stop);

        int epoch = 0;
        while (!stop.shouldStop()) {
            trainMain.iteration();
            System.out.println("Epoch #" + epoch + ", Error: " + trainMain.getError());
            epoch++;
        }

        final StringBuilder result = new StringBuilder();

        for (int layer = 0; layer < networkX.getLayerCount() - 1; layer++) {
            int bias = 0;
            if (networkX.isLayerBiased(layer)) {
                bias = 1;
            }

            for (int fromIdx = 0; fromIdx < networkX.getLayerNeuronCount(layer)
                    + bias; fromIdx++) {
                for (int toIdx = 0; toIdx < networkX.getLayerNeuronCount(layer + 1); toIdx++) {
                    String type1 = "", type2 = "";

                    if (layer == 0) {
                        type1 = "I";
                        type2 = "H" + (layer) + ",";
                    } else {
                        type1 = "H" + (layer - 1) + ",";
                        if (layer == (networkX.getLayerCount() - 2)) {
                            type2 = "O";
                        } else {
                            type2 = "H" + (layer) + ",";
                        }
                    }

                    if( bias ==1 && (fromIdx ==  networkX.getLayerNeuronCount(layer))) {
                        type1 = "bias";
                    } else {
                        type1 = type1 + fromIdx;
                    }

                    result.append(type1).append("-->").append(type2).append(toIdx).append(" : ").append(networkX.getWeight(layer, fromIdx, toIdx)).append("\n");
                }
            }
        }

        System.out.println(result);



        Main.TO_INIT = new Class[] {
                BotQueue.class
        };
        BotQueue.network = networkX;
        //BotQueue.network2 = networkY;
        Main.main(new String[] { "--offline"});
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