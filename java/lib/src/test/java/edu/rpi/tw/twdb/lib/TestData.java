package edu.rpi.tw.twdb.lib;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class TestData {
    public TestData() throws IOException {
        parseDatasetFromResource(specNanopublicationDataset, "spec_nanopublication.trig");
    }

    private void parseDatasetFromResource(final Dataset dataset, final String fileName) throws IOException {
        final URL url = getClass().getResource("./" + fileName);
        try (final InputStream inputStream = url.openStream()) {
            RDFParserBuilder.create().base(url.toString()).source(inputStream).parse(dataset);
        }
    }

    public final Dataset specNanopublicationDataset = DatasetFactory.create();
}
