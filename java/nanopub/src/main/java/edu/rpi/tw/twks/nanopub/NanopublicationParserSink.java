package edu.rpi.tw.twks.nanopub;

/**
 * Sink for the outputs of the nanopublication parser.
 * <p>
 * Use this instead of the standard library Supplier (for streams) or Consumer in order to accommodate exceptions.
 * <p>
 * Some sinks may want to ignore exceptions, others may wrap the checked exceptions in runtime exceptions.
 */
public interface NanopublicationParserSink {
    void accept(Nanopublication nanopublication);

    void onMalformedNanopublicationException(MalformedNanopublicationException exception);
}
