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
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class TwksObservers implements ChangeTwksObserver, DeleteNanopublicationTwksObserver, PutNanopublicationTwksObserver {
    private final static Logger logger = LoggerFactory.getLogger(TwksObservers.class);
    private final Set<TwksObserverRegistrationImpl<ChangeTwksObserver>> changeObserverRegistrations = new HashSet<>();
    private final Set<TwksObserverRegistrationImpl<DeleteNanopublicationTwksObserver>> deleteNanopublicationObserverRegistrations = new HashSet<>();
    private final Set<TwksObserverRegistrationImpl<PutNanopublicationTwksObserver>> putNanopublicationObserverRegistrations = new HashSet<>();
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

    public final TwksObserverRegistration registerChangeObserver(final ChangeTwksObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, changeObserverRegistrations);
    }

    public final TwksObserverRegistration registerDeleteNanopublicationObserver(final DeleteNanopublicationTwksObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, deleteNanopublicationObserverRegistrations);
    }

    public final TwksObserverRegistration registerPutNanopublicationObserver(final PutNanopublicationTwksObserver observer) {
        return new TwksObserverRegistrationImpl<>(observer, putNanopublicationObserverRegistrations);
    }

    @Override
    public void onChange(final Twks twks) {
        checkState(twks == this.twks);
        invokeObservers(observer -> observer.onChange(twks), changeObserverRegistrations);
    }

    @Override
    public final void onDeleteNanopublication(final Twks twks, final Uri nanopublicationUri) {
        checkState(twks == this.twks);
        invokeObservers(observer -> observer.onDeleteNanopublication(twks, nanopublicationUri), deleteNanopublicationObserverRegistrations);
    }

    @Override
    public final void onPutNanopublication(final Twks twks, final Nanopublication nanopublication) {
        checkState(twks == this.twks);
        invokeObservers(observer -> observer.onPutNanopublication(twks, nanopublication), putNanopublicationObserverRegistrations);
    }

    private <ObserverT extends TwksObserver> void invokeObservers(final Consumer<ObserverT> invoker, final Set<TwksObserverRegistrationImpl<ObserverT>> registrations) {
        for (final TwksObserverRegistrationImpl<ObserverT> observerRegistration : registrations) {
            if (observerRegistration.getObserver() instanceof AsynchronousTwksObserver) {
                getAsynchronousObserverExecutorService().submit(() -> invoker.accept(observerRegistration.getObserver()));
            } else {
                invoker.accept(observerRegistration.getObserver());
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
