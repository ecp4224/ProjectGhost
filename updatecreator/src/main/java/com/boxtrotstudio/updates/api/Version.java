package com.boxtrotstudio.updates.api;

public class Version {
    private int major;
    private int minor;
    private int bugfix;

    public static Version getHigher(Version version1, Version version2) {
        if (version1.major > version2.major)
            return version1;
        else if (version1.major < version2.major)
            return version2;
        else {
            if (version1.minor > version2.minor)
                return version1;
            else if (version1.minor < version2.minor)
                return version2;
            else {
                if (version1.bugfix > version2.bugfix)
                    return version1;
                else if (version1.bugfix < version2.bugfix)
                    return version2;
                else
                    return null;
            }
        }
    }

    public static Version parseVersion(String string) {
        String[] dots = string.split("\\.");
        int major;
        int minor = 0;
        int bugfix = 0;
        switch (dots.length) {
            case 0:
                throw new NumberFormatException("Could not find major!");
            case 1:
                major = Integer.parseInt(dots[0]);
                break;
            case 2:
                major = Integer.parseInt(dots[0]);
                minor = Integer.parseInt(dots[1]);
                break;
            case 3:
                major = Integer.parseInt(dots[0]);
                minor = Integer.parseInt(dots[1]);
                bugfix = Integer.parseInt(dots[2]);
                break;
            default:
                throw new UnsupportedOperationException("Version can only have 1, 2, or 3 attributes!");
        }

        return new Version(major, minor, bugfix);
    }

    public Version(int major, int minor, int bugfix) {
        this.major = major;
        this.minor = minor;
        this.bugfix = bugfix;
    }

    public Version(Version clone) {
        this.major = clone.major;
        this.minor = clone.minor;
        this.bugfix = clone.bugfix;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBugfix() {
        return bugfix;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setBugfix(int bugfix) {
        this.bugfix = bugfix;
    }

    public boolean isHigherThan(Version version) {
        Version higher = getHigher(this, version);
        return higher != null && higher.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        return major == version.major && minor == version.minor && bugfix == version.bugfix;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + bugfix;
        return result;
    }

    @Override
    public String toString() {
        return major + "." + minor + '.' + bugfix;
    }
}
