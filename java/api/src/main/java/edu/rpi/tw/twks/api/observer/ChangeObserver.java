package edu.rpi.tw.twks.api.observer;

/**
 * Observer callback interface for any operation that changes the store.
 */
public interface ChangeObserver extends TwksObserver {
    void onChange();
}
