package edu.rpi.tw.twdb.api;

@SuppressWarnings("serializable")
public final class MalformedNanopublicationException extends Exception {
    public MalformedNanopublicationException(final String message) {
        super(message);
    }

    public MalformedNanopublicationException(final Throwable cause) {
        super(cause);
    }
}
