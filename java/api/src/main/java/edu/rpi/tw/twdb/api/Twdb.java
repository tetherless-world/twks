package edu.rpi.tw.twdb.api;

import edu.rpi.tw.nanopub.Nanopublication;
import org.apache.jena.query.Dataset;
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
     * Get a Dataset (set of named graphs) that only refers to assertion graphs. A query over the union of this Dataset will query only assertions.
     * <p>
     * You must check whether this Dataset supportsTransactions() and use transactions accordingly.
     * <p>
     * EXPERIMENTAL: This method may be removed to hide Datasets.
     *
     * @return assertions Dataset.
     */
    Dataset getAssertionsDataset();

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
     * Get a Dataset (set of named graphs) that refers to all nanopublications in the database.
     * <p>
     * You must check whether this Dataset supportsTransactions() and use transactions accordingly.
     * <p>
     * EXPERIMENTAL: This method may be removed to hide Datasets.
     *
     * @return nanopublications Dataset.
     */
    Dataset getNanopublicationsDataset();

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
}
