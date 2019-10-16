package edu.rpi.tw.twks.ext;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserver;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ClasspathExtensions {
    private static <ObserverT extends TwksObserver> ImmutableSet<ObserverT> loadObserverServices(final Class<ObserverT> observerClass) {
        final ServiceLoader<ObserverT> serviceLoader = ServiceLoader.load(observerClass);
        final ImmutableSet.Builder<ObserverT> resultBuilder = ImmutableSet.builder();
        for (final Iterator<ObserverT> observerI = serviceLoader.iterator(); observerI.hasNext(); ) {
            final ObserverT observer = observerI.next();
            resultBuilder.add(observer);
        }
        return resultBuilder.build();
    }

    public final void registerObservers(final Twks twks) {
        loadObserverServices(DeleteNanopublicationTwksObserver.class).forEach(observer -> twks.registerDeleteNanopublicationObserver(observer));
        loadObserverServices(PutNanopublicationTwksObserver.class).forEach(observer -> twks.registerPutNanopublicationObserver(observer));
    }
}
