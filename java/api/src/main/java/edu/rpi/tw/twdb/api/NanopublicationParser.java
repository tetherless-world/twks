package edu.rpi.tw.twdb.api;

import org.apache.jena.riot.Lang;
import org.dmfs.rfc3986.Uri;

import java.io.File;
import java.io.IOException;

public interface NanopublicationParser {
    NanopublicationParser setLang(Lang lang);

    Nanopublication parse(File filePath) throws MalformedNanopublicationException, IOException;

    Nanopublication parse(Uri url) throws MalformedNanopublicationException, IOException;
}
