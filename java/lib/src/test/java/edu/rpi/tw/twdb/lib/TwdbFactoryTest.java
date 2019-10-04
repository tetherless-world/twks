package edu.rpi.tw.twdb.lib;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwdbFactoryTest {
    @Test
    public void testCreateTwdbDefault() {
        assertNotSame(null, TwdbFactory.getInstance().createTwdb());
    }

    @Test
    public void testCreateTwdbWithConfiguration() {
        assertNotSame(null, TwdbFactory.getInstance().createTwdb(new TwdbConfiguration().setFromSystemProperties()));
    }
}
