package edu.rpi.tw.twks.api.observer;

/**
 * Parent interface of all observer callback interfaces.
 * <p>
 * There are multiple observer child interfaces instead of a single Observer interface with all of the on* so that
 * - implementations can choose what they observe
 * - adding new on* methods will not break implementations
 */
public interface TwksObserver {
}
