package edu.rpi.tw.twks.api;

import org.apache.jena.query.ReadWrite;

import java.nio.file.Path;

/**
 * Tetherless World knowledge store API.
 * <p>
 * This is the entry point to all store operations, which are defined by parent interfaces.
 * <p>
 * The nanopublication CRUD operations can be executed in two ways:
 * 1) Within a transaction, by first calling beginTransaction and then invoking the operations on the transaction. This makes it possible to do multiple operations within a single store transaction.
 * 2) Directly on this class, which is equivalent to an "auto-commit" transaction: beginTransaction, do operation, end transaction.
 * <p>
 * The SPARQL API can only be accessed within a transaction.
 */
public interface Twks extends BulkReadApi, ChangeObservableApi, NanopublicationCrudApi, NanopublicationCrudObservableApi {
    /**
     * Begin a new transaction on the store.
     *
     * @see TwksTransaction for use information.
     */
    TwksTransaction beginTransaction(ReadWrite readWrite);

    /**
     * Dump the contents of the store to a directory, one nanopublication per file in .trig format.
     *
     * @param directoryPath directory to which to write nanopublication .trig files
     */
    void dump(Path directoryPath);
}
