package me.eddiep.ghost.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebUtils {

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
}
