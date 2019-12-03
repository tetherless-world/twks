package edu.rpi.tw.twks.factory;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class TwksFactoryConfigurationTest {
    @Test
    public void testSetFromProperties() {
        final TwksFactoryConfiguration.Builder builder = TwksFactoryConfiguration.builder();
        assertFalse(builder.getTdb2Configuration().isPresent());
        final PropertiesConfiguration properties = new PropertiesConfiguration();
        properties.setProperty("twks.tdbLocation", "test");
        builder.set(properties);
        assertEquals("test", builder.build().getTdb2Configuration().get().getLocation().get());
    }
}
