package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

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
    public void testCreateNanopublicationsFromDataset() throws MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = sut.createNanopublicationsFromDataset(testData.specNanopublicationDataset);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }
}
