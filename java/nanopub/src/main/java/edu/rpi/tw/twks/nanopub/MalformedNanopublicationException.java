package edu.rpi.tw.twks.nanopub;

@SuppressWarnings("serializable")
public final class MalformedNanopublicationException extends Exception {
    MalformedNanopublicationException(final String message) {
        super(message);
    }
}
