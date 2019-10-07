package edu.rpi.tw.twdb.server.servlet.nanopublication;

import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.TestData;
import edu.rpi.tw.twdb.server.servlet.AbstractHttpServletTest;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public final class NanopublicationHttpServletTest extends AbstractHttpServletTest<NanopublicationHttpServlet> {
    private static String toTrigString(final Nanopublication nanopublication) {
        final Dataset dataset = nanopublication.toDataset();
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dataset, Lang.TRIG);
        return stringWriter.toString();
    }

    @Override
    protected NanopublicationHttpServlet _setUp(final Twdb db, final TestData testData) {
        return new NanopublicationHttpServlet(db);
    }

    private HttpServletRequest newMockHttpServletRequest(final Optional<String> accept) {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        if (accept.isPresent()) {
            when(req.getHeader("Accept")).thenReturn(accept.get());
        }
        return req;
    }

    @Test
    public void testGetPresent() throws Exception {
        db.putNanopublication(testData.specNanopublication);

        final HttpServletRequest req = newMockHttpServletRequest(Optional.of(Lang.TRIG.getContentType().getContentType()));
        when(req.getPathInfo()).thenReturn("/" + Uris.toString(testData.specNanopublication.getUri()));
        final HttpServletResponse resp = newMockHttpServletResponse();

        sut.doGet(req, resp);

        final String respBody = getMockHttpServletResponseBody(resp);
        assertEquals(toTrigString(testData.specNanopublication), respBody);
    }

    private void verifyRequest(final HttpServletRequest req) {
        verify(req).getHeader("Accept");
    }

}
