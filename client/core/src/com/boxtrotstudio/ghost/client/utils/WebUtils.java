package com.boxtrotstudio.ghost.client.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtils {
    private static final Pattern EMAIL_REGEX = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    public static boolean isValidEmail(String email) {
        Matcher m = EMAIL_REGEX.matcher(email);

        return m.find();
    }

    public static List<String> readContentsToList(URL url) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line);

        reader.close();
        return lines;
    }

    public static String[] readContentsToArray(URL url) throws IOException {
        List<String> lines = readContentsToList(url);
        return lines.toArray(new String[lines.size()]);
    }

    public static String readContentsToString(URL url) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
        StringBuilder content = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null)
            content.append(line);

        return content.toString();
    }

    @NotNull
    public static String postToURL(@NotNull URL url, @NotNull String contents) throws IOException {
        String type = "application/x-www-form-urlencoded";

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", type);
        conn.setRequestProperty("Content-Length", String.valueOf(contents.length()));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(contents.getBytes());
        }

        Scanner reader = new Scanner(conn.getInputStream());
        StringBuilder output = new StringBuilder();
        String line;
        while (reader.hasNext()) {
            line = reader.nextLine();
            output.append(line).append("\n");
        }
        reader.close();

        return output.toString();
    }
}
