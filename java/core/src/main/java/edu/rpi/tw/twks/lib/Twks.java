package edu.rpi.tw.twks.lib;

import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;

import java.util.Optional;

/**
 * Tetherless World knowledge store API.
 * <p>
 * This is the sole entry point to all store operations.
 */
public interface Twks {
    /**
     * Begin a new transaction on the store.
     *
     * @see TwksTransaction for use information.
     */
    TwksTransaction beginTransaction(ReadWrite readWrite);

    /**
     * Delete a nanopublication.
     * <p>
     * Starts a new transaction and delegates to deleteNanopublication(Uri, TwksTransaction).
     *
     * @param uri URI of the nanopublication
     * @return true if the nanopublication was present, otherwise false
     */
    DeleteNanopublicationResult deleteNanopublication(Uri uri);

    /**
     * Delete a nanopublication with an existing transaction.
     *
     * @param uri         URI of the nanopublication
     * @param transaction existing transaction
     * @return true of the nanopublication was presented, otherwise false
     */
    DeleteNanopublicationResult deleteNanopublication(Uri uri, final TwksTransaction transaction);

    /**
     * Get a nanopublication.
     * <p>
     * Starts a new transaction and delegates to getNanopublication(Uri, TwksTransaction).
     *
     * @param uri URI of the nanopublication.
     * @return Optional.of(the nanopublication) if it exists in the store, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri);

    /**
     * Get a nanopublication.
     *
     * @param uri         URI of the nanopublication
     * @param transaction existing transaction to use
     * @return Optional.of(the nanopublication) if it exists in the store, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri, TwksTransaction transaction);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     * <p>
     * Starts a new transaction and delegates to putNanopublication(Nanopublication, TwksTransaction).
     *
     * @param nanopublication nanopublication to put.
     */
    PutNanopublicationResult putNanopublication(Nanopublication nanopublication);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     *
     * @param nanopublication nanopublication to put
     * @param transaction     existing transaction to use
     */
    PutNanopublicationResult putNanopublication(Nanopublication nanopublication, TwksTransaction transaction);

    /**
     * Query assertion parts of stored nanopublications.
     * <p>
     * See TwksTest for examples on how to use this.
     *
     * @param query       query to execute. This will be augmented by the implementation as needed.
     * @param transaction transaction this query will execute under
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryAssertions(Query query, TwksTransaction transaction);

    /**
     * Query all parts of stored nanopublications (head, assertion, provenance, publication info).
     *
     * @param query       query to execute. This will be augmented by the implementation as needed.
     * @param transaction transaction this query will execute under
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryNanopublications(Query query, TwksTransaction transaction);

    enum DeleteNanopublicationResult {
        DELETED, NOT_FOUND
    }

    enum PutNanopublicationResult {
        CREATED, OVERWROTE
    }
}
