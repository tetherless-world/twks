package edu.rpi.tw.twdb.lib;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class TwdbConfigurationTest {
    @Test
    public void testSetFromProperties() {
        final TwdbConfiguration sut = new TwdbConfiguration();
        assertFalse(sut.getTdb2Location().isPresent());
        final Properties properties = new Properties();
        properties.setProperty(TwdbConfiguration.PropertyKeys.TDB2_LOCATION, "test");
        sut.setFromProperties(properties);
        assertEquals("test", sut.getTdb2Location().get());
    }
}
