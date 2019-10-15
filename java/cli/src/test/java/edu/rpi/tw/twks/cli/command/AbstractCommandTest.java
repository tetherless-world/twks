package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.ReadWrite;
import org.junit.Before;

import java.io.IOException;

public abstract class AbstractCommandTest {
    private final TestData testData;
    private Twks twks;

    protected AbstractCommandTest() {
        try {
            this.testData = new TestData();
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public final void newTwks() {
        twks = TwksFactory.getInstance().createTwks();
    }

    public final Twks getTwks() {
        return twks;
    }

    protected final TestData getTestData() {
        return testData;
    }

    protected final void runCommand(final Command command) {
        try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.WRITE)) {
            command.run(new Command.Apis(transaction, transaction, transaction));
            transaction.commit();
        }
    }
}
