package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.api.TwksVersion;
import edu.rpi.tw.twks.api.observer.ChangeObserver;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;
import edu.rpi.tw.twks.configuration.TwksConfiguration;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTwks<TwksConfigurationT extends TwksConfiguration, TwksMetricsT extends AbstractTwksMetrics> implements Twks {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTwks.class);
    private final TwksConfigurationT configuration;
    private final TwksMetricsT metrics;
    private final TwksObservers observers = new TwksObservers(this);

    protected AbstractTwks(final TwksConfigurationT configuration, final TwksMetricsT metrics) {
        this.configuration = checkNotNull(configuration);
        this.metrics = checkNotNull(metrics);

        if (configuration.getGeoSparqlConfiguration().getEnable()) {
            GeoSPARQLConfig.setupMemoryIndex();
            logger.info("enabling GeoSPARQL memory index");
        }
    }

    protected abstract TwksTransaction _beginTransaction(ReadWrite readWrite);

    @Override
    public final TwksTransaction beginTransaction(final ReadWrite readWrite) {
        return new ObservingTwksTransaction(this, _beginTransaction(readWrite), observers);
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.WRITE)) {
            final DeleteNanopublicationResult result = transaction.deleteNanopublication(uri);
            if (result == DeleteNanopublicationResult.DELETED) {
                transaction.commit();
            } else {
                transaction.abort();
            }
            return result;
        }
    }

    @Override
    public final void deleteNanopublications() {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.WRITE)) {
            transaction.deleteNanopublications();
            transaction.commit();
        }
    }

    @Override
    public ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.WRITE)) {
            final ImmutableList<DeleteNanopublicationResult> results = transaction.deleteNanopublications(uris);
            transaction.commit();
            return results;
        }
    }

    @Override
    public final void dump() throws IOException {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            transaction.dump();
        }
    }

    @Override
    public final Model getAssertions() {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            // Create a copy of the assertions Model, so operations on the reference won't fail because they're outside a transaction.
            final Model reference = transaction.getAssertions();
            final Model copy = ModelFactory.createDefaultModel();
            copy.add(reference);
            return copy;
        }
    }

    public final TwksConfigurationT getConfiguration() {
        return configuration;
    }

    public final TwksMetricsT getMetrics() {
        return metrics;
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            return transaction.getNanopublication(uri);
        }
    }

    protected final TwksObservers getObservers() {
        return observers;
    }

    @Override
    public Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            // Create a copy of the assertions Model, so operations on the reference won't fail because they're outside a transaction.
            final Model reference = transaction.getOntologyAssertions(ontologyUris);
            final Model copy = ModelFactory.createDefaultModel();
            copy.add(reference);
            return copy;
        }
    }

    @Override
    public final TwksVersion getVersion() {
        return TwksLibraryVersion.getInstance();
    }

    @Override
    public boolean isEmpty() {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            return transaction.isEmpty();
        }
    }

    @Override
    public ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.WRITE)) {
            final ImmutableList<PutNanopublicationResult> results = transaction.postNanopublications(nanopublications);
            transaction.commit();
            return results;
        }
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.WRITE)) {
            final PutNanopublicationResult result = transaction.putNanopublication(nanopublication);
            transaction.commit();
            return result;
        }
    }

    @Override
    public final TwksObserverRegistration registerChangeObserver(final ChangeObserver observer) {
        return observers.registerChangeObserver(observer);
    }

    @Override
    public final TwksObserverRegistration registerDeleteNanopublicationObserver(final DeleteNanopublicationObserver observer) {
        return observers.registerDeleteNanopublicationObserver(observer);
    }

    @Override
    public final TwksObserverRegistration registerPutNanopublicationObserver(final PutNanopublicationObserver observer) {
        return observers.registerPutNanopublicationObserver(observer);
    }
}
