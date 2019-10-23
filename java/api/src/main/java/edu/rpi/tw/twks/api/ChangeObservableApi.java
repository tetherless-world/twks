package edu.rpi.tw.twks.api;

import edu.rpi.tw.twks.api.observer.ChangeTwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;

public interface ChangeObservableApi {
    /**
     * Register an observer of changes to the store.
     *
     * @param observer
     * @return registration instance, which can be used to unregister the observer
     */
    TwksObserverRegistration registerChangeObserver(ChangeTwksObserver observer);
}
