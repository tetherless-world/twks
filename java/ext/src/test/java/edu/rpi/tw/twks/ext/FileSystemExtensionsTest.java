package edu.rpi.tw.twks.ext;

import com.google.common.io.CharStreams;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.test.TestData;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class FileSystemExtensionsTest {
    private final TestData testData;
    private PrintStream originalSystemOut;
    private FileSystemExtensions sut;
    private Path tempDirPath;
    private Twks twks;

    public FileSystemExtensionsTest() throws Exception {
        testData = new TestData();
    }

    @Before
    public void setUp() throws IOException {
        originalSystemOut = System.out;
        tempDirPath = createTempDirectory(getClass().getCanonicalName());
        sut = new FileSystemExtensions(tempDirPath);
        twks = TwksFactory.getInstance().createTwks();
    }

    @After
    public void tearDown() throws IOException {
        System.setOut(originalSystemOut);
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
        tempDirPath = null;
    }

    @Test
    public void testDeleteNanopublicationScript() throws IOException, InterruptedException {
        if (!SystemUtils.IS_OS_UNIX) {
            return;
        }

        twks.putNanopublication(testData.specNanopublication);

        final Path tempSubdirPath = tempDirPath.resolve("delete_nanopublication");
        Files.createDirectory(tempSubdirPath);
        try (final FileWriter fileWriter = new FileWriter(tempSubdirPath.resolve("delete_nanopublication_test.sh").toFile())) {
            fileWriter.write("#!/bin/bash\n" +
                    "echo \"$1\">$(dirname \"$0\")/ran.txt");
        }

        sut.registerObservers(twks);

        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, twks.deleteNanopublication(testData.specNanopublication.getUri()));

        for (int tryI = 0; tryI < 10; tryI++) {
            try (final FileReader fileReader = new FileReader(tempSubdirPath.resolve("ran.txt").toFile())) {
                final String output = CharStreams.toString(fileReader);
                assertEquals(testData.specNanopublication.getUri().toString(), output);
                return;
            } catch (final IOException e) {
                Thread.sleep(500);
            }
        }
        fail();
    }
}
