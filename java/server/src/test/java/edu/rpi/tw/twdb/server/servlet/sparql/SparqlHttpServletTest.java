package edu.rpi.tw.twdb.server.servlet.sparql;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import edu.rpi.tw.twdb.lib.Tdb2Twdb;
import edu.rpi.tw.twdb.lib.TestData;
import edu.rpi.tw.twdb.server.servlet.AbstractHttpServletTest;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SparqlHttpServletTest extends AbstractHttpServletTest {
    private Twdb db;
    private MockSparqlHttpServlet sut;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.db = new Tdb2Twdb();
        this.testData = new TestData();
        db.putNanopublication(testData.specNanopublication);
        this.sut = new MockSparqlHttpServlet();
    }

    @Test
    public void testGetSelect() throws Exception {
        final String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        final HttpServletRequest req = newMockHttpServletRequest("text/csv", queryString);
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

    private HttpServletRequest newMockHttpServletRequest(final String accept, final String query) {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("accept")).thenReturn(accept);
        when(req.getParameter("query")).thenReturn(query);
        return req;
    }

    private void verifyRequest(final HttpServletRequest req) {
        verify(req).getParameter("query");
        verify(req).getParameterValues("default-graph-uri");
        verify(req).getParameterValues("named-graph-uri");
        verify(req).getHeader("Accept");
    }

    private final class MockSparqlHttpServlet extends SparqlHttpServlet {
        private Query query;

        MockSparqlHttpServlet() {
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
