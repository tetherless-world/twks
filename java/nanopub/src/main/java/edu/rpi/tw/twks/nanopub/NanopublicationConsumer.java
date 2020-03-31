package edu.rpi.tw.twks.nanopub;

/**
 * Consumer for the outputs of the nanopublication parser.
 * <p>
 * Use this instead of the standard library Supplier (for streams) or Consumer in order to accommodate exceptions.
 * <p>
 * Some consumers may want to ignore exceptions, others may wrap the checked exceptions in runtime exceptions.
 */
public interface NanopublicationConsumer {
    void accept(Nanopublication nanopublication);

    void onMalformedNanopublicationException(MalformedNanopublicationException exception);
}
