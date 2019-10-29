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

public abstract class AbstractCommandTest<CommandT extends Command> {
    private final TestData testData;
    protected CommandT command;
    private Path tempDirPath;
    private Twks twks;
    private TwksFactoryConfiguration twksConfiguration;

    protected AbstractCommandTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public final void deleteTempDir() throws IOException {
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
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
        command = newCommand();
        tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        twksConfiguration = TwksFactoryConfiguration.builder().setDumpDirectoryPath(tempDirPath.resolve("dump")).build();
        twks = TwksFactory.getInstance().createTwks(twksConfiguration);
    }
}
