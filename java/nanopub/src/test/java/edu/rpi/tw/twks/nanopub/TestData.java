package edu.rpi.tw.twks.nanopub;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Nanopub-specific TestData. Has to copied from the twks-test project to avoid a circular dependency.
 */
public final class TestData {
    public final File assertionOnlyFilePath;
    public final File duplicateNanopublicationsFilePath;
    public final File overlappingNanopublicationsFilePath;
    public final File specNanopublicationFilePath;
    public final Dataset specNanopublicationDataset = DatasetFactory.create();
    public final File uniqueNanopublicationsFilePath;
    public final File whyisNanopublicationFilePath;

    public TestData() throws IOException, MalformedNanopublicationException {
        assertionOnlyFilePath = getResourceFilePath("assertion_only.ttl");
        duplicateNanopublicationsFilePath = getResourceFilePath("duplicate_nanopublications.trig");
        overlappingNanopublicationsFilePath = getResourceFilePath("overlapping_nanopublications.trig");
        specNanopublicationFilePath = getResourceFilePath("spec_nanopublication.trig");
        parseDatasetFromResource(specNanopublicationDataset, "spec_nanopublication.trig");
        uniqueNanopublicationsFilePath = getResourceFilePath("unique_nanopublications.trig");
        whyisNanopublicationFilePath = getResourceFilePath("whyis_nanopublication.trig");
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
