package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class AbstractSparqlResourceTest extends AbstractResourceTest {
    @Override
    protected Object newResource(final Twks twks) {
        return new AssertionsSparqlResource(twks);
    }

    @Test
    public void testGetConstruct() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final String responseBody =
                target()
                        .path("/sparql/assertions")
                        .queryParam("query", URIUtil.encodeQuery(queryString))
                        .request(Lang.TTL.getContentType().getContentType())
                        .get(String.class);
        assertThat(responseBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetConstructWithDefaultGraph() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final String responseBody =
                target()
                        .path("/sparql/assertions")
                        .queryParam("query", URIUtil.encodeQuery(queryString))
                        .queryParam("default-graph-uri", URIUtil.encodeQuery("http://example.org/pubX#assertion"))
                        .request(Lang.TTL.getContentType().getContentType())
                        .get(String.class);

        assertThat(responseBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetConstructWithNamedGraph() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final String responseBody =
                target()
                        .path("/sparql/assertions")
                        .queryParam("query", URIUtil.encodeQuery(queryString))
                        .queryParam("named-graph-uri", URIUtil.encodeQuery("http://example.org/pubX#assertion"))
                        .request(Lang.TTL.getContentType().getContentType())
                        .get(String.class);
        assertThat(responseBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testGetMissingQuery() throws Exception {
        final Response response =
                target()
                        .path("/sparql/assertions")
                        .request(Lang.TTL.getContentType().getContentType())
                        .get();

        assertEquals(400, response.getStatus());
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
        assertThat(responseBody, containsString("http://example.org/trastuzumab,http://example.org/isindicatedfor,http://example.org/breast-cancer"));
    }

    @Test
    public void testGetSelectNoAccept() throws Exception {
        final String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        final String responseBody =
                target()
                        .path("/sparql/assertions")
                        .queryParam("query", URIUtil.encodeQuery(queryString))
                        .request()
                        .get(String.class);
        assertThat(responseBody, containsString("<uri>http://example.org/trastuzumab</uri>"));
    }

    @Test
    public void testPostDirect() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final Response response = target()
                .path("/sparql/assertions")
                .request(Lang.TTL.getContentType().getContentType())
                .header("Content-Type", "application/sparql-query")
                .post(Entity.entity(queryString, Lang.TRIG.getContentType().getContentType()));
        final String responseBody = response.getEntity().toString();

        assertThat(responseBody, containsString("<http://example.org/trastuzumab>"));
    }

    @Test
    public void testPostWithParameters() throws Exception {
        final String queryString = "CONSTRUCT WHERE { ?s ?p ?o }";

        final Response response = target()
                .path("/sparql/assertions")
                .queryParam("query", URIUtil.encodeQuery(queryString))
                .request(Lang.TTL.getContentType().getContentType())
                .post(Entity.entity("", Lang.TRIG.getContentType().getContentType()));
        final String responseBody = response.getEntity().toString();

        assertThat(responseBody, containsString("<http://example.org/trastuzumab>"));
    }
}
