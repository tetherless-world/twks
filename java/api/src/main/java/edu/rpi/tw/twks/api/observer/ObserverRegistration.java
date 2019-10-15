package edu.rpi.tw.twks.api.observer;

/**
 * Interface for instances returned by register*Observer, to allow un-registering observes without relying on characteristics of the observer instance.
 */
public interface ObserverRegistration {
    void unregister();
}
