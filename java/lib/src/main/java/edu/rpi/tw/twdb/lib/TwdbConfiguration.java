package edu.rpi.tw.twdb.lib;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwdbConfiguration {
    private Optional<String> tdb2Location = Optional.empty();

    public final Optional<String> getTdb2Location() {
        return tdb2Location;
    }

    public final TwdbConfiguration setTdb2Location(final Optional<String> tdb2Location) {
        this.tdb2Location = checkNotNull(tdb2Location);
        return this;
    }

    public final TwdbConfiguration setFromSystemProperties() {
        tdb2Location = Optional.ofNullable(System.getProperty("edu.rpi.tw.twdb.tdb2.location"));
        return this;
    }
}
