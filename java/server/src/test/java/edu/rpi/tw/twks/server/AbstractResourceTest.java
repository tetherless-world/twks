package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.test.TestData;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractResourceTest extends JerseyTest {
    private final TestData testData;
    private Twks twks;

    protected AbstractResourceTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    protected final Twks getTwks() {
        return checkNotNull(twks);
    }

    protected final TestData getTestData() {
        return testData;
    }

    @Override
    protected final Application configure() {
        final ResourceConfig config = new ResourceConfig();
        this.twks = TwksFactory.getInstance().createTwks();
        config.registerInstances(newResource(twks));
        return config;
    }

    protected abstract Object newResource(Twks twks);
}
