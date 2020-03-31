package edu.rpi.tw.twks.nanopub;

import java.nio.file.Path;

/**
 * Consumer for the outputs of the nanopublication directory parser.
 * <p>
 * Similar to the NanopublicationConsumer, except the callbacks also accept the source file path of a nanopublication or the file that induced an exception.
 */
public interface NanopublicationDirectoryConsumer {
    void accept(Nanopublication nanopublication, Path nanopublicationFilePath);

    void onMalformedNanopublicationException(MalformedNanopublicationException exception, Path nanopublicationFilePath);
}
