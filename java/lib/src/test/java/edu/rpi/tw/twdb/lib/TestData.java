package edu.rpi.tw.twdb.lib;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.IOException;
import java.io.InputStream;

public final class TestData {
    public TestData() throws IOException {
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("./nanopublication_without_provenance.trig")) {
            RDFParserBuilder.create().source(inputStream).parse(nanopublicationWithoutProvenanceDataset);
        }
    }

    public final Dataset nanopublicationWithoutProvenanceDataset = DatasetFactory.create();
}
