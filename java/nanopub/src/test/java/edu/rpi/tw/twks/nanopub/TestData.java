package edu.rpi.tw.twks.nanopub;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Nanopub-specific TestData. Has to copied from the twks-test project to avoid a circular dependency.
 */
public final class TestData {
    public final Path assertionOnlyFilePath;
    public final Dataset duplicateNanopublicationsDataset;
    public final Path mixFormedNanonpublicationFilePath;
    public final Dataset overlappingNanopublicationsDataset;
    public final Dataset specNanopublicationDataset;
    public final Path specNanopublicationFilePath;
    public final Dataset uniqueNanopublicationsDataset;
    public final Path whyisNanopublicationFilePath;

    public TestData() throws IOException, MalformedNanopublicationException {
        assertionOnlyFilePath = getResourceFilePath("assertion_only.ttl");
        duplicateNanopublicationsDataset = parseDatasetFromResource("duplicate_nanopublications.trig");
        mixFormedNanonpublicationFilePath = getResourceFilePath("mix_formed_nanopublications.trig");
        overlappingNanopublicationsDataset = parseDatasetFromResource("overlapping_nanopublications.trig");
        specNanopublicationFilePath = getResourceFilePath("spec_nanopublication.trig");
        specNanopublicationDataset = parseDatasetFromResource("spec_nanopublication.trig");
        uniqueNanopublicationsDataset = parseDatasetFromResource("unique_nanopublications.trig");
        whyisNanopublicationFilePath = getResourceFilePath("whyis_nanopublication.trig");
    }

    private Path getResourceFilePath(final String fileName) throws IOException {
        final URL url = getClass().getResource("./" + fileName);
        try {
            return new File(url.toURI()).toPath();
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private Dataset parseDatasetFromResource(final String fileName) throws IOException {
        final URL url = getClass().getResource("./" + fileName);
        try (final InputStream inputStream = url.openStream()) {
            final Dataset dataset = DatasetFactory.create();
            RDFParserBuilder.create().base(url.toString()).source(inputStream).parse(dataset);
            return dataset;
        }
    }


}
