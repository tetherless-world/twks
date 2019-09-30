package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import org.apache.jena.rdf.model.Model;

final class NanopublicationImpl implements Nanopublication {
    private final Model assertion;
    private final Model provenance;
    private final Model publicationInfo;
    private final String uri;

    NanopublicationImpl(final Model assertion, final Model provenance, final Model publicationInfo, final String uri) {
        this.assertion = assertion;
        this.provenance = provenance;
        this.publicationInfo = publicationInfo;
        this.uri = uri;
    }

    @Override
    public final Model getAssertion() {
        return assertion;
    }

    @Override
    public final Model getProvenance() {
        return provenance;
    }

    @Override
    public final Model getPublicationInfo() {
        return publicationInfo;
    }

    @Override
    public final String getUri() {
        return uri;
    }
}
