package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class QueryCommand extends Command {
    private final static String[] ALIASES = {};
    private final static String NAME = "query";
    private final static Logger logger = LoggerFactory.getLogger(QueryCommand.class);
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
    public void run(final TwksClient client, final MetricRegistry metricRegistry) {
        final String queryString;
        if (args.query != null) {
            queryString = args.query;
        } else {
            try {
                queryString = IOUtils.toString(System.in);
            } catch (final IOException e) {
                logger.error("error reading query: ", e);
                return;
            }
        }

        @Nullable Lang lang = null;
        if (args.format != null) {
            lang = RDFLanguages.shortnameToLang(args.format);
        }

        final Query query = QueryFactory.create(queryString);
        try (final QueryExecution queryExecution = args.nanopublications ? client.queryNanopublications(query) : client.queryAssertions(query)) {
            switch (query.queryType()) {
                case ASK: {
                    final boolean result = queryExecution.execAsk();
                    System.exit(result ? 0 : 1);
                    return;
                }
                case CONSTRUCT:
                case DESCRIBE: {
                    final Model result = query.queryType() == QueryType.CONSTRUCT ? queryExecution.execConstruct() : queryExecution.execDescribe();
                    if (lang == null) {
                        lang = RDFLanguages.TRIG;
                    }
                    result.write(System.out, lang.getName());
                    return;
                }
                case SELECT: {
                    final ResultSet resultSet = queryExecution.execSelect();
                    if (lang == null) {
                        lang = ResultSetLang.SPARQLResultSetCSV;
                    }
                    ResultSetFormatter.output(System.out, resultSet, lang);
                    return;
                }
                default:
                    throw new UnsupportedOperationException("query type " + query.queryType());
            }
        }
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(names = {"-f", "--format"}, description = "RDF format to print results in")
        String format = null;

        @Parameter(names = {"--nanopublications"}, description = "query nanopublications rather than assertions (defaults to false)")
        boolean nanopublications = false;

        @Parameter(description = "SPARQL query; if not specified, read from stdin")
        @Nullable
        String query = null;
    }
}
