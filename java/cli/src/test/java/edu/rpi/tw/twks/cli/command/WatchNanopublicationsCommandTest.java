package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.test.TestData;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public final class WatchNanopublicationsCommandTest extends AbstractCommandTest<WatchNanopublicationsCommand> {
    @Override
    protected WatchNanopublicationsCommand newCommand() {
        final WatchNanopublicationsCommand command = new WatchNanopublicationsCommand();
        command.getArgs().directoryPath = getTempDirPath().toString();
        return command;
    }

    @Test
    public void testCreate() throws InterruptedException, IOException {
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> runCommand(command));

        Thread.sleep(500);

        writeNanopublication();

        for (int i = 0; i < 10; i++) {
            if (getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent()) {
                command.stop();
                return;
            }
            Thread.sleep(500);
        }

        command.stop();
        fail();
    }

    private void writeNanopublication() throws IOException {
        final File filePath = new File(getTempDirPath().toFile(), "test.trig");
        try (final FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(TestData.SPEC_NANOPUBLICATION_TRIG);
            logger.info("wrote nanopublication to {}", filePath);
        }
    }
}
