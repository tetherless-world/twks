package edu.rpi.tw.twks.api;

import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
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
    TwksObserverRegistration registerDeleteNanopublicationObserver(DeleteNanopublicationObserver observer);

    /**
     * Register an observer of the putNanopublication operation.
     *
     * @param observer
     * @return registration instance, which can be used to unregister the observer
     */
    TwksObserverRegistration registerPutNanopublicationObserver(PutNanopublicationObserver observer);
}
