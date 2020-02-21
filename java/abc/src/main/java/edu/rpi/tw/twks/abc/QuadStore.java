package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.Model;

import java.util.Set;

public interface QuadStore {
    void addNamedGraph(final Uri name, final Model model);

    void deleteAllGraphs();

    void deleteNamedGraphs(final Set<Uri> graphNames);

    Model getNamedGraphs(Set<Uri> graphNames);

    boolean headNamedGraph(Uri graphName);
}
