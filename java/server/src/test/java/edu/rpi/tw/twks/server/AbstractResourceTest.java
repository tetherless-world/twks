package edu.rpi.tw.twks.server;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.lib.TwksFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractResourceTest extends JerseyTest {
    private final TestData testData;
    private Twks db;

    protected AbstractResourceTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    protected final Twks getDb() {
        return checkNotNull(db);
    }

    protected final TestData getTestData() {
        return testData;
    }

    @Override
    protected final Application configure() {
        final ResourceConfig config = new ResourceConfig();
        this.db = TwksFactory.getInstance().createTwks();
        config.registerInstances(newResource(db));
        return config;
    }

    protected abstract Object newResource(Twks db);
}
