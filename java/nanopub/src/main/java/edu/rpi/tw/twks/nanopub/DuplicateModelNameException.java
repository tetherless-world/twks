package edu.rpi.tw.twks.nanopub;

public final class DuplicateModelNameException extends RuntimeException {
    public DuplicateModelNameException(final String name) {
        super(name);
    }
}
