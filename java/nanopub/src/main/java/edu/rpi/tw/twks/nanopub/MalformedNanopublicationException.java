package edu.rpi.tw.twks.nanopub;

import org.apache.jena.riot.RiotParseException;

@SuppressWarnings("serializable")
public final class MalformedNanopublicationException extends Exception {
    MalformedNanopublicationException(final String message) {
        super(message);
    }

    public MalformedNanopublicationException(final RiotParseException cause) {
        super(cause);
    }
}
