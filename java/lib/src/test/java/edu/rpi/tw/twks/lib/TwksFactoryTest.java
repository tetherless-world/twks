package edu.rpi.tw.twks.lib;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwdbDefault() {
        assertNotSame(null, TwksFactory.getInstance().createTwdb());
    }

    @Test
    public void testCreateTwdbWithConfiguration() {
        assertNotSame(null, TwksFactory.getInstance().createTwdb(new TwksConfiguration().setFromSystemProperties()));
    }
}
