package edu.rpi.tw.twks.abc;

import java.util.Iterator;

public interface AutoCloseableIterator<T> extends AutoCloseable, Iterator<T> {
    @Override
    void close();
}
