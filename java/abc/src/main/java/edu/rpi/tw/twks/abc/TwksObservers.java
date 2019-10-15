package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.Observer;
import edu.rpi.tw.twks.api.observer.ObserverRegistration;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksObservers implements DeleteNanopublicationObserver, PutNanopublicationObserver {
    private final static Logger logger = LoggerFactory.getLogger(TwksObservers.class);
    private final Set<DeleteNanopublicationObserverRegistration> deleteNanopublicationObserverRegistrations = new HashSet<>();
    private final Set<PutNanopublicationObserverRegistration> putNanopublicationObserverRegistrations = new HashSet<>();

    public TwksObservers() {
        loadObserverServices(DeleteNanopublicationObserver.class).forEach(observer -> registerDeleteNanopublicationObserver(observer));
        loadObserverServices(PutNanopublicationObserver.class).forEach(observer -> registerPutNanopublicationObserver(observer));
    }

    private <ObserverT extends Observer> ImmutableSet<ObserverT> loadObserverServices(final Class<ObserverT> observerClass) {
        final ServiceLoader<ObserverT> serviceLoader = ServiceLoader.load(observerClass);
        final ImmutableSet.Builder<ObserverT> resultBuilder = ImmutableSet.builder();
        for (final Iterator<ObserverT> observerI = serviceLoader.iterator(); observerI.hasNext(); ) {
            final ObserverT observer = observerI.next();
            resultBuilder.add(observer);
        }
        return resultBuilder.build();
    }

    public final ObserverRegistration registerDeleteNanopublicationObserver(final DeleteNanopublicationObserver observer) {
        final DeleteNanopublicationObserverRegistration registration = new DeleteNanopublicationObserverRegistration(observer);
        deleteNanopublicationObserverRegistrations.add(registration);
        return registration;
    }

    public final ObserverRegistration registerPutNanopublicationObserver(final PutNanopublicationObserver observer) {
        final PutNanopublicationObserverRegistration registration = new PutNanopublicationObserverRegistration(observer);
        putNanopublicationObserverRegistrations.add(registration);
        return registration;
    }

    @Override
    public void onDeleteNanopublication(final Uri nanopublicationUri) {
        for (final DeleteNanopublicationObserverRegistration observerRegistration : deleteNanopublicationObserverRegistrations) {
            observerRegistration.getObserver().onDeleteNanopublication(nanopublicationUri);
        }
    }

    @Override
    public void onPutNanopublication(final Nanopublication nanopublication) {
        for (final PutNanopublicationObserverRegistration observerRegistration : putNanopublicationObserverRegistrations) {
            observerRegistration.getObserver().onPutNanopublication(nanopublication);
        }
    }

    private abstract class AbstractObserverRegistration<ObserverT extends Observer> implements ObserverRegistration {
        private final ObserverT observer;

        protected AbstractObserverRegistration(final ObserverT observer) {
            this.observer = checkNotNull(observer);
        }

        public final ObserverT getObserver() {
            return observer;
        }
    }

    private final class DeleteNanopublicationObserverRegistration extends AbstractObserverRegistration<DeleteNanopublicationObserver> {
        private DeleteNanopublicationObserverRegistration(final DeleteNanopublicationObserver observer) {
            super(observer);
        }

        @Override
        public final void unregister() {
            deleteNanopublicationObserverRegistrations.remove(this);
        }
    }

    private final class PutNanopublicationObserverRegistration extends AbstractObserverRegistration<PutNanopublicationObserver> {
        private PutNanopublicationObserverRegistration(final PutNanopublicationObserver observer) {
            super(observer);
        }

        @Override
        public final void unregister() {
            putNanopublicationObserverRegistrations.remove(this);
        }
    }
}
