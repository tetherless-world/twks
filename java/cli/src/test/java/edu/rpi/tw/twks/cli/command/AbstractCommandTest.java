package edu.rpi.tw.twks.cli.command;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.ReadWrite;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractCommandTest {
    private final TestData testData;
    private Twks twks;
    private Path tempDirPath;
    private TwksFactoryConfiguration twksConfiguration;

    protected AbstractCommandTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public final void newTwks() throws IOException {
        tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        twksConfiguration = TwksFactoryConfiguration.builder().setDumpDirectoryPath(tempDirPath.resolve("dump")).build();
        twks = TwksFactory.getInstance().createTwks(twksConfiguration);
    }

    @After
    public final void deleteTempDir() throws IOException {
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
    }

    protected final TwksFactoryConfiguration getTwksConfiguration() {
        return twksConfiguration;
    }

    protected final Twks getTwks() {
        return twks;
    }

    protected final TestData getTestData() {
        return testData;
    }

    protected final void runCommand(final Command command) {
        try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.WRITE)) {
            command.run(new Command.Apis(transaction));
            transaction.commit();
        }
    }
}
