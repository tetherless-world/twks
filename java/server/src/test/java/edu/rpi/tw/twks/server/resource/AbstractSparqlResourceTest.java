package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public final class AbstractSparqlResourceTest extends AbstractResourceTest {
    @Override
    protected Object newResource(final Twks twks) {
        return new AssertionsSparqlResource(twks);
    }

    @Test
    public void testGetSelectCsv() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);

        final String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";

        final String responseBody =
                target()
                        .path("/sparql/assertions")
                        .queryParam("query", URIUtil.encodeQuery(queryString))
                        .request(Lang.CSV.getContentType().getContentType())
                        .get(String.class);
        assertThat(responseBody, containsString("http://example.org/trastuzumab,http://example.org/is-indicated-for,http://example.org/breast-cancer"));
    }
}
