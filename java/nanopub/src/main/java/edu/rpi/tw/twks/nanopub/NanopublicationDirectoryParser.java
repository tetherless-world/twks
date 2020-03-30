package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Parser for a file system directory of nanopublications.
 * <p>
 * Wraps a NanopublicationParser because the signatures are very different than the former's parseX methods.
 */
public final class NanopublicationDirectoryParser {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationDirectoryParser.class);
    private final NanopublicationParser nanopublicationParser;

    public NanopublicationDirectoryParser(final NanopublicationParser nanopublicationParser) {
        this.nanopublicationParser = checkNotNull(nanopublicationParser);
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(File sourceDirectoryPath) throws MalformedNanopublicationRuntimeException {
        final ImmutableMultimap.Builder<Path, Nanopublication> resultBuilder = ImmutableMultimap.builder();
        if (nanopublicationParser.getDialect() == NanopublicationDialect.SPECIFICATION) {
            // Assume it's a directory where every .trig file is a nanopublication.
            final File[] sourceFiles = sourceDirectoryPath.listFiles();
            if (sourceFiles == null) {
                return ImmutableMultimap.of();
            }
            for (final File trigFile : sourceFiles) {
                if (!trigFile.isFile()) {
                    continue;
                }
                if (!trigFile.getName().endsWith(".trig")) {
                    continue;
                }
                resultBuilder.putAll(trigFile.toPath(), nanopublicationParser.parseFile(trigFile));
            }
        } else if (nanopublicationParser.getDialect() == NanopublicationDialect.WHYIS) {
            if (sourceDirectoryPath.getName().equals("data")) {
                sourceDirectoryPath = new File(sourceDirectoryPath, "nanopublications");
            }
            if (sourceDirectoryPath.getName().equals("nanopublications")) {
                // Trawl all of the subdirectories of /data/nanopublications
                final File[] nanopublicationSubdirectories = sourceDirectoryPath.listFiles();
                if (nanopublicationSubdirectories == null) {
                    return ImmutableMultimap.of();
                }

                for (final File nanopublicationSubdirectory : nanopublicationSubdirectories) {
                    if (!nanopublicationSubdirectory.isDirectory()) {
                        continue;
                    }
                    final File twksFile = new File(nanopublicationSubdirectory, "file.twks.trig");
                    // #106
                    // We've previously parsed this Whyis nanopublication and written in back as a spec-compliant nanopublication.
                    // The conversion has to create new urn:uuid: graph URIs, which means that subsequent conversions won't
                    // produce the same spec-compliant nanopublication. We cache the converted nanopublication on disk so
                    // re-parsing it always produces the same result.
                    if (twksFile.isFile()) {
                        resultBuilder.putAll(twksFile.toPath(), nanopublicationParser.parseFile(twksFile));
                    } else {
                        final File whyisFile = new File(nanopublicationSubdirectory, "file");
                        final ImmutableList<Nanopublication> twksNanopublications = nanopublicationParser.parseFile(whyisFile);
                        {
                            final Dataset dataset = DatasetFactory.create();
                            for (final Nanopublication nanopublication : twksNanopublications) {
                                nanopublication.toDataset(dataset);
                            }
                            try (final OutputStream twksFileOutputStream = new FileOutputStream(twksFile)) {
                                RDFDataMgr.write(twksFileOutputStream, dataset, Lang.TRIG);
                            } catch (final IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        resultBuilder.putAll(whyisFile.toPath(), twksNanopublications);
                    }
                }
            } else {
                // Assume the directory contains a single nanopublication
                final File file = new File(sourceDirectoryPath, "file");
                resultBuilder.putAll(file.toPath(), nanopublicationParser.parseFile(file));
            }
        }
        final ImmutableMultimap<Path, Nanopublication> result = resultBuilder.build();
        if (logger.isDebugEnabled()) {
            logger.debug("parsed {} nanopublications from {}", result.size(), sourceDirectoryPath);
        }
        return result;
    }
}
