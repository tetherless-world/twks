package edu.rpi.tw.twks.factory;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.Twks;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class TwksFactoryTest {
    @Test
    public void testCreateTwksDefault() {
        try (final Twks twks = TwksFactory.getInstance().createTwks()) {
            assertNotSame(null, twks);
        }
    }

    @Test
    public void testCreateTwksWithConfiguration() {
        try (final Twks twks = TwksFactory.getInstance().createTwks(TwksFactoryConfiguration.builder().setFromEnvironment().build(), new MetricRegistry())) {
            assertNotSame(null, twks);
        }
    }
}
