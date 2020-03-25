package edu.rpi.tw.twks.cli;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationDialect;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public final class CliNanopublicationParser {
    private final static Logger logger = LoggerFactory.getLogger(CliNanopublicationParser.class);
    private final NanopublicationDialect dialect;
    private final Optional<Lang> lang;
    private final int retry;
    private final int retryDelayS;

    public CliNanopublicationParser(final Args args) {
        this.retry = args.retry >= 0 ? args.retry : 0;
        this.retryDelayS = args.retrayDelayS > 0 ? args.retrayDelayS : 1;

        NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
        if (args.dialect != null) {
            dialect = NanopublicationDialect.valueOf(args.dialect.toUpperCase());
        }
        this.dialect = dialect;

        Optional<Lang> lang = Optional.empty();
        if (args.lang != null) {
            lang = Optional.ofNullable(RDFLanguages.shortnameToLang(args.lang));
            if (!lang.isPresent()) {
                throw new IllegalArgumentException("invalid lang " + args.lang);
            }
        }
        this.lang = lang;
    }

//    public CliNanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
//        this.dialect = checkNotNull(dialect);
//        this.lang = checkNotNull(lang);
//    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    public final ImmutableList<Nanopublication> parseFile(final File sourceFilePath) {
        final int tryMax = (retry > 0 ? retry + 1 : 1);
        for (int tryI = 0; tryI < tryMax; tryI++) {
            try {
                final ImmutableList<Nanopublication> result = newNanopublicationParserBuilder().setSource(sourceFilePath).build().parseAll();
                if (logger.isDebugEnabled()) {
                    logger.debug("parsed {} nanopublications from {}: {}", result.size(), sourceFilePath, getNanopublicationUris(result));
                }
                return result;
            } catch (final RiotNotFoundException e) {
                logger.error("nanopublication file {} not found", sourceFilePath);
                return ImmutableList.of();
            } catch (final MalformedNanopublicationException e) {
                logger.error("error parsing {}: ", sourceFilePath, e);
                if (tryI + 1 < tryMax) {
                    logger.error("retrying {} parse after {} seconds", sourceFilePath, retryDelayS);
                    try {
                        Thread.sleep(retryDelayS);
                    } catch (final InterruptedException ex) {
                        return ImmutableList.of();
                    }
                }
            }
        }
        return ImmutableList.of();
    }

    public final ImmutableList<Nanopublication> parseUri(final Uri sourceUri) {
        try {
            final ImmutableList<Nanopublication> result = newNanopublicationParserBuilder().setSource(sourceUri).build().parseAll();
            if (logger.isDebugEnabled()) {
                logger.info("parsed {} nanopublications from {}: {}", result.size(), sourceUri, getNanopublicationUris(result));
            }
            return result;
        } catch (final MalformedNanopublicationException e) {
            logger.error("error parsing {}: ", sourceUri, e);
            return ImmutableList.of();
        }
    }

    public abstract static class Args {
        @Parameter(names = {"--dialect"}, description = "dialect of the nanopublication, such as SPECIFICATION or WHYIS")
        public String dialect = null;

        @Parameter(names = {"-l", "--lang"}, description = "language/format of the nanopublication file e.g., TRIG")
        public String lang = null;

        @Parameter(names = {"--retry-delay-s"}, description = "delay between retries")
        public int retrayDelayS = 1;
        @Parameter(names = {"--retry"}, description = "retry parsing a file n times after a short delay, defaults to no retries")
        public int retry = 0;
    }
}
