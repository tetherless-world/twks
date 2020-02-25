package edu.rpi.tw.twks.cli;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.api.TwksVersion;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class InProcessTwksClient implements TwksClient {
    private final Twks delegate;

    public InProcessTwksClient(final Twks twks) {
        this.delegate = checkNotNull(twks);
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        return delegate.deleteNanopublication(uri);
    }

    @Override
    public final ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        return delegate.deleteNanopublications(uris);
    }

    @Override
    public final void deleteNanopublications() {
        delegate.deleteNanopublications();
    }

    @Override
    public final void dump() throws IOException {
        delegate.dump();
    }

    @Override
    public final Model getAssertions() {
        return delegate.getAssertions();
    }

    @Override
    public final TwksVersion getClientVersion() {
        return delegate.getVersion();
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        return delegate.getNanopublication(uri);
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        return delegate.getOntologyAssertions(ontologyUris);
    }

    @Override
    public final TwksVersion getServerVersion() {
        return delegate.getVersion();
    }

    @Override
    public final ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        return delegate.postNanopublications(nanopublications);
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        return delegate.putNanopublication(nanopublication);
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        final TwksTransaction transaction = delegate.beginTransaction(ReadWrite.READ);
        final QueryExecution queryExecution = transaction.queryAssertions(query);
        return new TransactionalQueryExecution(queryExecution, transaction);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        final TwksTransaction transaction = delegate.beginTransaction(ReadWrite.READ);
        final QueryExecution queryExecution = transaction.queryNanopublications(query);
        return new TransactionalQueryExecution(queryExecution, transaction);
    }

    private final static class TransactionalQueryExecution implements QueryExecution {
        private final QueryExecution delegate;
        private final TwksTransaction transaction;

        private TransactionalQueryExecution(final QueryExecution delegate, final TwksTransaction transaction) {
            this.delegate = delegate;
            this.transaction = transaction;
        }

        @Override
        public void abort() {
            delegate.abort();
        }

        @Override
        public void close() {
            delegate.close();
            transaction.close();
        }

        @Override
        public boolean execAsk() {
            return delegate.execAsk();
        }

        @Override
        public Model execConstruct() {
            return delegate.execConstruct();
        }

        @Override
        public Model execConstruct(final Model model) {
            return delegate.execConstruct(model);
        }

        @Override
        public Dataset execConstructDataset() {
            return delegate.execConstructDataset();
        }

        @Override
        public Dataset execConstructDataset(final Dataset dataset) {
            return delegate.execConstructDataset(dataset);
        }

        @Override
        public Iterator<Quad> execConstructQuads() {
            return delegate.execConstructQuads();
        }

        @Override
        public Iterator<Triple> execConstructTriples() {
            return delegate.execConstructTriples();
        }

        @Override
        public Model execDescribe() {
            return delegate.execDescribe();
        }

        @Override
        public Model execDescribe(final Model model) {
            return delegate.execDescribe(model);
        }

        @Override
        public Iterator<Triple> execDescribeTriples() {
            return delegate.execDescribeTriples();
        }

        @Override
        public JsonArray execJson() {
            return delegate.execJson();
        }

        @Override
        public Iterator<JsonObject> execJsonItems() {
            return delegate.execJsonItems();
        }

        @Override
        public ResultSet execSelect() {
            return delegate.execSelect();
        }

        @Override
        public Context getContext() {
            return delegate.getContext();
        }

        @Override
        public Dataset getDataset() {
            return delegate.getDataset();
        }

        @Override
        public Query getQuery() {
            return delegate.getQuery();
        }

        @Override
        public long getTimeout1() {
            return delegate.getTimeout1();
        }

        @Override
        public long getTimeout2() {
            return delegate.getTimeout2();
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override
        public void setInitialBinding(final QuerySolution querySolution) {
            delegate.setInitialBinding(querySolution);
        }

        @Override
        public void setInitialBinding(final Binding binding) {
            delegate.setInitialBinding(binding);
        }

        @Override
        public void setTimeout(final long l, final TimeUnit timeUnit) {
            delegate.setTimeout(l, timeUnit);
        }

        @Override
        public void setTimeout(final long l) {
            delegate.setTimeout(l);
        }

        @Override
        public void setTimeout(final long l, final TimeUnit timeUnit, final long l1, final TimeUnit timeUnit1) {
            delegate.setTimeout(l, timeUnit, l1, timeUnit1);
        }

        @Override
        public void setTimeout(final long l, final long l1) {
            delegate.setTimeout(l, l1);
        }
    }
}
