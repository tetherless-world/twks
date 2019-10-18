package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class NanopublicationFactoryTest {
    private NanopublicationFactory sut;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.sut = NanopublicationFactory.getInstance();
        this.testData = new TestData();
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
}
