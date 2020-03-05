package edu.rpi.tw.twks.cli.command;

import com.google.common.base.Charsets;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PostNanopublicationsCommandTest extends AbstractCommandTest<PostNanopublicationsCommand> {
    private InputStream originalSystemIn;

    @Override
    protected PostNanopublicationsCommand newCommand() {
        return new PostNanopublicationsCommand();
    }

    @After
    public void restoreSystemIn() {
        System.setIn(originalSystemIn);
    }

    @Before
    public void saveSystemIn() {
        originalSystemIn = System.in;
    }

    @Test
    public void testDirectory() throws IOException {
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());

        try (final FileWriter fileWriter = new FileWriter(new File(getTempDirPath().toFile(), "test.trig"))) {
            fileWriter.write(TestData.SPEC_NANOPUBLICATION_TRIG);
        }
        command.getArgs().sources.add(getTempDirPath().toString());
        runCommand();

        final Optional<Nanopublication> actual = getTwks().getNanopublication(getTestData().specNanopublication.getUri());
        assertTrue(getTestData().specNanopublication.isIsomorphicWith(actual.get()));
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
            runCommand();
        } finally {
            tempFilePath.delete();
        }

        final Optional<Nanopublication> actual = getTwks().getNanopublication(getTestData().specNanopublication.getUri());
        assertTrue(getTestData().specNanopublication.isIsomorphicWith(actual.get()));
    }

    @Test
    public void testStdin() throws IOException {
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());

        System.setIn(new ByteArrayInputStream(TestData.SPEC_NANOPUBLICATION_TRIG.getBytes(Charsets.UTF_8)));
        command.getArgs().lang = "trig";
        command.getArgs().sources.add("-");
        runCommand();

        final Optional<Nanopublication> actual = getTwks().getNanopublication(getTestData().specNanopublication.getUri());
        assertTrue(getTestData().specNanopublication.isIsomorphicWith(actual.get()));
    }
}
