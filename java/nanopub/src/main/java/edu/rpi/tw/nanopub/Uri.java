package edu.rpi.tw.nanopub;

import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tiny type for URIs.
 * <p>
 * This is preferred to passing around Strings because it:
 * - does basic RFC 3986 validation
 * - clearly distinguishes URIs from other types of String parameters
 * <p>
 * The class is designed to be immutable and opaque. We rarely need to introspect the structure of URIs.
 */
public final class Uri {
    private final org.dmfs.rfc3986.Uri delegate;
    private final String input;

    private Uri(final org.dmfs.rfc3986.Uri delegate, final String input) {
        this.delegate = checkNotNull(delegate);
        this.input = checkNotNull(input);
    }

    /**
     * Parse an opaque Uri instance from a String.
     *
     * @param uri String representation of a URI
     * @return
     * @throws IllegalArgumentException on a parse error
     */
    public static Uri parse(final String uri) throws IllegalArgumentException {
        final LazyUri delegate = new LazyUri(new Precoded(uri));
        delegate.authority();
        return new Uri(delegate, uri);
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Uri)) {
            return false;
        }
        final Uri other = (Uri) obj;
        return toString().equals(other.toString());
    }

    @Override
    public final String toString() {
        return input;
//        return new Text(delegate).toString();
    }
}
