package edu.rpi.tw.twks.nanopub;

import java.nio.file.Path;

/**
 * Sink for the outputs of the nanopublication directory parser.
 * <p>
 * Similar to the NanopublicationParserSink, except the callbacks also accept the source file path of a nanopublication or the file that induced an exception.
 */
public interface NanopublicationDirectoryParserSink {
    void accept(Nanopublication nanopublication, Path nanopublicationFilePath);

    void onMalformedNanopublicationException(MalformedNanopublicationException exception, Path nanopublicationFilePath);
}
