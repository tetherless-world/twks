package edu.rpi.tw.twdb.server.servlet.sparql;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import edu.rpi.tw.twdb.server.TestData;
import edu.rpi.tw.twdb.server.servlet.AbstractHttpServletTest;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class SparqlHttpServletTest extends AbstractHttpServletTest<SparqlHttpServletTest.MockSparqlHttpServlet> {
    @Override
    protected MockSparqlHttpServlet _setUp(final Twdb db, final TestData testData) {
        db.putNanopublication(testData.specNanopublication);
        return new MockSparqlHttpServlet(db);
    }

    @Test
    public void testGetSelectCsv() throws Exception {
        final String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.of("text/csv"), queryString);
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("SELECT  ?s ?p ?o\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("http://example.org/trastuzumab,http://example.org/is-indicated-for,http://example.org/breast-cancer"));
    }

    @Test
    public void testGetConstruct() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.of("text/turtle"), queryString);
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("CONSTRUCT \n" +
                "  { \n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetConstructWithDefaultGraph() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.of("text/turtle"), queryString);
        when(req.getParameterValues("default-graph-uri")).thenReturn(new String[]{"http://example.org/pubX#assertion"});
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("CONSTRUCT \n" +
                "  { \n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "FROM <http://example.org/pubX#assertion>\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetConstructWithNamedGraph() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.of("text/turtle"), queryString);
        when(req.getParameterValues("named-graph-uri")).thenReturn(new String[]{"http://example.org/pubX#assertion"});
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("CONSTRUCT \n" +
                "  { \n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "FROM NAMED <http://example.org/pubX#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetMissingQuery() throws Exception {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        assertEquals(400, getMockHttpServletErrorResponseCode(resp));
    }

    @Test
    public void testGetSelectNoAccept() throws Exception {
        final String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.empty(), queryString);
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("SELECT  ?s ?p ?o\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<uri>http://example.org/trastuzumab</uri>"));
    }

    @Test
    public void testPostDirect() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Accept")).thenReturn("text/turtle");
        when(req.getContentType()).thenReturn("application/sparql-query");
        setMockHttpServletRequestBody(req, queryString);

        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doPost(req, resp);

        verify(req).getHeader("Accept");

        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("CONSTRUCT \n" +
                "  { \n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testPostWithParameters() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest(Optional.of("text/turtle"), queryString);
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doPost(req, resp);

        verifyRequest(req);
        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals("CONSTRUCT \n" +
                "  { \n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "FROM <http://example.org/pub1#assertion>\n" +
                "WHERE\n" +
                "  { ?s  ?p  ?o }\n", sut.query.toString());
        assertThat(respBody, containsString("<http://example.org/trastuzumab>"));
    }

    private HttpServletRequest newMockHttpServletRequest(final Optional<String> accept, final String query) {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        if (accept.isPresent()) {
            when(req.getHeader("Accept")).thenReturn(accept.get());
        }
        when(req.getParameter("query")).thenReturn(query);
        return req;
    }

    private void verifyRequest(final HttpServletRequest req) {
        verify(req).getParameter("query");
        verify(req).getParameterValues("default-graph-uri");
        verify(req).getParameterValues("named-graph-uri");
        verify(req).getHeader("Accept");
    }

    public final class MockSparqlHttpServlet extends SparqlHttpServlet {
        private Query query;

        MockSparqlHttpServlet(final Twdb db) {
            super(db);
        }

        @Override
        protected QueryExecution query(final Query query, final TwdbTransaction transaction) {
            assertSame(null, this.query);
            this.query = query;
            return db.queryAssertions(query, transaction);
        }
    }
}
