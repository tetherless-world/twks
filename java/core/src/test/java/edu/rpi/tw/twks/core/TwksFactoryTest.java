package edu.rpi.tw.twks.core;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwksDefault() {
        assertNotSame(null, TwksFactory.getInstance().createTwks());
    }

    @Test
    public void testCreateTwksWithConfiguration() {
        assertNotSame(null, TwksFactory.getInstance().createTwks(new TwksConfiguration().setFromSystemProperties()));
    }
}
