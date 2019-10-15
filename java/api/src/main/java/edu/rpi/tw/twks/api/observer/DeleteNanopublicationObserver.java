package edu.rpi.tw.twks.api.observer;

import edu.rpi.tw.twks.uri.Uri;

/**
 * Observer callback interface for the deleteNanopublication operation.
 */
public interface DeleteNanopublicationObserver extends Observer {
    void onDeleteNanopublication(final Uri nanopublicationUri);
}
