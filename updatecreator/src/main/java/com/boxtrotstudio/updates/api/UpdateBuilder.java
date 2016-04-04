package com.boxtrotstudio.updates.api;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UpdateBuilder {
    private Update previousUpdate;
    private String description;
    private List<String> filesModified;
    private HashMap<String, String> md5;
    private Version version;
    private UpdateType type;
    private String download;

    public static UpdateBuilder fromPreviousUpdate(Update update) {
        UpdateBuilder builder = new UpdateBuilder();
        builder.previousUpdate = update;

        if (update != null) {
            builder.version = new Version(update.getVersion());
        } else {
            builder.version = new Version(1, 0, 0);
        }

        return builder;
    }

    public static UpdateBuilder fromVersion(Version version) {
        UpdateBuilder builder = new UpdateBuilder();
        builder.previousUpdate = null;
        builder.version = version;

        return builder;
    }

    public static Update rollback(Update update) {
        UpdateBuilder builder = fromPreviousUpdate(update);
        builder.bumpMinor();
        builder.withType(UpdateType.ROLLBACK);
        builder.filesModified = Arrays.asList(update.getFilesModified());
        builder.md5 = update.getMd5();

        return builder.build();
    }

    private UpdateBuilder() { }

    public UpdateBuilder bumpBugfix() {
        version.setBugfix(version.getBugfix() + 1);
        type = UpdateType.BUGFIX;
        return this;
    }

    public UpdateBuilder bumpMinor() {
        version.setBugfix(0);
        version.setMinor(version.getMinor() + 1);
        type = UpdateType.MINOR;
        return this;
    }

    public UpdateBuilder bumpMajor() {
        version.setBugfix(0);
        version.setMinor(0);
        version.setMajor(version.getMajor() + 1);
        type = UpdateType.MAJOR;
        return this;
    }

    public UpdateBuilder withVersion(Version version) {
        this.version = version;
        return this;
    }

    public UpdateBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public UpdateBuilder withArchive(File archive) throws IOException {
        ZipFile zip = new ZipFile(archive);

        filesModified = new ArrayList<String>();
        md5 = new HashMap<String, String>();

        Enumeration<? extends ZipEntry> wat = zip.entries();
        while (wat.hasMoreElements()) {
            ZipEntry entry = wat.nextElement();

            String md5 = DigestUtils.md5Hex(zip.getInputStream(entry));

            filesModified.add(entry.getName());
            this.md5.put(entry.getName(), md5);
        }

        zip.close();

        return this;
    }

    public UpdateBuilder withType(UpdateType type) {
        this.type = type;
        return this;
    }

    public UpdateBuilder withDownloadLocation(String location) {
        this.download = location;
        return this;
    }

    public Update build() {
        Update update = new Update();
        update.setVersion(version);

        if (this.previousUpdate != null)
            update.setId(previousUpdate.getId() + 1);
        else
            update.setId(1);

        if (type == null)
            update.setType(UpdateType.UNKNOWN);
        else
            update.setType(type);

        if (description != null)
            update.setDescription(description);
        else
            update.setDescription("No description specified");

        if (filesModified != null)
            update.setFilesModified(filesModified.toArray(new String[filesModified.size()]));
        else
            update.setFilesModified(new String[0]);

        if (md5 != null)
            update.setMd5(md5);
        else
            update.setMd5(new HashMap<String, String>());

        if (download != null)
            update.setArchiveLocation(download);
        else
            update.setArchiveLocation("");

        update.setReleaseDate(System.currentTimeMillis());
        return update;
    }
}
