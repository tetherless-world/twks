package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.twdb.api.Twdb;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public abstract class TwdbTest {
    private Twdb sut;
    private TestData testData;

    @Before
    public final void setUp() throws IOException, MalformedNanopublicationException {
        this.sut = newTdb();
        this.testData = new TestData();
    }

    @Test
    public void testGetNanopublication() {
        final Nanopublication expected = testData.specNanopublication;
        sut.putNanopublication(expected);
        final Nanopublication actual = sut.getNanopublication(expected.getUri()).get();
        assertNotSame(expected, actual);
        assertTrue(actual.isIsomorphicWith(expected));
    }

    @Test
    public void testPutNanopublication() {
        sut.putNanopublication(testData.specNanopublication);
    }

    protected abstract Twdb newTdb();
}
