package edu.rpi.tw.twks.cli;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.client.TwksClient;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class InProcessTwksClient implements TwksClient {
    private final Twks delegate;

    public InProcessTwksClient(final Twks twks) {
        this.delegate = checkNotNull(twks);
    }

    @Override
    public DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        return delegate.deleteNanopublication(uri);
    }

    @Override
    public ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        return delegate.deleteNanopublications(uris);
    }

    @Override
    public void dump() throws IOException {
        delegate.dump();
    }

    @Override
    public Model getAssertions() {
        return delegate.getAssertions();
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        return delegate.getNanopublication(uri);
    }

    @Override
    public Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        return delegate.getOntologyAssertions(ontologyUris);
    }

    @Override
    public ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        return delegate.postNanopublications(nanopublications);
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        return delegate.putNanopublication(nanopublication);
    }

    @Override
    public QueryExecution queryAssertions(final Query query) {
        return null;
    }

    @Override
    public QueryExecution queryNanopublications(final Query query) {
        return null;
    }
}
