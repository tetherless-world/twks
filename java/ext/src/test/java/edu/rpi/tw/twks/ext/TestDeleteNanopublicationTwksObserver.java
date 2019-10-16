package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.uri.Uri;

public final class TestDeleteNanopublicationTwksObserver implements DeleteNanopublicationTwksObserver {
    static boolean instantiated = false;

    public TestDeleteNanopublicationTwksObserver() {
        instantiated = true;
    }

    @Override
    public void onDeleteNanopublication(final Twks twks, final Uri nanopublicationUri) {

    }
}
