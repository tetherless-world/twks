package edu.rpi.tw.twks.nanopub;

public final class DuplicateNanopublicationPartName extends RuntimeException {
    public DuplicateNanopublicationPartName(final String name) {
        super(name);
    }
}
