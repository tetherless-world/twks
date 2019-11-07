package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.ReadWrite;

import java.nio.file.Path;

public abstract class TwksTransactionTest extends ApisTest<TwksTransaction> {
    @Override
    protected final void closeSystemUnderTest(final TwksTransaction sut) {
        sut.commit();
        sut.close();
    }

    protected abstract Twks newTwks(Path dumpDirectoryPath);

    @Override
    protected final TwksTransaction openSystemUnderTest() throws Exception {
        return newTwks(getTempDirPath().resolve("dump")).beginTransaction(ReadWrite.WRITE);
    }
}
