package edu.rpi.tw.twks.client;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Client for a TWKS server.
 */
public final class TwksClient implements NanopublicationCrudApi {
    private final static Logger logger = LoggerFactory.getLogger(TwksClient.class);
    private final String baseUrl;
    private final NetHttpTransport httpTransport;
    private final HttpRequestFactory httpRequestFactory;

    public TwksClient() {
        this("http://localhost:8080");
    }

    /**
     * Construct a new TWKS client.
     *
     * @param baseUrl base URL of the server without a path e.g., http://localhost:8080
     */
    public TwksClient(final String baseUrl) {
        this.baseUrl = checkNotNull(baseUrl);
        httpTransport = new NetHttpTransport();
        httpRequestFactory = httpTransport.createRequestFactory();
    }

    @Override
    public DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final HttpResponse response;
        try {
            response = httpRequestFactory.buildDeleteRequest(newNanopublicationUrl(uri)).execute();
        } catch (final IOException e) {
            throw wrapException(e);
        }
        switch (response.getStatusCode()) {
            case 204:
                return DeleteNanopublicationResult.DELETED;
            case 404:
                return DeleteNanopublicationResult.NOT_FOUND;
            default:
                throw toException(response);
        }
    }

    private RuntimeException wrapException(final IOException e) {
        return new RuntimeException(e);
    }

    private RuntimeException toException(final HttpResponse errorResponse) {
        return new RuntimeException(String.format("%d %s", errorResponse.getStatusCode(), errorResponse.getStatusMessage()));
    }

    private GenericUrl newNanopublicationUrl(final Uri nanopublicationUri) {
        try {
            return new GenericUrl(baseUrl + "/nanopublication/" + URLEncoder.encode(nanopublicationUri.toString(), "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        try {
            final HttpResponse response = httpRequestFactory.buildGetRequest(newNanopublicationUrl(uri)).setHeaders(new HttpHeaders().setAccept("text/trig")).execute();
            switch (response.getStatusCode()) {
                case 200:
                    try (final InputStream inputStream = response.getContent()) {
                        final byte[] contentBytes = ByteStreams.toByteArray(inputStream);
                        try {
                            return Optional.of(new NanopublicationParser().setLang(Lang.TRIG).parse(new StringReader(new String(contentBytes, "UTF-8"))));
                        } catch (final MalformedNanopublicationException e) {
                            logger.error("malformed nanopublication from server: ", e);
                            return Optional.empty();
                        }
                    }
                case 404:
                    return Optional.empty();
                default:
                    throw toException(response);
            }
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final StringWriter contentStringWriter = new StringWriter();
        RDFDataMgr.write(contentStringWriter, nanopublication.toDataset(), Lang.TRIG);
        final String contentString = contentStringWriter.toString();
        final byte[] contentBytes = contentString.getBytes(Charsets.UTF_8);

        final HttpResponse response;
        try {
            response = httpRequestFactory.buildPutRequest(newNanopublicationUrl(nanopublication.getUri()), new ByteArrayContent("text/trig; charset=utf-8", contentBytes)).execute();
        } catch (final IOException e) {
            throw wrapException(e);
        }

        switch (response.getStatusCode()) {
            case 201:
                return PutNanopublicationResult.CREATED;
            case 204:
                return PutNanopublicationResult.OVERWROTE;
            default:
                throw toException(response);
        }
    }
}
