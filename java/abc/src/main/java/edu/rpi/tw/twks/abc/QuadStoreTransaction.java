package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.DoesNotExistException;

/**
 * Interface for transactions on quad stores, with a minimal set of operations needed by TwksTransaction.
 * <p>
 * The names and semantics are adapted from Jena Dataset.
 */
public interface QuadStoreTransaction extends AutoCloseable {
    /**
     * Abort the transaction.
     */
    void abort();

    /**
     * Add a named graph to the quad store.
     * <p>
     * If there is already a graph in the store with that name, replace it.
     * If there isn't, create it.
     */
    void addNamedGraph(final Uri graphName, final Model model);

    /**
     * Close the transaction.
     */
    @Override
    void close();

    /**
     * Commit the transaction.
     */
    void commit();

    /**
     * Check whether the quad store contains a named graph.
     */
    boolean containsNamedGraph(Uri graphName);

    /**
     * Get a named graph's Model. Throws an exception if the named graph does not exist in the store.
     */
    Model getNamedGraph(Uri graphName) throws DoesNotExistException;

    /**
     * Get a named graph's Model or create the named graph if it doesn't exist.
     */
    Model getOrCreateNamedGraph(Uri graphName);

    /**
     * Query over all named graphs in the quad store.
     */
    QueryExecution query(Query query);

    /**
     * Remove all named graphs in the store.
     */
    void removeAllGraphs();

    /**
     * Remove a specific named graph from the store.
     */
    void removeNamedGraph(final Uri graphName);
}
