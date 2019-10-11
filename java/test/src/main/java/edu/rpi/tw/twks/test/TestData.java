package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public final class TestData {
    public final File assertionOnlyFilePath;
    public final File specNanopublicationFilePath;
    public final Dataset specNanopublicationDataset = DatasetFactory.create();
    public final Nanopublication secondNanopublication;
    public final Nanopublication specNanopublication;
    public final File whyisNanopublicationFilePath;

    public TestData() throws IOException, MalformedNanopublicationException {
        assertionOnlyFilePath = getResourceFilePath("assertion_only.ttl");
        specNanopublicationFilePath = getResourceFilePath("spec_nanopublication.trig");
        parseDatasetFromResource(specNanopublicationDataset, "spec_nanopublication.trig");
        whyisNanopublicationFilePath = getResourceFilePath("whyis_nanopublication.trig");
        secondNanopublication = parseNanopublicationFromResource("second_nanopublication.trig");
        specNanopublication = parseNanopublicationFromResource("spec_nanopublication.trig");
    }

    private Nanopublication parseNanopublicationFromResource(final String fileName) throws IOException, MalformedNanopublicationException {
        final URL url = getClass().getResource("./" + fileName);
        return new NanopublicationParser().parse(Uri.parse(url.toString()));
    }

    private File getResourceFilePath(final String fileName) throws IOException {
        final URL url = getClass().getResource("./" + fileName);
        try {
            return new File(url.toURI());
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void parseDatasetFromResource(final Dataset dataset, final String fileName) throws IOException {
        final URL url = getClass().getResource("./" + fileName);
        try (final InputStream inputStream = url.openStream()) {
            RDFParserBuilder.create().base(url.toString()).source(inputStream).parse(dataset);
        }
    }

}
