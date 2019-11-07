package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.test.ApisTest;
import org.junit.Test;

import java.io.IOException;

public final class DumpCommandTest extends AbstractCommandTest<DumpCommand> {
    @Override
    protected DumpCommand newCommand() {
        return new DumpCommand();
    }

    @Test
    public void testRun() throws IOException {
        getTwks().putNanopublication(getTestData().specNanopublication);
        runCommand(command);
        ApisTest.checkDump(getTwksConfiguration().getTdb2Configuration().get().getDumpDirectoryPath());
    }
}
