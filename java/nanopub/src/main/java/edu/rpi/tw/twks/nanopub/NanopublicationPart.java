package edu.rpi.tw.twks.nanopub;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.mem.GraphInMemory;
import org.apache.jena.sparql.graph.GraphWrapper;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationPart {
    private final Model model;
    private final Uri name;

    public NanopublicationPart(final Model model, final Uri name) {
        this.model = checkModelType(model);
        this.name = checkNotNull(name);
    }

    static Model checkModelType(final Model model) {
        checkNotNull(model);
        Graph graph = model.getGraph();
        if (graph instanceof GraphWrapper) {
            graph = ((GraphWrapper) graph).get();
        }
        if (!(graph instanceof GraphMem) && !(graph instanceof GraphInMemory)) {
            throw new IllegalStateException(String.format("nanopublication must be backed by memory so that changes are not persist, not %s", graph.getClass().getCanonicalName()));
        }
        return model;
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

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("name", getName()).toString();
    }
}
