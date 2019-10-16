package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.*;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class TwksObservers implements DeleteNanopublicationTwksObserver, PutNanopublicationTwksObserver {
    private final static Logger logger = LoggerFactory.getLogger(TwksObservers.class);
    private final Set<DeleteNanopublicationTwksObserverRegistration> deleteNanopublicationObserverRegistrations = new HashSet<>();
    private final Set<PutNanopublicationTwksObserverRegistration> putNanopublicationObserverRegistrations = new HashSet<>();
    private final Twks twks;
    private @Nullable
    ExecutorService asynchronousObserverExecutorService = null;

    TwksObservers(final Twks twks) {
        this.twks = checkNotNull(twks);
    }

    private synchronized ExecutorService getAsynchronousObserverExecutorService() {
        if (asynchronousObserverExecutorService == null) {
            asynchronousObserverExecutorService = Executors.newWorkStealingPool();
        }
        return asynchronousObserverExecutorService;
    }

    public final TwksObserverRegistration registerDeleteNanopublicationObserver(final DeleteNanopublicationTwksObserver observer) {
        final DeleteNanopublicationTwksObserverRegistration registration = new DeleteNanopublicationTwksObserverRegistration(observer);
        deleteNanopublicationObserverRegistrations.add(registration);
        return registration;
    }

    public final TwksObserverRegistration registerPutNanopublicationObserver(final PutNanopublicationTwksObserver observer) {
        final PutNanopublicationTwksObserverRegistration registration = new PutNanopublicationTwksObserverRegistration(observer);
        putNanopublicationObserverRegistrations.add(registration);
        return registration;
    }

    @Override
    public final void onDeleteNanopublication(final Twks twks, final Uri nanopublicationUri) {
        checkState(twks == this.twks);
        for (final DeleteNanopublicationTwksObserverRegistration observerRegistration : deleteNanopublicationObserverRegistrations) {
            if (observerRegistration.getObserver() instanceof AsynchronousTwksObserver) {
                getAsynchronousObserverExecutorService().submit(() -> invokeDeleteNanonpublicationObserver(nanopublicationUri, observerRegistration.getObserver()));
            } else {
                invokeDeleteNanonpublicationObserver(nanopublicationUri, observerRegistration.getObserver());
            }
        }
    }

    @Override
    public final void onPutNanopublication(final Twks twks, final Nanopublication nanopublication) {
        checkState(twks == this.twks);
        for (final PutNanopublicationTwksObserverRegistration observerRegistration : putNanopublicationObserverRegistrations) {
            if (observerRegistration.getObserver() instanceof AsynchronousTwksObserver) {
                getAsynchronousObserverExecutorService().submit(() -> invokePutNanopublicationObserver(nanopublication, observerRegistration.getObserver()));
            } else {
                invokePutNanopublicationObserver(nanopublication, observerRegistration.getObserver());
            }
        }
    }

    private void invokeDeleteNanonpublicationObserver(final Uri nanopublicationUri, final DeleteNanopublicationTwksObserver observer) {
        observer.onDeleteNanopublication(twks, nanopublicationUri);
    }

    private void invokePutNanopublicationObserver(final Nanopublication nanopublication, final PutNanopublicationTwksObserver observer) {
        observer.onPutNanopublication(twks, nanopublication);
    }

    private abstract class AbstractTwksObserverRegistration<ObserverT extends TwksObserver> implements TwksObserverRegistration {
        private final ObserverT observer;

        protected AbstractTwksObserverRegistration(final ObserverT observer) {
            this.observer = checkNotNull(observer);
        }

        public final ObserverT getObserver() {
            return observer;
        }
    }

    private final class DeleteNanopublicationTwksObserverRegistration extends AbstractTwksObserverRegistration<DeleteNanopublicationTwksObserver> {
        private DeleteNanopublicationTwksObserverRegistration(final DeleteNanopublicationTwksObserver observer) {
            super(observer);
        }

        @Override
        public final void unregister() {
            deleteNanopublicationObserverRegistrations.remove(this);
        }
    }

    private final class PutNanopublicationTwksObserverRegistration extends AbstractTwksObserverRegistration<PutNanopublicationTwksObserver> {
        private PutNanopublicationTwksObserverRegistration(final PutNanopublicationTwksObserver observer) {
            super(observer);
        }

        @Override
        public final void unregister() {
            putNanopublicationObserverRegistrations.remove(this);
        }
    }
}
