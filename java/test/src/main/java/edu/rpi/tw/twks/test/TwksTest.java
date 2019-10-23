package edu.rpi.tw.twks.test;

import com.google.common.io.MoreFiles;
import edu.rpi.tw.twks.api.Twks;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public abstract class TwksTest extends ApisTest<Twks> {
    @Override
    protected final Twks openSystemUnderTest() throws Exception {
        return newTwks();
    }

    @Override
    protected final void closeSystemUnderTest(final Twks sut) {
    }

    @Test
    public void testDump() throws Exception {
        final Path tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        try {
            getSystemUnderTest().dump(tempDirPath);
            final List<Path> filePaths = Files.list(tempDirPath).collect(Collectors.toList());
            for (final Path filePath : filePaths) {
                if (Files.isRegularFile(filePath) && filePath.getFileName().endsWith(".trig")) {
                    return;
                }
            }
            fail();
        } finally {
            MoreFiles.deleteRecursively(tempDirPath);
        }
    }

    protected abstract Twks newTwks();
}
