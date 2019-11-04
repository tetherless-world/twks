package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.api.observer.ChangeObserver;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTwks<TwksConfigurationT extends TwksConfiguration> implements Twks {
    private final TwksConfigurationT configuration;
    private final TwksObservers observers = new TwksObservers(this);

    protected AbstractTwks(final TwksConfigurationT configuration) {
        this.configuration = checkNotNull(configuration);
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
            return transaction.getAssertions();
        }
    }

    protected final TwksConfigurationT getConfiguration() {
        return configuration;
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
            return transaction.getOntologyAssertions(ontologyUris);
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
