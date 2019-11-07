package edu.rpi.tw.twks.server;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
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
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public abstract class AbstractResourceTest extends JerseyTest {
    private final TestData testData;
    private Path tempDirPath;
    private Twks twks;

    protected AbstractResourceTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Entity<String> toTrigEntity(final Nanopublication... nanopublications) {
        return Entity.entity(toTrigString(nanopublications), Lang.TRIG.getContentType().getContentType());
    }

    protected static Entity<String> toTrigEntity(final Model model) {
        return Entity.entity(toTrigString(model), Lang.TRIG.getContentType().getContentType());
    }

    protected static String toTrigString(final Nanopublication... nanopublications) {
        final Dataset dataset = DatasetFactory.create();
        for (final Nanopublication nanopublication : nanopublications) {
            nanopublication.toDataset(dataset);
        }
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dataset, Lang.TRIG);
        return stringWriter.toString();
    }

    protected static String toTrigString(final Model model) {
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.TRIG);
        return stringWriter.toString();
    }

    @Override
    protected final Application configure() {
        assertSame(tempDirPath, null);
        try {
            tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final ResourceConfig config = new ResourceConfig();
        this.twks = TwksFactory.getInstance().createTwks(TwksFactoryConfiguration.builder().setTdb2Configuration(Tdb2TwksConfiguration.builder().setDumpDirectoryPath(tempDirPath.resolve("dump")).build()).build());
        config.registerInstances(newResource(twks));
        return config;
    }

    @After
    public final void deleteTempDir() throws Exception {
        assertNotSame(null, tempDirPath);
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
        tempDirPath = null;
    }

    protected final Path getTempDirPath() {
        return tempDirPath;
    }

    protected final TestData getTestData() {
        return testData;
    }

    protected final Twks getTwks() {
        return checkNotNull(twks);
    }

    protected abstract Object newResource(Twks twks);
}
