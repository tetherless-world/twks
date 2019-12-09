package edu.rpi.tw.twks.api;

import java.util.Objects;

public class TwksVersion {
    private final int incremental, major, minor;

    public TwksVersion(final int incremental, final int major, final int minor) {
        this.incremental = incremental;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof TwksVersion)) {
            return false;
        }
        final TwksVersion that = (TwksVersion) o;
        return incremental == that.incremental &&
                major == that.major &&
                minor == that.minor;
    }

    public final int getIncremental() {
        return incremental;
    }

    public final int getMajor() {
        return major;
    }

    public final int getMinor() {
        return minor;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(incremental, major, minor);
    }

    @Override
    public final String toString() {
        return String.format("%d.%d.%d", major, minor, incremental);
    }
}
