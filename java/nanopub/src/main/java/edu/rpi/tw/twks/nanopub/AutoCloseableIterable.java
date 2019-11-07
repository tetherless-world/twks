package edu.rpi.tw.twks.nanopub;

public interface AutoCloseableIterable<T> extends AutoCloseable, Iterable<T> {
    @Override
    void close();
}
