package edu.rpi.tw.twdb.server;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.TwdbFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractResourceTest extends JerseyTest {
    private final TestData testData;
    private Twdb db;

    protected AbstractResourceTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    protected final Twdb getDb() {
        return checkNotNull(db);
    }

    protected final TestData getTestData() {
        return testData;
    }

    @Override
    protected final Application configure() {
        final ResourceConfig config = new ResourceConfig();
        this.db = TwdbFactory.getInstance().createTwdb();
        config.registerInstances(newResource(db));
        return config;
    }

    protected abstract Object newResource(Twdb db);
}
