package edu.rpi.tw.twks.api.observer;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.uri.Uri;

/**
 * Observer callback interface for the deleteNanopublication operation.
 */
public interface DeleteNanopublicationTwksObserver extends TwksObserver {
    void onDeleteNanopublication(Twks twks, Uri nanopublicationUri);
}
