package edu.rpi.tw.twdb.lib;

import org.dmfs.rfc3986.Uri;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc3986.uris.Text;

public final class Uris {
    public static Uri parse(final String uri) {
        return new LazyUri(new Precoded(uri));
    }

    public static String toString(final Uri uri) {
        return new Text(uri).toString();
    }
}
