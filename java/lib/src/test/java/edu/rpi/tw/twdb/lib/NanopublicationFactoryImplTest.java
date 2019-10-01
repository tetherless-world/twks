package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class NanopublicationFactoryImplTest {
    private NanopublicationFactoryImpl sut;
    private TestData testData;

    @Before
    public void setUp() throws IOException {
        this.sut = new NanopublicationFactoryImpl();
        this.testData = new TestData();
    }

    @Test
    public void testCreateNanopublicationWithoutProvenanceFromDataset() {
        final List<Nanopublication> nanopublications = sut.createNanopublicationsFromDataset(testData.specNanopublicationDataset);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals(1, nanopublication.getAssertion().listStatements().toList().size());
        assertEquals(0, nanopublication.getProvenance().listStatements().toList().size());
        assertEquals(5, nanopublication.getPublicationInfo().listNameSpaces().toList().size());
    }
}
