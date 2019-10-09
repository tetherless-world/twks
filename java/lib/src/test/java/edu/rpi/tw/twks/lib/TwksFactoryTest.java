package edu.rpi.tw.twks.lib;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwdbDefault() {
        assertNotSame(null, TwdbFactory.getInstance().createTwdb());
    }

    @Test
    public void testCreateTwdbWithConfiguration() {
        assertNotSame(null, TwdbFactory.getInstance().createTwdb(new TwdbConfiguration().setFromSystemProperties()));
    }
}
