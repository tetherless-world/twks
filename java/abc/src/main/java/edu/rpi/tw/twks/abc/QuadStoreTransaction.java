package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

import java.util.Set;

public interface QuadStoreTransaction {
    void abort();

    void addNamedGraph(final Uri graphName, final Model model);

    void close();

    void commit();

    void deleteAllGraphs();

    void deleteNamedGraphs(final Set<Uri> graphNames);

    Model getNamedGraph(Uri graphName);

    Model getNamedGraphs(Set<Uri> graphNames);

    boolean headNamedGraph(Uri graphName);

    QueryExecution query(Query query);
}
