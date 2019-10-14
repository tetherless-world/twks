package edu.rpi.tw.twks.factory;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwksDefault() {
        Assert.assertNotSame(null, TwksFactory.getInstance().createTwks());
    }

    @Test
    public void testCreateTwksWithConfiguration() {
        Assert.assertNotSame(null, TwksFactory.getInstance().createTwks(new TwksConfiguration().setFromSystemProperties()));
    }
}
