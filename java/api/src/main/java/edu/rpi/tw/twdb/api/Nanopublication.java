package edu.rpi.tw.twdb.api;

import org.dmfs.rfc3986.Uri;

public interface Nanopublication {
    NamedModel getAssertion();

    NamedModel getProvenance();

    NamedModel getPublicationInfo();

    Uri getUri();
}
