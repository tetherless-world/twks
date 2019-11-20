package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WatchNanopublicationsCommand extends Command {
    private final static String[] ALIASES = {};
    private final static String NAME = "watch-nanopublications";
    private final static Logger logger = LoggerFactory.getLogger(WatchNanopublicationsCommand.class);
    private final Args args = new Args();

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public Args getArgs() {
        return args;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(final Apis apis) {
        final Path directoryPath = Paths.get(args.directoryPath);
        final CliNanopublicationParser nanopublicationParser = new CliNanopublicationParser(args);

        final ImmutableList<Nanopublication> initialNanopublications = nanopublicationParser.parseDirectory(directoryPath.toFile());
        apis.getNanopublicationCrudApi().postNanopublications(initialNanopublications);
        logger.info("posted {} initial nanopublications", initialNanopublications.size());

        NanopublicationsDirectoryWatcher watcher = null;
        try {
            watcher = new NanopublicationsDirectoryWatcher(directoryPath, initialNanopublications, apis.getNanopublicationCrudApi(), nanopublicationParser);
        } catch (final IOException e) {
            logger.error("error setting up directory watcher: ", e);
            return;
        }
        watcher.watch();
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(required = true, description = "path to a nanopublications directory")
        String directoryPath;
    }

    private final class NanopublicationsDirectoryWatcher implements DirectoryChangeListener {
        private final Set<Uri> currentNanopublicationUris = new HashSet<>();
        private final File directoryFile;
        private final Path directoryPath;
        private final DirectoryWatcher directoryWatcher;
        private final NanopublicationCrudApi nanopublicationCrudApi;
        private final CliNanopublicationParser nanopublicationParser;

        NanopublicationsDirectoryWatcher(final Path directoryPath, final ImmutableList<Nanopublication> initialNanopublications, final NanopublicationCrudApi nanopublicationCrudApi, final CliNanopublicationParser nanopublicationParser) throws IOException {
            this.directoryPath = checkNotNull(directoryPath);
            this.directoryFile = this.directoryPath.toFile();
            this.directoryWatcher = DirectoryWatcher.builder().path(this.directoryPath).listener(this).build();
            initialNanopublications.forEach(nanopublication -> currentNanopublicationUris.add(nanopublication.getUri()));
            this.nanopublicationCrudApi = checkNotNull(nanopublicationCrudApi);
            this.nanopublicationParser = checkNotNull(nanopublicationParser);
        }

        @Override
        public synchronized void onEvent(final DirectoryChangeEvent event) throws IOException {
            logger.info("directory change event: {}", event);

            final ImmutableList<Nanopublication> nanopublications = nanopublicationParser.parseDirectory(directoryPath.toFile());

            final Set<Uri> nanopublicationUris = new HashSet<>();
            nanopublications.forEach(nanopublication -> nanopublicationUris.add(nanopublication.getUri()));

            final Sets.SetView<Uri> newNanopublicationUris = Sets.difference(nanopublicationUris, currentNanopublicationUris);
            logger.info("new nanopublication URIs: {}", newNanopublicationUris);
            final Sets.SetView<Uri> deletedNanopublicationUris = Sets.difference(currentNanopublicationUris, nanopublicationUris);
            logger.info("deleted nanopublication URIs: {}", deletedNanopublicationUris);

            currentNanopublicationUris.clear();
            currentNanopublicationUris.addAll(nanopublicationUris);

            switch (event.eventType()) {
                case CREATE: {
                    if (newNanopublicationUris.isEmpty()) {
                        logger.info("no new nanopublication URIs, ignoring create");
                        return;
                    }
                    nanopublicationCrudApi.postNanopublications(nanopublications);
                    logger.info("posted {} nanopublications after creation", nanopublications.size());
                    break;
                }
                case MODIFY: {
                    nanopublicationCrudApi.postNanopublications(nanopublications);
                    logger.info("posted {} nanopublications after modification", nanopublications.size());
                    break;
                }
                case DELETE: {
                    if (deletedNanopublicationUris.isEmpty()) {
                        logger.info("no deleted nanopublication URIs, ignoring delete");
                        return;
                    }
                    nanopublicationCrudApi.deleteNanopublications(ImmutableList.copyOf(deletedNanopublicationUris));
                }
                default:
                    throw new UnsupportedOperationException(event.eventType().toString());
            }
        }

        public void watch() {
            logger.info("watching {} for nanopublication changes", directoryPath);
            directoryWatcher.watch();
        }
    }
}
