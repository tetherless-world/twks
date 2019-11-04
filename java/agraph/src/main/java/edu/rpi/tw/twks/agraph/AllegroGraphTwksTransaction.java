package edu.rpi.tw.twks.agraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphTwksTransaction implements TwksTransaction {
    private final AllegroGraphTwksConfiguration configuration;

    public AllegroGraphTwksTransaction(final AllegroGraphTwksConfiguration configuration, final ReadWrite readWrite) {
        this.configuration = checkNotNull(configuration);
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dump() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model getAssertions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryExecution queryAssertions(final Query query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryExecution queryNanopublications(final Query query) {
        throw new UnsupportedOperationException();
    }
}
