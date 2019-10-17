package edu.rpi.tw.twks.api.observer;

import edu.rpi.tw.twks.api.Twks;

/**
 * Observer callback interface for any operation that changes the store.
 */
public interface ChangeTwksObserver extends TwksObserver {
    void onChange(Twks twks);
}
