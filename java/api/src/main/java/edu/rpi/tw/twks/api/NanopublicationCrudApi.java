package edu.rpi.tw.twks.api;

import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;

import java.util.Optional;

/**
 * Nanopublication Create-Read-Update-Delete (CRUD) interface.
 */
public interface NanopublicationCrudApi {
    /**
     * Delete a nanopublication.
     * <p>
     * Starts a new transaction and delegates to deleteNanopublication(Uri, TwksTransaction).
     *
     * @param uri URI of the nanopublication
     * @return true if the nanopublication was present, otherwise false
     */
    DeleteNanopublicationResult deleteNanopublication(Uri uri);

    /**
     * Get a nanopublication.
     * <p>
     * Starts a new transaction and delegates to getNanopublication(Uri, TwksTransaction).
     *
     * @param uri URI of the nanopublication.
     * @return Optional.of(the nanopublication) if it exists in the store, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     * <p>
     * Starts a new transaction and delegates to putNanopublication(Nanopublication, TwksTransaction).
     *
     * @param nanopublication nanopublication to put.
     */
    PutNanopublicationResult putNanopublication(Nanopublication nanopublication);

    enum DeleteNanopublicationResult {
        DELETED, NOT_FOUND
    }

    enum PutNanopublicationResult {
        CREATED, OVERWROTE
    }
}
