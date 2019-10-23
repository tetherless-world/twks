package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.api.observer.ChangeObserver;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;

import java.util.Optional;

public abstract class AbstractTwks implements Twks {
    private final TwksObservers observers = new TwksObservers(this);

    protected final TwksObservers getObservers() {
        return observers;
    }

    @Override
    public final TwksTransaction beginTransaction(final ReadWrite readWrite) {
        return new ObservingTwksTransaction(this, _beginTransaction(readWrite), observers);
    }

    protected abstract TwksTransaction _beginTransaction(ReadWrite readWrite);

    @Override
    public final Model getAssertions() {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            return transaction.getAssertions();
        }
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
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        try (final TwksTransaction transaction = beginTransaction(ReadWrite.READ)) {
            return transaction.getNanopublication(uri);
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
