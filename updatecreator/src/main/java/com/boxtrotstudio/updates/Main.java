package com.boxtrotstudio.updates;

import com.boxtrotstudio.updates.api.Update;
import com.boxtrotstudio.updates.api.UpdateBuilder;
import com.boxtrotstudio.updates.api.UpdateType;
import com.boxtrotstudio.updates.api.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length == 0) {
            String header = "Create and update an update file!\nAt least one command is required.\n";
            String footer = "\nYou must specifiy the location of the current update file, or you can use -s to start a new one";

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("updatecreator", header, getOptions(), footer, true);
            System.exit(-1);
            return;
        }

        Options options = parseArgs(args);
        List<Update> updates;

        if (options.hasOption("s")) {
            updates = new ArrayList<>();
        } else if (!options.hasOption("u")) {
            System.err.println("You must specify the location of the current update file!");
            System.exit(1);
            return;
        } else if (options.hasOption("u")) {
            URL url;
            String location = options.getOption("u").getValue();
            File test = new File(location);
            if (test.exists())
                url = test.toURI().toURL();
            else
                url = new URL(location);

            String json = IOUtils.toString(url.openStream(), Charset.defaultCharset());

            Type t = new TypeToken<ArrayList<Update>>() { }.getType();
            updates = GSON.fromJson(json, t);
        } else {
            System.err.println("You must specify the location of the current update file!");
            System.exit(1);
            return;
        }

        if (options.hasOption("l")) {
            if (updates.size() == 0) {
                System.err.println("You cannot list a new update file!");
                System.exit(3);
                return;
            }

            for (Update u : updates) {
                System.out.println(u);
                System.out.println();
            }
        } else if (options.hasOption("c")) {
            UpdateBuilder builder;
            if (updates.size() == 0) {
                builder = UpdateBuilder.fromVersion(new Version(1, 0, 0));
                builder.withType(UpdateType.NEW);
            }
            else
                builder = UpdateBuilder.fromPreviousUpdate(updates.get(updates.size() - 1));

            if (options.hasOption("d"))
                builder.withDescription(options.getOption("d").getValue());
            if (options.hasOption("a")) {
                File location = new File(options.getOption("a").getValue());
                if (!location.exists()) {
                    System.err.println("File does not exist! \"" + location.getAbsolutePath() + '"');
                    System.exit(2);
                    return;
                }
                builder.withArchive(location);
            }
            if (options.hasOption("b")) {
                String type = options.getOption("b").getValue().toLowerCase();

                switch (type) {
                    case "bugfix":
                    case "bf":
                        builder.bumpBugfix();
                        break;
                    case "minor":
                        builder.bumpMinor();
                        break;
                    case "major":
                        builder.bumpMajor();
                        break;
                    default:
                        System.err.println("Invalid bump value! (Given: " + type + ", Expected: bugfix, minor, or major)");
                        System.exit(4);
                        break;
                }
            }

            Update update = builder.build();
            updates.add(update);

            String prettyJson = GSON.toJson(updates);
            System.out.println(prettyJson);
        } else if (options.hasOption("r")) {
            if (updates.size() == 0) {
                System.err.println("No updates to rollback!");
                System.exit(5);
                return;
            }

            Update update = UpdateBuilder.rollback(updates.get(updates.size() - 1));
            updates.add(update);

            String prettyJson = GSON.toJson(updates);
            System.out.println(prettyJson);
        }
    }

    private static Options parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(getOptions(), args);

        Options output = new Options();
        for (Option o : cmd.getOptions()) {
            output.addOption(o);
        }

        return output;
    }

    private static Options getOptions() {
        Options options = new Options();

        Option startFile = new Option("s", false, "[command] Start a new update file, this should be followed by the -c command");
        Option currentFile = new Option("u", true, "[command] The current location of update file");
        Option list = new Option("l", false, "[command] List all updates from current file");
        Option build = new Option("c", false, "[command] Create a new update to add to the current file. This should be followed by options");
        Option rollback = new Option("r", false, "[command] Create an update that rollsback the latest update");

        //Build Options
        Option description = new Option("d", true, "[option] A description for the update");
        Option archiveLocation = new Option("a", true, "[option] The location of the archive to use");
        Option downloadLocation = new Option("d", true, "[option] The download location clients should download the archive from");
        Option bump = new Option("b", true, "[option] Bump one of the 3 version attributes instead of specifying a version (ex; -b major)");

        options.addOption(startFile);
        options.addOption(downloadLocation);
        options.addOption(currentFile);
        options.addOption(list);
        options.addOption(build);
        options.addOption(rollback);

        options.addOption(description);
        options.addOption(archiveLocation);
        options.addOption(bump);

        return options;
    }
}
