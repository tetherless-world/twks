package edu.rpi.tw.twks.core;

import org.apache.jena.query.ReadWrite;

/**
 * Tetherless World knowledge store API.
 * <p>
 * This is the entry point to all store operations.
 */
public interface Twks {
    /**
     * Begin a new transaction on the store.
     * <p>
     * All operations on the store must start with a transaction, which is usually begun in a try-with-resources block.
     *
     * @see TwksTransaction for use information.
     */
    TwksTransaction beginTransaction(ReadWrite readWrite);
}
