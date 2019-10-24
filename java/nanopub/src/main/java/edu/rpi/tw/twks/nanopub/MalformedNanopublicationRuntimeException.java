package edu.rpi.tw.twks.nanopub;

public final class MalformedNanopublicationRuntimeException extends RuntimeException {
    MalformedNanopublicationRuntimeException(final MalformedNanopublicationException cause) {
        super(cause);
    }
}
