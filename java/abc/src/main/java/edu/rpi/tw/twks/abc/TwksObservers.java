package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.ChangeObservableApi;
import edu.rpi.tw.twks.api.NanopublicationCrudObservableApi;
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
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class TwksObservers implements ChangeObservableApi, DeleteNanopublicationObserver, NanopublicationCrudObservableApi, PutNanopublicationObserver {
    private final static Logger logger = LoggerFactory.getLogger(TwksObservers.class);
    private final Set<TwksObserverRegistrationImpl<ChangeObserver>> changeObserverRegistrations = new HashSet<>();
    private final Set<TwksObserverRegistrationImpl<DeleteNanopublicationObserver>> deleteNanopublicationObserverRegistrations = new HashSet<>();
    private final Set<TwksObserverRegistrationImpl<PutNanopublicationObserver>> putNanopublicationObserverRegistrations = new HashSet<>();
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

    @Override
    public final TwksObserverRegistration registerChangeObserver(final ChangeObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, changeObserverRegistrations);
    }

    @Override
    public final TwksObserverRegistration registerDeleteNanopublicationObserver(final DeleteNanopublicationObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, deleteNanopublicationObserverRegistrations);
    }

    @Override
    public final TwksObserverRegistration registerPutNanopublicationObserver(final PutNanopublicationObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, putNanopublicationObserverRegistrations);
    }

    private void onChange(final Twks twks) {
        checkState(twks == this.twks);
        invokeObservers(observer -> observer.onChange(), changeObserverRegistrations);
    }

    @Override
    public final void onDeleteNanopublication(final Uri nanopublicationUri) {
        invokeObservers(observer -> observer.onDeleteNanopublication(nanopublicationUri), deleteNanopublicationObserverRegistrations);
        onChange(twks);
    }

    @Override
    public final void onPutNanopublication(final Nanopublication nanopublication) {
        invokeObservers(observer -> observer.onPutNanopublication(nanopublication), putNanopublicationObserverRegistrations);
        onChange(twks);
    }

    private <ObserverT extends TwksObserver> void invokeObservers(final Consumer<ObserverT> invoker, final Set<TwksObserverRegistrationImpl<ObserverT>> registrations) {
        for (final TwksObserverRegistrationImpl<ObserverT> observerRegistration : registrations) {
            if (observerRegistration.getObserver() instanceof AsynchronousTwksObserver) {
                getAsynchronousObserverExecutorService().submit(() -> {
                    try {
                        invoker.accept(observerRegistration.getObserver());
                    } catch (final RuntimeException e) {
                        logger.error("uncaught exception in observer: {}", e);
                    }
                });
            } else {
                try {
                    invoker.accept(observerRegistration.getObserver());
                } catch (final RuntimeException e) {
                    logger.error("uncaught exception in observer: {}", e);
                }
            }
        }
    }

    private final static class TwksObserverRegistrationImpl<ObserverT extends TwksObserver> implements TwksObserverRegistration {
        private final ObserverT observer;
        private final Set<TwksObserverRegistrationImpl<ObserverT>> registrations;

        protected TwksObserverRegistrationImpl(final ObserverT observer, final Set<TwksObserverRegistrationImpl<ObserverT>> registrations) {
            this.observer = checkNotNull(observer);
            this.registrations = checkNotNull(registrations);
            registrations.add(this);
        }

        public final ObserverT getObserver() {
            return observer;
        }

        @Override
        public void unregister() {
            registrations.remove(this);
        }
    }
}
