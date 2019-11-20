package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.google.common.collect.*;
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
import java.util.Map;
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

        final ImmutableMultimap<Path, Nanopublication> initialNanopublicationsByPath = nanopublicationParser.parseDirectory(directoryPath.toFile());
        apis.getNanopublicationCrudApi().postNanopublications(ImmutableList.copyOf(initialNanopublicationsByPath.values()));
        logger.info("posted {} initial nanopublications", initialNanopublicationsByPath.size());

        NanopublicationsDirectoryWatcher watcher = null;
        try {
            watcher = new NanopublicationsDirectoryWatcher(directoryPath, initialNanopublicationsByPath, apis.getNanopublicationCrudApi(), nanopublicationParser);
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
        private final Multimap<Path, Uri> currentNanopublicationUrisByPath = ArrayListMultimap.create();
        private final File directoryFile;
        private final Path directoryPath;
        private final DirectoryWatcher directoryWatcher;
        private final NanopublicationCrudApi nanopublicationCrudApi;
        private final CliNanopublicationParser nanopublicationParser;

        NanopublicationsDirectoryWatcher(final Path directoryPath, final ImmutableMultimap<Path, Nanopublication> initialNanopublicationsByPath, final NanopublicationCrudApi nanopublicationCrudApi, final CliNanopublicationParser nanopublicationParser) throws IOException {
            this.directoryPath = checkNotNull(directoryPath);
            this.directoryFile = this.directoryPath.toFile();
            this.directoryWatcher = DirectoryWatcher.builder().path(this.directoryPath).listener(this).build();
            for (final Map.Entry<Path, Nanopublication> entry : initialNanopublicationsByPath.entries()) {
                currentNanopublicationUrisByPath.put(entry.getKey(), entry.getValue().getUri());
            }
            this.nanopublicationCrudApi = checkNotNull(nanopublicationCrudApi);
            this.nanopublicationParser = checkNotNull(nanopublicationParser);
        }

        @Override
        public synchronized void onEvent(final DirectoryChangeEvent event) throws IOException {
            logger.info("directory change event: {}", event);

            final Set<Path> currentNanopublicationPaths = currentNanopublicationUrisByPath.keySet();
            final ImmutableMultimap<Path, Nanopublication> nanopublicationsByPath = nanopublicationParser.parseDirectory(directoryPath.toFile());

            final ImmutableSet<Path> nanopublicationPaths = nanopublicationsByPath.keySet();

            final ImmutableSet<Path> newNanopublicationPaths = Sets.difference(nanopublicationPaths, currentNanopublicationPaths).immutableCopy();
            logger.info("new nanopublication paths: {}", newNanopublicationPaths);
            final ImmutableSet<Path> deletedNanopublicationPaths = Sets.difference(currentNanopublicationPaths, nanopublicationPaths).immutableCopy();
            logger.info("deleted nanopublication paths: {}", deletedNanopublicationPaths);

            try {
                switch (event.eventType()) {
                    case CREATE:
                    case MODIFY: {
                        if (event.eventType() == DirectoryChangeEvent.EventType.CREATE && newNanopublicationPaths.isEmpty()) {
                            logger.info("no new nanopublication paths, ignoring {}", event.eventType());
                            return;
                        }

                        final ImmutableList<Nanopublication> nanopublications = ImmutableList.copyOf(nanopublicationsByPath.values());
                        final ImmutableList<Uri> nanopublicationUris = nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());

                        nanopublicationCrudApi.postNanopublications(nanopublications);
                        logger.info("posted {} nanopublications from {} files after {}: {}", nanopublications.size(), nanopublicationPaths.size(), event.eventType(), nanopublicationUris);
                        break;
                    }
                    case DELETE: {
                        if (deletedNanopublicationPaths.isEmpty()) {
                            logger.info("no deleted nanopublication paths, ignoring {}", event.eventType());
                            return;
                        }
                        final ImmutableList.Builder<Uri> deletedNanopublicationUrisBuilder = ImmutableList.builder();
                        for (final Path deletedNanopublicationPath : deletedNanopublicationPaths) {
                            deletedNanopublicationUrisBuilder.addAll(currentNanopublicationUrisByPath.get(deletedNanopublicationPath));
                        }
                        final ImmutableList<Uri> deletedNanopublicationUris = deletedNanopublicationUrisBuilder.build();
                        nanopublicationCrudApi.deleteNanopublications(deletedNanopublicationUris);
                        logger.info("deleted {} nanopublications from {} files after {}: {}", deletedNanopublicationUris.size(), deletedNanopublicationPaths.size(), event.eventType(), deletedNanopublicationUris);
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException(event.eventType().toString());
                }
            } finally {
                currentNanopublicationUrisByPath.clear();
                for (final Map.Entry<Path, Nanopublication> entry : nanopublicationsByPath.entries()) {
                    currentNanopublicationUrisByPath.put(entry.getKey(), entry.getValue().getUri());
                }
            }
        }

        public void watch() {
            logger.info("watching {} for nanopublication changes", directoryPath);
            directoryWatcher.watch();
        }
    }
}
