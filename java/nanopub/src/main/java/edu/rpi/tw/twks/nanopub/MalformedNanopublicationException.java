package edu.rpi.tw.twks.nanopub;

import org.apache.jena.riot.RiotException;

@SuppressWarnings("serializable")
public final class MalformedNanopublicationException extends Exception {
    MalformedNanopublicationException(final String message) {
        super(message);
    }

    public MalformedNanopublicationException(final RiotException cause) {
        super(cause);
    }
}
