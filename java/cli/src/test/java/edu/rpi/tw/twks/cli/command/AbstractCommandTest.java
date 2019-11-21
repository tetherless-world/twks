package edu.rpi.tw.twks.cli.command;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.ReadWrite;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractCommandTest<CommandT extends Command> {
    protected final Logger logger;
    private final TestData testData;
    protected CommandT command;
    private Path tempDirPath;
    private Twks twks;
    private TwksFactoryConfiguration twksConfiguration;

    protected AbstractCommandTest() {
        logger = LoggerFactory.getLogger(getClass());
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public final void deleteTempDir() throws IOException {
        if (tempDirPath != null) {
            MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
            logger.info("deleted temp directory {}", tempDirPath);
            tempDirPath = null;
        }
    }

    protected final Path getTempDirPath() {
        return checkNotNull(tempDirPath);
    }

    protected final TestData getTestData() {
        return testData;
    }

    protected final Twks getTwks() {
        return twks;
    }

    protected final TwksFactoryConfiguration getTwksConfiguration() {
        return twksConfiguration;
    }

    protected abstract CommandT newCommand();

    protected final void runCommand(final Command command) {
        try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.WRITE)) {
            command.run(new Command.Apis(transaction));
            transaction.commit();
        }
    }

    @Before
    public final void setUp() throws IOException {
        tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        twksConfiguration = TwksFactoryConfiguration.builder().setTdb2Configuration(Tdb2TwksConfiguration.builder().setDumpDirectoryPath(tempDirPath.resolve("dump")).build()).build();
        twks = TwksFactory.getInstance().createTwks(twksConfiguration);
        // Order is important.
        command = newCommand();
    }
}
