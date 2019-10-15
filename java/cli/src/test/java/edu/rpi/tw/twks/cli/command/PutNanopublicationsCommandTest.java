package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PutNanopublicationsCommandTest extends AbstractCommandTest {
    private PutNanopublicationsCommand command;

    @Before
    public void setUp() {
        command = new PutNanopublicationsCommand();
    }

    @Test
    public void testFile() throws IOException {
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());

        final File tempFilePath = File.createTempFile(getClass().getCanonicalName(), ".trig");
        try {
            try (final FileWriter fileWriter = new FileWriter(tempFilePath)) {
                fileWriter.write(TestData.SPEC_NANOPUBLICATION_TRIG);
            }
            command.getArgs().sources.add(tempFilePath.toString());
            runCommand(command);
        } finally {
            tempFilePath.delete();
        }

        final Optional<Nanopublication> actual = getTwks().getNanopublication(getTestData().specNanopublication.getUri());
        assertTrue(getTestData().specNanopublication.isIsomorphicWith(actual.get()));
    }
}
