package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

public interface QuadStoreTransaction {
    void abort();

    void addNamedGraph(final Uri graphName, final Model model);

    void close();

    void commit();

    boolean containsNamedGraph(Uri graphName);

    void deleteAllGraphs();

    void deleteNamedGraph(final Uri graphName);

    Model getNamedGraph(Uri graphName);

    QueryExecution query(Query query);
}
