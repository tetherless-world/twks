package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.uri.Uri;

/**
 * Interface for accessing graph names from an underlying Twks instance.
 * <p>
 * This is an interface and not an implementation class so there can be two implementations, one caching and one not.
 */
public interface TwksGraphNames {
    /**
     * Get the names of all assertion graphs in the store.
     *
     * @param transaction transaction to use to query the store
     * @return set of assertion graph names
     */
    ImmutableSet<Uri> getAllAssertionGraphNames(final TwksTransaction transaction);

    /**
     * Get the names of all a nanopublication's parts (graph + name).
     *
     * @param nanopublicationUri URI of the nanopublication whose part names should be retrieved
     * @param transaction        transaction to use to query the store
     * @return names of the nanopublication's parts
     */
    ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri, final TwksTransaction transaction);

    /**
     * Get the names of the assertion graphs associated with a set of ontologies.
     *
     * @param ontologyUris URIs of the ontologies
     * @param transaction  transaction to use to query the store
     * @return set of assertion graph names
     */
    ImmutableSet<Uri> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris, final TwksTransaction transaction);

    /**
     * Invalidate any underlying caches. Not all implementations of this interface cache names.
     */
    void invalidateCache();
}
