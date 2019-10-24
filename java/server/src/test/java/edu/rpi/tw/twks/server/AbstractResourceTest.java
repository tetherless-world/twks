package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.io.StringWriter;

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

    protected static String toTrigString(final Nanopublication nanopublication) {
        final Dataset dataset = nanopublication.toDataset();
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dataset, Lang.TRIG);
        return stringWriter.toString();
    }

    protected static String toTrigString(final Model model) {
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.TRIG);
        return stringWriter.toString();
    }

    protected static Entity<String> toTrigEntity(final Nanopublication nanopublication) {
        return Entity.entity(toTrigString(nanopublication), Lang.TRIG.getContentType().getContentType());
    }

    protected static Entity<String> toTrigEntity(final Model model) {
        return Entity.entity(toTrigString(model), Lang.TRIG.getContentType().getContentType());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        throw new UnsupportedOperationException("implement dump directory delete");
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
