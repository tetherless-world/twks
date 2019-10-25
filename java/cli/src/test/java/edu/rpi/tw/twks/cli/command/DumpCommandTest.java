package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.test.ApisTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public final class DumpCommandTest extends AbstractCommandTest {
    private DumpCommand command;

    @Before
    public void setUp() {
        command = new DumpCommand();
    }

    @Test
    public void testRun() throws IOException {
        getTwks().putNanopublication(getTestData().specNanopublication);
        runCommand(command);
        ApisTest.checkDump(getTwksConfiguration().getDumpDirectoryPath());
    }
}
