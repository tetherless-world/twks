package edu.rpi.tw.nanopub;

public final class DuplicateModelNameException extends RuntimeException {
    public DuplicateModelNameException(final String name) {
        super(name);
    }
}
