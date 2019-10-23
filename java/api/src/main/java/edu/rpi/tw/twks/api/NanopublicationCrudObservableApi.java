package edu.rpi.tw.twks.api;

import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;

/**
 * Nanopublication Create-Read-Update-Delete (CRUD) observable interface.
 */
public interface NanopublicationCrudObservableApi {
    /**
     * Register an observer of the deleteNanopublication operation.
     *
     * @param observer
     * @return registration instance, which can be used to unregister the observer
     */
    TwksObserverRegistration registerDeleteNanopublicationObserver(DeleteNanopublicationTwksObserver observer);

    /**
     * Register an observer of the putNanopublication operation.
     *
     * @param observer
     * @return registration instance, which can be used to unregister the observer
     */
    TwksObserverRegistration registerPutNanopublicationObserver(PutNanopublicationTwksObserver observer);
}
