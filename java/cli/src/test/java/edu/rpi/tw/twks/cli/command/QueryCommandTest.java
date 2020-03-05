package edu.rpi.tw.twks.cli.command;

import com.google.common.base.Charsets;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class QueryCommandTest extends AbstractCommandTest<QueryCommand> {
    private final static String ASSERTIONS_CONSTRUCT_QUERY = "CONSTRUCT WHERE { ?s ?p ?o }";
    private final static String ASSERTIONS_SELECT_QUERY = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
    private final static String NANOPUBLICATIONS_CONSTRUCT_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }";
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

    private String runCommandAndReturnOutput() throws IOException {
        final PrintStream systemOut = System.out;
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(bos, true, Charsets.UTF_8.name()));
            runCommand();
            return new String(bos.toByteArray());
        } finally {
            System.setOut(systemOut);
        }
    }

    private Model runConstruct(final Lang lang) throws IOException {
        final String output = runCommandAndReturnOutput();
        final Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader(output), "http://example.com", lang.getName());
        return model;
    }

    private Model runConstruct() throws IOException {
        return runConstruct(Lang.TRIG);
    }

    @Test
    public void testFormatParameter() throws Exception {
        command.getArgs().format = Lang.NT.getName();
        command.getArgs().query = ASSERTIONS_CONSTRUCT_QUERY;
        final Model output = runConstruct(Lang.NT);
        assertTrue(nanopublication.getAssertion().getModel().isIsomorphicWith(output));
    }

    @Test
    public void testQueryNanopublications() throws Exception {
        command.getArgs().nanopublications = true;
        command.getArgs().query = NANOPUBLICATIONS_CONSTRUCT_QUERY;
        final Model actual = runConstruct();
//        final Model expected = ModelFactory.createDefaultModel();
//        expected.add(nanopublication.getAssertion().getModel());
//        expected.add(nanopublication.getProvenance().getModel());
//        expected.add(nanopublication.getPublicationInfo().getModel());
//        assertTrue(expected.isIsomorphicWith(actual));
        assertTrue(actual.size() > nanopublication.getAssertion().getModel().size());
    }

    @Test
    public void testQueryParameter() throws Exception {
        command.getArgs().query = ASSERTIONS_CONSTRUCT_QUERY;
        final Model output = runConstruct();
        assertTrue(nanopublication.getAssertion().getModel().isIsomorphicWith(output));
    }

    @Test
    public void testQueryStdin() throws Exception {
        final InputStream stdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream(ASSERTIONS_CONSTRUCT_QUERY.getBytes(Charsets.UTF_8)));
            final Model output = runConstruct();
            assertTrue(nanopublication.getAssertion().getModel().isIsomorphicWith(output));
        } finally {
            System.setIn(stdin);
        }
    }

    @Test
    public void testSelect() throws IOException {
        command.getArgs().query = ASSERTIONS_SELECT_QUERY;
        final String output = runCommandAndReturnOutput();
        final String[] lines = output.split("\\n");
        assertEquals(2, lines.length);
        final Statement statement = nanopublication.getAssertion().getModel().listStatements().toList().get(0);
        assertEquals(String.format("%s,%s,%s", statement.getSubject().getURI(), statement.getPredicate().getURI(), statement.getObject().asResource().getURI()), lines[1].trim());
    }
}
