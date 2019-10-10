package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.graph.GraphWrapper;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationPart {
    private final Model model;
    private final Uri name;

    public NanopublicationPart(final Model model, final Uri name) {
        checkNotNull(model);
        checkNotNull(name);

        Graph graph = model.getGraph();
        if (graph instanceof GraphWrapper) {
            graph = ((GraphWrapper) graph).get();
        }
        if (!(graph instanceof GraphMem)) {
            throw new IllegalStateException(String.format("nanopublication must be backed by an %s, not %s", GraphMem.class.getCanonicalName(), graph.getClass().getCanonicalName()));
        }

        this.model = model;
        this.name = name;
    }

    public final Model getModel() {
        return model;
    }

    public final Uri getName() {
        return name;
    }

    public final boolean isIsomorphicWith(final NanopublicationPart other) {
        if (!getName().equals(other.getName())) {
            return false;
        }

        if (!getModel().isIsomorphicWith(other.getModel())) {
            return false;
        }

        return true;
    }
}
