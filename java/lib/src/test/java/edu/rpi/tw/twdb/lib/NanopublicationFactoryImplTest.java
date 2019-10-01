package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.MalformedNanopublicationException;
import edu.rpi.tw.twdb.api.Nanopublication;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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
    public void testCreateNanopublicationWithoutProvenanceFromDataset() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.createNanopublicationFromDataset(testData.specNanopublicationDataset);
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }

//    @Test
//    public void testCreateNanopublicationFromFile() throws IOException, MalformedNanopublicationException {
//        final Nanopublication nanopublication = sut.createNanopublicationFromFile(testData.specNanopublicationFilePath);
//        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
//        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
//        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
//    }
}
