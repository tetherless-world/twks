package edu.rpi.tw.twks.cli.command;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PostNanopublicationsCommandTest extends AbstractCommandTest {
    private PostNanopublicationsCommand command;
    private InputStream originalSystemIn;

    @Before
    public void setUp() {
        command = new PostNanopublicationsCommand();
        originalSystemIn = System.in;
    }

    @After
    public void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    public void testDirectory() throws IOException {
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());

        final File tempDirPath = Files.createTempDir().getAbsoluteFile();
        try {
            try (final FileWriter fileWriter = new FileWriter(new File(tempDirPath, "test.trig"))) {
                fileWriter.write(TestData.SPEC_NANOPUBLICATION_TRIG);
            }
            command.getArgs().sources.add(tempDirPath.toString());
            runCommand(command);
        } finally {
            MoreFiles.deleteRecursively(tempDirPath.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        }

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
            runCommand(command);
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
        runCommand(command);

        final Optional<Nanopublication> actual = getTwks().getNanopublication(getTestData().specNanopublication.getUri());
        assertTrue(getTestData().specNanopublication.isIsomorphicWith(actual.get()));
    }
}
