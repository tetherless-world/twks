package edu.rpi.tw.twks.api;

import org.apache.jena.query.ReadWrite;

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
public interface Twks extends AdministrationApi, ChangeObservableApi, GetAssertionsApi, NanopublicationCrudApi, NanopublicationCrudObservableApi {
    /**
     * Begin a new transaction on the store.
     *
     * @see TwksTransaction for use information.
     */
    TwksTransaction beginTransaction(ReadWrite readWrite);


}
