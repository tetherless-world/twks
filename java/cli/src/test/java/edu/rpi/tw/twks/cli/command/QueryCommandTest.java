package edu.rpi.tw.twks.cli.command;

import com.google.common.base.Charsets;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;

public final class QueryCommandTest extends AbstractCommandTest<QueryCommand> {
    private final static String ASSERTIONS_CONSTRUCT_QUERY = "CONSTRUCT WHERE { ?s ?p ?o }";
    private Nanopublication nanopublication;

    @Override
    protected QueryCommand newCommand() {
        return new QueryCommand();
    }

    @Before
    public void postNanopublication() throws MalformedNanopublicationException {
        nanopublication = NanopublicationParser.builder().setSource(new StringReader(TestData.SPEC_NANOPUBLICATION_TRIG)).build().parseOne();
        getTwks().putNanopublication(nanopublication);
    }

    private Model runConstruct() throws IOException {
        final PrintStream systemOut = System.out;
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(bos, true, Charsets.UTF_8.name()));
            runCommand();
            final String output = new String(bos.toByteArray());
            final Model model = ModelFactory.createDefaultModel();
            model.read(new StringReader(output), "http://example.com", Lang.TRIG.getName());
            return model;
        } finally {
            System.setOut(systemOut);
        }
    }

    @Test
    public void testQueryParameter() throws Exception {
        command.getArgs().query = ASSERTIONS_CONSTRUCT_QUERY;
        final Model output = runConstruct();
        assertTrue(nanopublication.getAssertion().getModel().isIsomorphicWith(output));
    }
}
