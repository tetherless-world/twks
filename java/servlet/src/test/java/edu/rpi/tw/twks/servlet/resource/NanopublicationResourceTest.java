package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class NanopublicationResourceTest extends AbstractResourceTest {
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
    public void testDeleteNanopublicationsAbsent() throws Exception {
        final List<NanopublicationCrudApi.DeleteNanopublicationResult> results = target().path("/nanopublication/").queryParam("uri", getTestData().specNanopublication.getUri().toString(), getTestData().secondNanopublication.getUri().toString()).request(MediaType.APPLICATION_JSON).delete(new GenericType<List<NanopublicationCrudApi.DeleteNanopublicationResult>>() {
        });
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(1));
    }

    @Test
    public void testDeleteNanopublicationsMixed() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final List<NanopublicationCrudApi.DeleteNanopublicationResult> results = target().path("/nanopublication/").queryParam("uri", getTestData().specNanopublication.getUri().toString(), getTestData().secondNanopublication.getUri().toString()).request(MediaType.APPLICATION_JSON).delete(new GenericType<List<NanopublicationCrudApi.DeleteNanopublicationResult>>() {
        });
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(1));
    }

    @Test
    public void testDeleteNanopublicationsPresent() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        getTwks().putNanopublication(getTestData().secondNanopublication);
        final List<NanopublicationCrudApi.DeleteNanopublicationResult> results = target().path("/nanopublication/").queryParam("uri", getTestData().specNanopublication.getUri().toString(), getTestData().secondNanopublication.getUri().toString()).request(MediaType.APPLICATION_JSON).delete(new GenericType<List<NanopublicationCrudApi.DeleteNanopublicationResult>>() {
        });
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(1));
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

    @Test
    public void testGetNanopublicationPresent() throws Exception {
        final Nanopublication expected = getTestData().specNanopublication;
        getTwks().putNanopublication(expected);
        final String responseBody = target().path("/nanopublication/").path(URLEncoder.encode(expected.getUri().toString(), "UTF-8")).request(Lang.TRIG.getContentType().getContentType()).get(String.class);
        final Nanopublication actual = NanopublicationParser.SPECIFICATION.parseString(responseBody).get(0);
        assertTrue(expected.isIsomorphicWith(actual));
    }

    @Test
    public void testPostNanopublicationsAbsent() {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .post(toTrigEntity(getTestData().specNanopublication, getTestData().secondNanopublication));
        final List<NanopublicationCrudApi.PutNanopublicationResult> results = response.readEntity(new GenericType<List<NanopublicationCrudApi.PutNanopublicationResult>>() {
        });
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.CREATED, results.get(0));
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.CREATED, results.get(1));
    }

    @Test
    public void testPostNanopublicationsMixed() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .post(toTrigEntity(getTestData().specNanopublication, getTestData().secondNanopublication));
        final List<NanopublicationCrudApi.PutNanopublicationResult> results = response.readEntity(new GenericType<List<NanopublicationCrudApi.PutNanopublicationResult>>() {
        });
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, results.get(0));
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.CREATED, results.get(1));
    }

    @Test
    public void testPostNanopublicationsPresent() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        getTwks().putNanopublication(getTestData().secondNanopublication);
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .post(toTrigEntity(getTestData().specNanopublication, getTestData().secondNanopublication));
        final List<NanopublicationCrudApi.PutNanopublicationResult> results = response.readEntity(new GenericType<List<NanopublicationCrudApi.PutNanopublicationResult>>() {
        });
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, results.get(0));
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, results.get(1));
    }

    @Test
    public void testPutAssertions() {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication.getAssertion().getModel()));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        final String location = response.getHeaderString("Location");
        assertTrue(location.contains("/nanopublication/urn"));
        assertFalse(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testPutNanopublicationAbsent() throws Exception {
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        final String location = response.getHeaderString("Location");
        assertTrue(location.contains("/nanopublication/" + URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8")));
        assertTrue(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }

    @Test
    public void testPutNanopublicationPresent() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final Response response =
                target()
                        .path("/nanopublication/")
                        .request()
                        .put(toTrigEntity(getTestData().specNanopublication));
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        final String location = response.getHeaderString("Location");
        assertTrue(location.contains("/nanopublication/" + URLEncoder.encode(getTestData().specNanopublication.getUri().toString(), "UTF-8")));
        assertTrue(getTwks().getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
    }
}
