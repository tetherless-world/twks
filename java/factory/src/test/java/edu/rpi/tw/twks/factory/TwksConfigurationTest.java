package edu.rpi.tw.twks.factory;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class TwksConfigurationTest {
    @Test
    public void testSetFromProperties() {
        final TwksConfiguration sut = new TwksConfiguration();
        Assert.assertFalse(sut.getTdb2Location().isPresent());
        final Properties properties = new Properties();
        properties.setProperty(TwksConfiguration.PropertyKeys.TDB2_LOCATION, "test");
        sut.setFromProperties(properties);
        assertEquals("test", sut.getTdb2Location().get());
    }
}
