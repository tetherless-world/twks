package edu.rpi.tw.twks.api;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;

import java.util.Optional;

/**
 * Nanopublication Create-Read-Update-Delete (CRUD) interface.
 */
public interface NanopublicationCrudApi {
    /**
     * Delete a nanopublication.
     *
     * @param uri URI of the nanopublication
     * @return true if the nanopublication was present, otherwise false
     */
    DeleteNanopublicationResult deleteNanopublication(Uri uri);

    /**
     * Delete nanopublications.
     *
     * @param uris list of nanopublication URIs
     * @return a list of results, guaranteed to be the same size as the input list
     */
    ImmutableList<DeleteNanopublicationResult> deleteNanopublications(ImmutableList<Uri> uris);

    /**
     * Get a nanopublication.
     *
     * @param uri URI of the nanopublication.
     * @return Optional.of(the nanopublication) if it exists in the store, otherwise Optional.empty
     */
    Optional<Nanopublication> getNanopublication(Uri uri);

    /**
     * Put a new nanopublication, overwriting an existing nanopublication with the same URI if necessary.
     *
     * @param nanopublication nanopublication to put.
     * @return result of the operation
     */
    PutNanopublicationResult putNanopublication(Nanopublication nanopublication);

    /**
     * Put a list of nanopublications. See putNanopublication for semantics.
     *
     * @param nanopublications nanopublications to put
     * @return list of results, guaranteed to be the same size as the input list
     */
    ImmutableList<PutNanopublicationResult> putNanopublications(ImmutableList<Nanopublication> nanopublications);

    enum DeleteNanopublicationResult {
        DELETED, NOT_FOUND
    }

    enum PutNanopublicationResult {
        CREATED, OVERWROTE
    }
}
