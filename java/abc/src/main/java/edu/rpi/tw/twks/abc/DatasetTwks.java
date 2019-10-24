package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks extends AbstractTwks {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwks.class);
    private final Dataset dataset;

    protected DatasetTwks(final TwksConfiguration configuration, final Dataset dataset) {
        super(configuration);
        this.dataset = checkNotNull(dataset);
    }

    @Override
    public void dump() throws IOException {
        final Path dumpDirectoryPath = getConfiguration().getDumpDirectoryPath();
        if (!Files.isDirectory(dumpDirectoryPath)) {
            logger.info("dump directory {} does not exist, creating", dumpDirectoryPath);
            Files.createDirectory(dumpDirectoryPath);
            logger.info("created dump directory {}", dumpDirectoryPath);
        }
    }

    protected final Dataset getDataset() {
        return dataset;
    }
}
