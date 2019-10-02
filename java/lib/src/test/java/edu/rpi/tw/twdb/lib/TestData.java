package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.NanopublicationParser;
import edu.rpi.tw.nanopub.Uris;

import java.io.IOException;
import java.net.URL;

public final class TestData {
    public final Nanopublication specNanopublication;

    public TestData() throws MalformedNanopublicationException, IOException {
        specNanopublication = parseNanopublicationFromResource("spec_nanopublication.trig");
    }

    private Nanopublication parseNanopublicationFromResource(final String fileName) throws IOException, MalformedNanopublicationException {
        final URL url = getClass().getResource("./" + fileName);
        return new NanopublicationParser().parse(Uris.parse(url.toString()));
    }
}
