package edu.rpi.tw.twks.api.observer;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.nanopub.Nanopublication;

/**
 * Observer callback interface for the putNanopublication operation.
 */
public interface PutNanopublicationObserver extends Observer {
    void onPutNanopublication(Twks twks, Nanopublication nanopublication);
}
