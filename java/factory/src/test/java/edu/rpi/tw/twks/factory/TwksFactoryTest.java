package edu.rpi.tw.twks.factory;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwksDefault() {
        assertNotSame(null, TwksFactory.getInstance().createTwks());
    }

    @Test
    public void testCreateTwksWithConfiguration() {
        assertNotSame(null, TwksFactory.getInstance().createTwks(TwksFactoryConfiguration.builder().setFromEnvironment().build(), new MetricRegistry()));
    }
}
