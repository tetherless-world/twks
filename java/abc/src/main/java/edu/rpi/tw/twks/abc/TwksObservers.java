package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class TwksObservers implements DeleteNanopublicationTwksObserver, PutNanopublicationTwksObserver {
    private final static Logger logger = LoggerFactory.getLogger(TwksObservers.class);
    private final Set<DeleteNanopublicationTwksObserverRegistration> deleteNanopublicationObserverRegistrations = new HashSet<>();
    private final Set<PutNanopublicationTwksObserverRegistration> putNanopublicationObserverRegistrations = new HashSet<>();
    private final Twks twks;

    TwksObservers(final Twks twks) {
        this.twks = checkNotNull(twks);
        loadObserverServices(DeleteNanopublicationTwksObserver.class).forEach(observer -> registerDeleteNanopublicationObserver(observer));
        loadObserverServices(PutNanopublicationTwksObserver.class).forEach(observer -> registerPutNanopublicationObserver(observer));
    }

    private <ObserverT extends TwksObserver> ImmutableSet<ObserverT> loadObserverServices(final Class<ObserverT> observerClass) {
        final ServiceLoader<ObserverT> serviceLoader = ServiceLoader.load(observerClass);
        final ImmutableSet.Builder<ObserverT> resultBuilder = ImmutableSet.builder();
        for (final Iterator<ObserverT> observerI = serviceLoader.iterator(); observerI.hasNext(); ) {
            final ObserverT observer = observerI.next();
            resultBuilder.add(observer);
        }
        return resultBuilder.build();
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
            observerRegistration.getObserver().onDeleteNanopublication(twks, nanopublicationUri);
        }
    }

    @Override
    public final void onPutNanopublication(final Twks twks, final Nanopublication nanopublication) {
        checkState(twks == this.twks);
        for (final PutNanopublicationTwksObserverRegistration observerRegistration : putNanopublicationObserverRegistrations) {
            observerRegistration.getObserver().onPutNanopublication(twks, nanopublication);
        }
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
