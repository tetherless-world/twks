package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.factory.TwksFactory;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ClasspathExtensionsTest {
    @Test
    public void testLifecycle() throws Exception {
        try (final Twks twks = TwksFactory.getInstance().createTwks()) {
            assertFalse(TestTwksExtension.instantiated);
            final ClasspathExtensions sut = new ClasspathExtensions(Optional.empty(), twks);
            assertTrue(TestTwksExtension.instantiated);
            assertFalse(TestTwksExtension.initialized);
            sut.initialize();
            assertTrue(TestTwksExtension.initialized);
            sut.destroy();
            assertTrue(TestTwksExtension.destroyed);
        }
    }
}
