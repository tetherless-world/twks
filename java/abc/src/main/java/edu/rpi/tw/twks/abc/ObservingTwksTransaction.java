package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps a TwksTransaction, recording operations and then replaying them to observers if/when the transaction commit.
 */
final class ObservingTwksTransaction extends ForwardingTwksTransaction {
    private final List<ObservedOperation> observedOperations = new ArrayList<>();
    private final TwksObservers observers;

    ObservingTwksTransaction(final TwksTransaction delegate, final TwksObservers observers) {
        super(delegate);
        this.observers = checkNotNull(observers);
    }

    @Override
    public final void abort() {
        delegate().abort();
        observedOperations.clear();
    }

    @Override
    public final void commit() {
        delegate().commit();
        observedOperations.forEach(operation -> operation.notifyObservers(observers));
        observedOperations.clear();
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final DeleteNanopublicationResult result = delegate().deleteNanopublication(uri);
        observedOperations.add(new DeleteNanopublicationObservedOperation(uri));
        return result;
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final PutNanopublicationResult result = delegate().putNanopublication(nanopublication);
        observedOperations.add(new PutNanopublicationObservedOperation(nanopublication));
        return result;
    }

    private abstract static class ObservedOperation {
        abstract void notifyObservers(TwksObservers observers);
    }

    private final static class DeleteNanopublicationObservedOperation extends ObservedOperation {
        private final Uri nanopublicationUri;

        DeleteNanopublicationObservedOperation(final Uri nanopublicationUri) {
            this.nanopublicationUri = checkNotNull(nanopublicationUri);
        }

        @Override
        void notifyObservers(final TwksObservers observers) {
            observers.onDeleteNanopublication(nanopublicationUri);
        }
    }

    private final static class PutNanopublicationObservedOperation extends ObservedOperation {
        private final Nanopublication nanopublication;

        PutNanopublicationObservedOperation(final Nanopublication nanopublication) {
            this.nanopublication = checkNotNull(nanopublication);
        }

        @Override
        void notifyObservers(final TwksObservers observers) {
            observers.onPutNanopublication(nanopublication);
        }
    }
}
