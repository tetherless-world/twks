package edu.rpi.tw.twks.nanopub;

public final class MalformedNanopublicationRuntimeException extends RuntimeException {
    public MalformedNanopublicationRuntimeException(final MalformedNanopublicationException cause) {
        super(cause);
    }
}
