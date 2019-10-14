package edu.rpi.tw.twks.server.resource.nanopublication;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.net.URLEncoder;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class NanopublicationResourceTest extends AbstractResourceTest {
    private static String toTrigString(final Nanopublication nanopublication) {
        final Dataset dataset = nanopublication.toDataset();
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dataset, Lang.TRIG);
        return stringWriter.toString();
    }

    private static String toTrigString(final Model model) {
        final StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.TRIG);
        return stringWriter.toString();
    }

    private static Entity<String> toTrigEntity(final Nanopublication nanopublication) {
        return Entity.entity(toTrigString(nanopublication), Lang.TRIG.getContentType().getContentType());
    }

    private static Entity<String> toTrigEntity(final Model model) {
        return Entity.entity(toTrigString(model), Lang.TRIG.getContentType().getContentType());
    }

    @Test
    public void testDeleteNanopublicationPresent() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final Response response =
                target()
                        .path("/nanopublication/")
                        .path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8"))
                        .request(Lang.TRIG.getContentType().getContentType())
                        .delete();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testDeleteNanopublicationAbsent() throws Exception {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8"))
                        .request(Lang.TRIG.getContentType().getContentType())
                        .delete();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetNanopublicationPresent() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final String responseBody = target().path("/nanopublication/").path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8")).request(Lang.TRIG.getContentType().getContentType()).get(String.class);
        assertEquals(toTrigString(getTestData().specNanopublication), responseBody);
    }

    @Test
    public void testGetNanopublicationAbsent() throws Exception {
        final Response response = target()
                .path("/nanopublication/")
                .path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8"))
                .request(Lang.TRIG.getContentType().getContentType())
                .get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Override
    protected Object newResource(final Twks twks) {
        return new NanopublicationResource(twks);
    }

    @Test
    public void testPutNanopublicationWithUri() throws Exception {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8"))
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testPutNanopublicationWithoutUri() throws Exception {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testPutAssertionsWithoutUri() {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication.getAssertion().getModel()));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testPutAssertionsWithUri() throws Exception {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .path(URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8"))
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication.getAssertion().getModel()));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }
}
