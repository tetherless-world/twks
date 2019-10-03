package edu.rpi.tw.twdb.api;

import edu.rpi.tw.nanopub.Nanopublication;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

/**
 * Tetherless World database API.
 * <p>
 * This is the sole entry point to all database operations.
 */
public interface Twdb {
    /**
     * Begin a new transaction on the database.
     *
     * @see TwdbTransaction for use information.
     */
    TwdbTransaction beginTransaction(ReadWrite readWrite);

    /**
     * Delete a nanopublication.
     * <p>
     * Starts a new transaction and delegates to deleteNanopublication(Uri, TwdbTransaction).
     *
     * @param uri URI of the nanopublication
     * @return true if the nanopublication was present, otherwise false
     */
    boolean deleteNanopublication(Uri uri);

    /**
     * Delete a nanopublication with an existing transaction.
     *
     * @param uri         URI of the nanopublication
     * @param transaction existing transaction
     * @return true of the nanopublication was presented, otherwise false
     */
    boolean deleteNanopublication(Uri uri, final TwdbTransaction transaction);

    /**
     * Get a nanopublication.
     * <p>
     * Starts a new transaction and delegates to getNanopublication(Uri, TwdbTransaction).
     *
     * @param uri URI of the nanopublication.
     * @return Optional.of(the nanopublication) if it exists in the database, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri);

    /**
     * Get a nanopublication.
     *
     * @param uri         URI of the nanopublication
     * @param transaction existing transaction to use
     * @return Optional.of(the nanopublication) if it exists in the database, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri, TwdbTransaction transaction);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     * <p>
     * Starts a new transaction and delegates to putNanopublication(Nanopublication, TwdbTransaction).
     *
     * @param nanopublication nanopublication to put.
     */
    void putNanopublication(Nanopublication nanopublication);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     *
     * @param nanopublication nanopublication to put
     * @param transaction     existing transaction to use
     */
    void putNanopublication(Nanopublication nanopublication, TwdbTransaction transaction);

    /**
     * Query assertion parts of stored nanopublications.
     * <p>
     * See TwdbTest for examples on how to use this.
     *
     * @param query       query to execute. This will be augmented by the implementation as needed.
     * @param transaction transaction this query will execute under
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryAssertions(Query query, TwdbTransaction transaction);

    /**
     * Query all parts of stored nanopublications (head, assertion, provenance, publication info).
     *
     * @param query       query to execute. This will be augmented by the implementation as needed.
     * @param transaction transaction this query will execute under
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryNanopublications(Query query, TwdbTransaction transaction);
}
