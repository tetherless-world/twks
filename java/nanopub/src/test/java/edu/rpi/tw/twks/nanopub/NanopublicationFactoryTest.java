package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public final class NanopublicationFactoryTest {
    private NanopublicationFactory sut;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.sut = NanopublicationFactory.getInstance();
        this.testData = new TestData();
    }

    @Test
    public void testDuplicateNanopublications() throws IOException {
        try {
            sut.createNanopublicationsFromDataset(testData.duplicateNanopublicationsDataset);
            fail();
        } catch (final MalformedNanopublicationException e) {
        }
    }

    @Test
    public void testCreateNanopublicationFromDataset() throws MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = sut.createNanopublicationsFromDataset(testData.specNanopublicationDataset);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }

    @Test
    public void testCreateNanopublicationsFromDataset() throws MalformedNanopublicationException, IOException {
        final Dataset dataset = DatasetFactory.create();
        new NanopublicationParser().parse(testData.assertionOnlyFilePath).get(0).toDataset(dataset);
        sut.createNanopublicationsFromDataset(testData.specNanopublicationDataset).get(0).toDataset(dataset);
        assertEquals(8, ImmutableList.copyOf(dataset.listNames()).size());
        final ImmutableList<Nanopublication> nanopublications = NanopublicationFactory.getInstance().createNanopublicationsFromDataset(dataset);
        assertEquals(2, nanopublications.size());
    }

    @Test
    public void testMultipleUniqueNanopublications() throws Exception {
        final ImmutableList<Nanopublication> nanopublications = sut.createNanopublicationsFromDataset(testData.uniqueNanopublicationsDataset);
        assertEquals(2, nanopublications.size());
        final Map<String, Nanopublication> nanopublicationsByUri = nanopublications.stream().collect(Collectors.toMap(nanopublication -> nanopublication.getUri().toString(), nanopublication -> nanopublication));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub1"));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub2"));
    }

    @Test
    public void testOverlappingNanopublications() throws IOException {
        try {
            sut.createNanopublicationsFromDataset(testData.overlappingNanopublicationsDataset);
            fail();
        } catch (final MalformedNanopublicationException e) {
        }
    }
}
