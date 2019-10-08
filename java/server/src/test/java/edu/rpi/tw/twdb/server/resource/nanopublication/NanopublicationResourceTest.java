package edu.rpi.tw.twdb.server.resource.nanopublication;

import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.AbstractResourceTest;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import java.io.StringWriter;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;

public final class NanopublicationResourceTest extends AbstractResourceTest {
    private static String toTrigString(final Nanopublication nanopublication) {
        final Dataset dataset = nanopublication.toDataset();
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dataset, Lang.TRIG);
        return stringWriter.toString();
    }

    @Test
    public void testGetNanopublicationPresent() throws Exception {
        getDb().putNanopublication(getTestData().specNanopublication);
        final Invocation.Builder invocationBuilder = target().path("/nanopublication/").path(URLEncoder.encode(Uris.toString(getTestData().specNanopublication.getUri()), "UTF-8")).request(Lang.TRIG.getContentType().getContentType());
        final String responseBody = invocationBuilder.get(String.class);
        assertEquals(toTrigString(getTestData().specNanopublication), responseBody);
    }

    @Override
    protected Object newResource(final Twdb db) {
        return new NanopublicationResource(db);
    }

    @Test
    public void testPutNanopublicationWithUri() {
    }
}
