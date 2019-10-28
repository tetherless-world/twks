package edu.rpi.tw.twks.client;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import edu.rpi.tw.twks.api.BulkReadApi;
import edu.rpi.tw.twks.api.BulkWriteApi;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.QueryApi;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

/**
 * Client for a TWKS server.
 */
public final class TwksClient implements BulkReadApi, BulkWriteApi, NanopublicationCrudApi, QueryApi {
    private final static Logger logger = LoggerFactory.getLogger(TwksClient.class);
    private final HttpRequestFactory httpRequestFactory;
    private final ApacheHttpTransport httpTransport;
    private final String serverBaseUrl;

    public TwksClient() {
        this(new TwksClientConfiguration());
    }

    /**
     * Construct a new TWKS client.
     */
    public TwksClient(final TwksClientConfiguration configuration) {
        this.serverBaseUrl = checkNotNull(configuration.getServerBaseUrl());
        httpTransport = new ApacheHttpTransport();
        httpRequestFactory = httpTransport.createRequestFactory(request -> {
            request.setParser(new JsonObjectParser(new JacksonFactory()));
        });
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final HttpResponse response;
        try {
            response = httpRequestFactory.buildDeleteRequest(newNanopublicationUrl(uri)).execute();
        } catch (final HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                return DeleteNanopublicationResult.NOT_FOUND;
            } else {
                throw wrapException(e);
            }
        } catch (final IOException e) {
            throw wrapException(e);
        }
        checkState(response.getStatusCode() == 204);
        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        try {
            final GenericUrl url = new GenericUrl(serverBaseUrl + "/nanopublication/");
            url.set("uri", uris);
            final HttpResponse response = httpRequestFactory.buildDeleteRequest(url).execute();
            final List<String> resultStrings = (List<String>) response.parseAs(new TypeToken<List<String>>() {
            }.getType());
            return resultStrings.stream().map(resultString -> DeleteNanopublicationResult.valueOf(resultString)).collect(ImmutableList.toImmutableList());
        } catch (final HttpResponseException e) {
            throw wrapException(e);
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public void dump() {
        try {
            httpRequestFactory.buildPostRequest(new GenericUrl(serverBaseUrl + "/dump"), new EmptyContent()).execute();
        } catch (final HttpResponseException e) {
            throw wrapException(e);
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public final Model getAssertions() {
        try {
            final HttpResponse response = httpRequestFactory.buildGetRequest(new GenericUrl(serverBaseUrl + "/assertions")).setHeaders(new HttpHeaders().setAccept("text/trig")).execute();
            try (final InputStream inputStream = response.getContent()) {
                final Model model = ModelFactory.createDefaultModel();
                setNsPrefixes(model);
                RDFParserBuilder.create().source(inputStream).lang(Lang.TRIG).parse(model);
                return model;
            }
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        try {
            final HttpResponse response = httpRequestFactory.buildGetRequest(newNanopublicationUrl(uri)).setHeaders(new HttpHeaders().setAccept("text/trig")).execute();
            checkState(response.getStatusCode() == 200);
            try (final InputStream inputStream = response.getContent()) {
                final byte[] contentBytes = ByteStreams.toByteArray(inputStream);
                try {
                    return Optional.of(new NanopublicationParser().setLang(Lang.TRIG).parseOne(new StringReader(new String(contentBytes, "UTF-8"))));
                } catch (final MalformedNanopublicationException e) {
                    logger.error("malformed nanopublication from server: ", e);
                    return Optional.empty();
                }
            }
        } catch (final HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            } else {
                throw wrapException(e);
            }
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    private GenericUrl newNanopublicationUrl(final Uri nanopublicationUri) {
        try {
            return new GenericUrl(serverBaseUrl + "/nanopublication/" + URLEncoder.encode(nanopublicationUri.toString(), "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final StringWriter contentStringWriter = new StringWriter();
        RDFDataMgr.write(contentStringWriter, nanopublication.toDataset(), Lang.TRIG);
        final String contentString = contentStringWriter.toString();
        final byte[] contentBytes = contentString.getBytes(Charsets.UTF_8);

        final HttpResponse response;
        try {
            response = httpRequestFactory.buildPutRequest(new GenericUrl(serverBaseUrl + "/nanopublication/"), new ByteArrayContent("text/trig; charset=utf-8", contentBytes)).execute();
        } catch (final IOException e) {
            throw wrapException(e);
        }

        switch (response.getStatusCode()) {
            case 201:
                return PutNanopublicationResult.CREATED;
            case 204:
                return PutNanopublicationResult.OVERWROTE;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public final ImmutableList<PutNanopublicationResult> putNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        return QueryExecutionFactory.sparqlService(serverBaseUrl + "/sparql/assertions", query, httpTransport.getHttpClient());
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.sparqlService(serverBaseUrl + "/sparql/nanopublications", query, httpTransport.getHttpClient());
    }

    private RuntimeException wrapException(final IOException e) {
        return new RuntimeException(e);
    }
}
