package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.google.common.collect.*;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
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
    private @Nullable
    NanopublicationsDirectoryWatcher watcher = null;

    @Override
    public final String[] getAliases() {
        return ALIASES;
    }

    @Override
    public final Args getArgs() {
        return args;
    }

    @Override
    public final String getName() {
        return NAME;
    }

    @Override
    public final void run(final TwksClient client) {
        final Path directoryPath = Paths.get(args.directoryPath);
        final NanopublicationParser nanopublicationParser = new CliNanopublicationParser(args);

        final ImmutableMultimap<Path, Nanopublication> initialNanopublicationsByPath = nanopublicationParser.parseDirectory(directoryPath.toFile());
        if (!initialNanopublicationsByPath.isEmpty()) {
            client.postNanopublications(ImmutableList.copyOf(initialNanopublicationsByPath.values()));
            logger.info("posted {} initial nanopublications", initialNanopublicationsByPath.size());
        }

        try {
            watcher = new NanopublicationsDirectoryWatcher(client, directoryPath, initialNanopublicationsByPath, nanopublicationParser);
        } catch (final IOException e) {
            logger.error("error setting up directory watcher: ", e);
            return;
        }
        watcher.watch();
    }

    public final void stop() {
        if (watcher == null) {
            return;
        }

        try {
            watcher.close();
        } catch (final IOException e) {
            logger.error("error closing directory watcher: ", e);
        }
        watcher = null;
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(required = true, description = "path to a nanopublications directory")
        String directoryPath;
    }

    private final class NanopublicationsDirectoryWatcher implements DirectoryChangeListener {
        private final TwksClient client;
        private final Multimap<Path, Uri> currentNanopublicationUrisByPath = ArrayListMultimap.create();
        private final File directoryFile;
        private final Path directoryPath;
        private final DirectoryWatcher directoryWatcher;
        private final NanopublicationParser nanopublicationParser;

        NanopublicationsDirectoryWatcher(final TwksClient client, final Path directoryPath, final ImmutableMultimap<Path, Nanopublication> initialNanopublicationsByPath, final NanopublicationParser nanopublicationParser) throws IOException {
            this.client = checkNotNull(client);
            this.directoryPath = checkNotNull(directoryPath);
            this.directoryFile = this.directoryPath.toFile();
            this.directoryWatcher = DirectoryWatcher.builder().path(this.directoryPath).listener(this).build();
            for (final Map.Entry<Path, Nanopublication> entry : initialNanopublicationsByPath.entries()) {
                currentNanopublicationUrisByPath.put(entry.getKey(), entry.getValue().getUri());
            }
            this.nanopublicationParser = checkNotNull(nanopublicationParser);
        }

        public final void close() throws IOException {
            directoryWatcher.close();
        }

        @Override
        public final synchronized void onEvent(final DirectoryChangeEvent event) throws IOException {
            logger.info("directory change event: {}", event);

            final Set<Path> currentNanopublicationPaths = currentNanopublicationUrisByPath.keySet();
            final ImmutableMultimap<Path, Nanopublication> nanopublicationsByPath = nanopublicationParser.parseDirectory(directoryPath.toFile());

            final ImmutableSet<Path> nanopublicationPaths = nanopublicationsByPath.keySet();

            // #130: we were ignoring new nanopublications found by scanning the file system on DELETE events.
            // Ignore the file system change event type and scan the file system every time.

            {
                final ImmutableSet<Path> newNanopublicationPaths = Sets.difference(nanopublicationPaths, currentNanopublicationPaths).immutableCopy();
                if (!newNanopublicationPaths.isEmpty()) {
                    logger.info("new nanopublication paths: {}", newNanopublicationPaths);
                    final ImmutableList.Builder<Nanopublication> newNanopublicationsBuilder = ImmutableList.builder();
                    for (final Path newNanopublicationPath : newNanopublicationPaths) {
                        newNanopublicationsBuilder.addAll(nanopublicationsByPath.get(newNanopublicationPath));
                    }
                    final ImmutableList<Nanopublication> newNanopublications = newNanopublicationsBuilder.build();
                    if (logger.isInfoEnabled()) {
                        final ImmutableList<Uri> newNanopublicationUris = newNanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
                        logger.info("posted {} nanopublications from {} files after {}: {}", newNanopublications.size(), nanopublicationPaths.size(), event.eventType(), newNanopublicationUris);
                    }
                    client.postNanopublications(newNanopublications);
                } else {
                    logger.debug("no new nanopublication paths");
                }
            }

            {
                final ImmutableSet<Path> deletedNanopublicationPaths = Sets.difference(currentNanopublicationPaths, nanopublicationPaths).immutableCopy();
                if (!deletedNanopublicationPaths.isEmpty()) {
                    logger.info("deleted nanopublication paths: {}", deletedNanopublicationPaths);
                    final ImmutableList.Builder<Uri> deletedNanopublicationUrisBuilder = ImmutableList.builder();
                    for (final Path deletedNanopublicationPath : deletedNanopublicationPaths) {
                        deletedNanopublicationUrisBuilder.addAll(currentNanopublicationUrisByPath.get(deletedNanopublicationPath));
                    }
                    final ImmutableList<Uri> deletedNanopublicationUris = deletedNanopublicationUrisBuilder.build();
                    client.deleteNanopublications(deletedNanopublicationUris);
                    logger.info("deleted {} nanopublications from {} files after {}: {}", deletedNanopublicationUris.size(), deletedNanopublicationPaths.size(), event.eventType(), deletedNanopublicationUris);
                } else {
                    logger.debug("no deleted nanopublication paths");
                }
            }

            currentNanopublicationUrisByPath.clear();
            for (final Map.Entry<Path, Nanopublication> entry : nanopublicationsByPath.entries()) {
                currentNanopublicationUrisByPath.put(entry.getKey(), entry.getValue().getUri());
            }
        }

        public final void watch() {
            logger.info("watching {} for nanopublication changes", directoryPath);
            try {
                directoryWatcher.watch();
            } catch (final ClosedWatchServiceException e) {
                logger.debug("watcher closed");
            }
            logger.info("exited watch of {} for nanopublication changes", directoryPath);
        }
    }
}
