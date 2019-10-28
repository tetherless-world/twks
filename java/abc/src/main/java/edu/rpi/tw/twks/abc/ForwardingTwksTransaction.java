package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps another TwksTransaction, forwarding all methods to it.
 * <p>
 * Meant to be extended with selected methods overridden.
 */
public class ForwardingTwksTransaction implements TwksTransaction {
    private final TwksTransaction delegate;

    public ForwardingTwksTransaction(final TwksTransaction delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void abort() {
        delegate.abort();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void commit() {
        delegate.commit();
    }

    protected final TwksTransaction delegate() {
        return delegate;
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
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        return delegate.putNanopublication(nanopublication);
    }

    @Override
    public ImmutableList<PutNanopublicationResult> putNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        return delegate.putNanopublications(nanopublications);
    }

    @Override
    public QueryExecution queryAssertions(final Query query) {
        return delegate.queryAssertions(query);
    }

    @Override
    public QueryExecution queryNanopublications(final Query query) {
        return delegate.queryNanopublications(query);
    }
}
