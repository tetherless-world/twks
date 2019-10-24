package edu.rpi.tw.twks.nanopub;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationPart {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationPart.class);
    private final Model model;
    private final Uri name;

    public NanopublicationPart(Model model, final Uri name) {
        checkNotNull(model);
        Graph graph = model.getGraph();
        if (graph instanceof GraphWrapper) {
            graph = ((GraphWrapper) graph).get();
        }
        if (!(graph instanceof GraphMem)) {
            logger.debug("nanopublication part is not backed by memory ({}), copying", graph.getClass().getCanonicalName());
            final Model modelCopy = ModelFactory.createDefaultModel();
            modelCopy.add(model);
            model = modelCopy;
        }
        this.model = model;
        this.name = checkNotNull(name);
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
