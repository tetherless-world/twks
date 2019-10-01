package edu.rpi.tw.twdb.lib;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public final class TwdbImplTest {
    private TwdbImpl sut;

    @Before
    public void setUp() {
        this.sut = new TwdbImpl();
    }

    @Test
    public void testGetNanopublicationFactory() {
        assertNotEquals(null, sut.getNanopublicationFactory());
    }

    @Test
    public void testNewNanopublicationParser() {
        assertNotEquals(null, sut.newNanopublicationParser());
    }
}
