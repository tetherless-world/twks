package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.NamedModel;
import edu.rpi.tw.twdb.api.Nanopublication;

final class NanopublicationImpl implements Nanopublication {
    private final NamedModel assertion;
    private final NamedModel provenance;
    private final NamedModel publicationInfo;
    private final String uri;

    NanopublicationImpl(final NamedModel assertion, final NamedModel provenance, final NamedModel publicationInfo, final String uri) {
        this.assertion = assertion;
        this.provenance = provenance;
        this.publicationInfo = publicationInfo;
        this.uri = uri;
    }

    @Override
    public final NamedModel getAssertion() {
        return assertion;
    }

    @Override
    public final NamedModel getProvenance() {
        return provenance;
    }

    @Override
    public final NamedModel getPublicationInfo() {
        return publicationInfo;
    }

    @Override
    public final String getUri() {
        return uri;
    }
}
