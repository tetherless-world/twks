package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.DatasetTransaction;
import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public abstract class TwdbTest {
    private Twdb sut;
    private TestData testData;

    @Before
    public final void setUp() throws IOException, MalformedNanopublicationException {
        this.sut = newTdb();
        this.testData = new TestData();
    }

    @Test
    public void testDeleteNanopublicationAbsent() {
        assertFalse(sut.deleteNanopublication(testData.specNanopublication.getUri()));
    }

    @Test
    public void testDeleteNanopublicationPresent() {
        sut.putNanopublication(testData.specNanopublication);
        assertTrue(sut.deleteNanopublication(testData.specNanopublication.getUri()));
        assertFalse(sut.deleteNanopublication(testData.specNanopublication.getUri()));
    }

    @Test
    public void testGetNanopublicationAbsent() throws MalformedNanopublicationException {
        final Optional<Nanopublication> actual = sut.getNanopublication(testData.specNanopublication.getUri());
        assertFalse(actual.isPresent());
    }

    @Test
    public void testGetNanopublicationPresent() throws MalformedNanopublicationException {
        final Nanopublication expected = testData.specNanopublication;
        sut.putNanopublication(expected);
        final Nanopublication actual = sut.getNanopublication(expected.getUri()).get();
        assertNotSame(expected, actual);
        RDFDataMgr.write(System.out, actual.toDataset(), Lang.TRIG);
        assertTrue(actual.isIsomorphicWith(expected));
    }

    @Test
    public void testGetNanopublicationsDataset() {
        final Dataset nanopublicationDataset = testData.specNanopublication.toDataset();
        {
            final Dataset nanopublicationsDataset = sut.getNanopublicationsDataset();
            try (final DatasetTransaction nanopublicationsDatasetTransaction = new DatasetTransaction(nanopublicationsDataset, ReadWrite.READ)) {
                assertFalse(nanopublicationDataset.getUnionModel().isIsomorphicWith(nanopublicationsDataset.getUnionModel()));
            }
        }
        sut.putNanopublication(testData.specNanopublication);
        {
            final Dataset nanopublicationsDataset = sut.getNanopublicationsDataset();
            try (final DatasetTransaction nanopublicationsDatasetTransaction = new DatasetTransaction(nanopublicationsDataset, ReadWrite.READ)) {
                assertTrue(nanopublicationDataset.getUnionModel().isIsomorphicWith(nanopublicationsDataset.getUnionModel()));
            }
        }
    }

    @Test
    public void testPutNanopublication() {
        sut.putNanopublication(testData.specNanopublication);
    }

    protected abstract Twdb newTdb();
}
