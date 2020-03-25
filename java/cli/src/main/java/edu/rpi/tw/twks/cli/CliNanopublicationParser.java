package edu.rpi.tw.twks.cli;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationRuntimeException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationDialect;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public final class CliNanopublicationParser extends NanopublicationParser {
    private final static Logger logger = LoggerFactory.getLogger(CliNanopublicationParser.class);
    private final int retry;
    private final int retryDelayS;

    public CliNanopublicationParser(final Args args) {
        super(
                args.dialect != null ? NanopublicationDialect.valueOf(args.dialect.toUpperCase()) : NanopublicationDialect.SPECIFICATION,
                true,
                args.lang != null ? Optional.ofNullable(RDFLanguages.shortnameToLang(args.lang)) : Optional.empty()
        );
        this.retry = args.retry >= 0 ? args.retry : 0;
        this.retryDelayS = args.retrayDelayS > 0 ? args.retrayDelayS : 1;
    }

//    public CliNanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
//        this.dialect = checkNotNull(dialect);
//        this.lang = checkNotNull(lang);
//    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    @Override
    public final ImmutableList<Nanopublication> parseFile(final File sourceFilePath) {
        final int tryMax = (retry > 0 ? retry + 1 : 1);
        for (int tryI = 0; tryI < tryMax; tryI++) {
            try {
                final ImmutableList<Nanopublication> result = super.parseFile(sourceFilePath);
                if (!result.isEmpty()) {
                    return result;
                }
            } catch (final RiotNotFoundException e) {
                logger.error("nanopublication file {} not found", sourceFilePath);
                return ImmutableList.of();
            } catch (final MalformedNanopublicationRuntimeException e) {
            }

            logger.error("error parsing {}", sourceFilePath);
            if (tryI + 1 < tryMax) {
                logger.error("retrying {} parse after {} seconds", sourceFilePath, retryDelayS);
                try {
                    Thread.sleep(retryDelayS);
                } catch (final InterruptedException ex) {
                    return ImmutableList.of();
                }
            }
        }
        return ImmutableList.of();
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
