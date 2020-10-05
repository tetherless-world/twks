package edu.rpi.tw.twks.text;

import edu.rpi.tw.twks.abc.DatasetTwks;
import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.configuration.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTest;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public final class FullTextSearchableTwksTest extends TwksTest {
    private final static class FullTextSearchableDatasetTwksConfiguration extends TwksConfiguration {
        private FullTextSearchableDatasetTwksConfiguration(final Builder builder) {
            super(builder);
        }

        public static Builder builder() {
            return new Builder();
        }

        private final static class Builder extends TwksConfiguration.Builder<Builder, FullTextSearchableDatasetTwksConfiguration> {
            @Override
            public FullTextSearchableDatasetTwksConfiguration build() {
                return new FullTextSearchableDatasetTwksConfiguration(this);
            }
        }

        private FullTextSearchableDatasetTwksConfiguration() {
            super(new Builder());
        }
    }

    private final static class FullTextSearchableDatasetTwks extends DatasetTwks<FullTextSearchableDatasetTwksConfiguration, QuadStoreTwksMetrics> {
        private FullTextSearchableDatasetTwks(final FullTextSearchableDatasetTwksConfiguration configuration, final QuadStoreTwksMetrics metrics) {
            super(configuration, FullTextSearchableDatasetFactory.getInstance().createFullTextSearchableDataset(FullTextSearchConfiguration.builder().setEnable(true).build(), DatasetFactory.createTxnMem()), metrics);
        }

        @Override
        protected TwksTransaction _beginTransaction(final ReadWrite readWrite) {
            return new DatasetTwksTransaction<FullTextSearchableDatasetTwks, FullTextSearchableDatasetTwksConfiguration, QuadStoreTwksMetrics>(readWrite, this) {
            };
        }
    }

    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new FullTextSearchableDatasetTwks(FullTextSearchableDatasetTwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).build(), new QuadStoreTwksMetrics(getMetricRegistry()));
    }

    @Test
    public void testSearch() {
        final Twks sut = getSystemUnderTest();
        sut.putNanopublication(getTestData().searchableNanopublication);
        try (final TwksTransaction transaction = sut.beginTransaction(ReadWrite.READ)) {
            final List<Statement> assertionStatements = transaction.getAssertions().listStatements().toList();
            assertEquals(1, assertionStatements.size());
            try (final QueryExecution queryExecution = transaction.queryAssertions(QueryFactory.create("PREFIX text: <http://jena.apache.org/text#>\n" +
                    "\n" +
                    "SELECT ?s\n" +
                    "WHERE { \n" +
                    "    ?s text:query 'drug' \n" +
                    "}\n"))) {
                final ResultSet resultSet = queryExecution.execSelect();
                while (resultSet.hasNext()) {
                    final QuerySolution solution = resultSet.next();
                    final Resource subject = solution.getResource("s");
                    assertNotSame(subject, null);
                    assertEquals("http://example.org/trastuzumab", subject.getURI());
                    return;
                }
                fail();
            }
        }
    }

    @Test
    public void testSearchNoResults() {
        final Twks sut = getSystemUnderTest();
        sut.putNanopublication(getTestData().searchableNanopublication);
        try (final TwksTransaction transaction = sut.beginTransaction(ReadWrite.READ)) {
            final List<Statement> assertionStatements = transaction.getAssertions().listStatements().toList();
            assertEquals(1, assertionStatements.size());
            try (final QueryExecution queryExecution = transaction.queryAssertions(QueryFactory.create("PREFIX text: <http://jena.apache.org/text#>\n" +
                    "\n" +
                    "SELECT ?s\n" +
                    "WHERE { \n" +
                    "    ?s text:query 'whatever' \n" +
                    "}\n"))) {
                final ResultSet resultSet = queryExecution.execSelect();
                assertFalse(resultSet.hasNext());
            }
        }
    }

}
