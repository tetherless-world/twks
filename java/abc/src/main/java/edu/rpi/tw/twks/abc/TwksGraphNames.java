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
    ImmutableSet<Uri> getAllAssertionGraphNames(final TwksTransaction transaction);

    ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri, final TwksTransaction transaction);

    ImmutableSet<Uri> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris, final TwksTransaction transaction);
}
