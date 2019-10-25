package edu.rpi.tw.twks.factory;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class TwksFactoryConfigurationTest {
    @Test
    public void testSetFromProperties() {
        final TwksFactoryConfiguration.Builder builder = TwksFactoryConfiguration.builder();
        assertFalse(builder.getTdb2Location().isPresent());
        final Properties properties = new Properties();
        properties.setProperty(TwksFactoryConfiguration.PropertyKeys.TDB2_LOCATION, "test");
        builder.setFromProperties(properties);
        assertEquals("test", builder.build().getTdb2Location().get());
    }
}
