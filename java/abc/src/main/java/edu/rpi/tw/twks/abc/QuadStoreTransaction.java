package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

/**
 * Interface for transactions on quad stores, with a minimal set of operations needed by TwksTransaction.
 * <p>
 * The names and semantics are adapted from Jena Dataset.
 */
public interface QuadStoreTransaction {
    void abort();

    void addNamedGraph(final Uri graphName, final Model model);

    void close();

    void commit();

    boolean containsNamedGraph(Uri graphName);

    Model getNamedGraph(Uri graphName);

    QueryExecution query(Query query);

    void removeAllGraphs();

    void removeNamedGraph(final Uri graphName);
}
