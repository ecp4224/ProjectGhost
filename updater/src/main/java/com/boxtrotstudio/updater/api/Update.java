package com.boxtrotstudio.updater.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This object represents a single update.
 */
public class Update {
    private int id;
    private Version version;
    private UpdateType type;
    private String[] filesModified;
    private HashMap<String, String> md5;
    private String archiveLocation;
    private long releaseDate;
    private String description;

    /**
     * Get the semantic {@link Version} of this {@link Update}
     * @return The {@link Version} of this update
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Get the {@link UpdateType} of this {@link Update}
     * @return The {@link UpdateType}
     */
    public UpdateType getType() {
        return type;
    }

    /**
     * Get an array of all the files modified. This usually reflects the
     * files contained in the archive
     * @return All the files that were modified in this update
     */
    public String[] getFilesModified() {
        return filesModified;
    }

    /**
     * Get a {@link HashMap} of all the MD5 hash for the files modified. The key of the {@link HashMap} is
     * the file path and the value is the file's MD5
     * @return A {@link HashMap}
     */
    public HashMap<String, String> getMd5() {
        return md5;
    }

    /**
     * Get the location of this update
     * @return The location of this update represented as a {@link String}
     */
    public String getArchiveLocation() {
        return archiveLocation;
    }

    /**
     * Get the location of this update
     * @return The location of this update represented as a {@link URL}
     */
    public URL getArchiveURL() {
        try {
            return new URL(archiveLocation);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get when this update was released as a Unix timestamp
     * @return The time this update was released
     */
    public long getReleaseDate() {
        return releaseDate;
    }

    /**
     * Get a text description of this update
     * @return The text description of this update
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the unique ID for this update
     * @return The unique ID for this update
     */
    public int getId() {
        return id;
    }

    void setVersion(Version version) {
        this.version = version;
    }

    void setType(UpdateType type) {
        this.type = type;
    }

    void setFilesModified(String[] filesModified) {
        this.filesModified = filesModified;
    }

    void setMd5(HashMap<String, String> md5) {
        this.md5 = md5;
    }

    void setArchiveLocation(String archiveLocation) {
        this.archiveLocation = archiveLocation;
    }

    void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Update update = (Update) o;

        if (id != update.id) return false;
        if (releaseDate != update.releaseDate) return false;
        if (version != null ? !version.equals(update.version) : update.version != null) return false;
        if (type != update.type) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(filesModified, update.filesModified)) return false;
        if (md5 != null ? !md5.equals(update.md5) : update.md5 != null) return false;
        if (!archiveLocation.equals(update.archiveLocation)) return false;
        return description.equals(update.description);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + Arrays.hashCode(filesModified);
        result = 31 * result + (md5 != null ? md5.hashCode() : 0);
        result = 31 * result + archiveLocation.hashCode();
        result = 31 * result + (int) (releaseDate ^ (releaseDate >>> 32));
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Update{" +
                "id=" + id +
                ", version=" + version +
                ", type=" + type +
                ", filesModified=" + Arrays.toString(filesModified) +
                ", md5=" + md5 +
                ", archiveLocation='" + archiveLocation + '\'' +
                ", releaseDate=" + releaseDate +
                ", description='" + description + '\'' +
                '}';
    }
}
