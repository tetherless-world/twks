package edu.rpi.tw.twks.client.rest;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.api.TwksVersion;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationRuntimeException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationDialect;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

/**
 * TWKS client implementation that communicates with the server via the latter's REST interface.
 */
public final class RestTwksClient implements TwksClient {
    private final static Logger logger = LoggerFactory.getLogger(RestTwksClient.class);
    private final HttpRequestFactory httpRequestFactory;
    private final ApacheHttpTransport httpTransport;
    private final String serverBaseUrl;

    /**
     * Construct a new TWKS client with a default configuration.
     */
    public RestTwksClient() {
        this(RestTwksClientConfiguration.builder().build());
    }

    /**
     * Construct a new TWKS client.
     */
    public RestTwksClient(final RestTwksClientConfiguration configuration) {
        this.serverBaseUrl = StringUtils.stripEnd(checkNotNull(configuration.getServerBaseUrl()), "/");
        httpTransport = new ApacheHttpTransport();
        httpRequestFactory = httpTransport.createRequestFactory(request -> {
            if (configuration.getClientConnectTimeoutMs().isPresent()) {
                request.setConnectTimeout(configuration.getClientConnectTimeoutMs().get());
            }
            if (configuration.getClientReadTimeoutMs().isPresent()) {
                request.setReadTimeout(configuration.getClientReadTimeoutMs().get());
            }
            if (configuration.getClientWriteTimeoutMs().isPresent()) {
                request.setWriteTimeout(configuration.getClientWriteTimeoutMs().get());
            }
            request.setParser(new JsonObjectParser(new JacksonFactory()));
        });
    }

    @Override
    public final void close() {
        try {
            httpTransport.shutdown();
        } catch (final IOException e) {
            logger.error("error shutting down HTTP transport: ", e);
        }
    }

    private static HttpContent toContent(final Nanopublication... nanopublications) {
        final StringWriter contentStringWriter = new StringWriter();
        final Dataset dataset = DatasetFactory.create();
        for (final Nanopublication nanopublication : nanopublications) {
            nanopublication.toDataset(dataset);
        }
        RDFDataMgr.write(contentStringWriter, dataset, Lang.TRIG);
        final String contentString = contentStringWriter.toString();
        final byte[] contentBytes = contentString.getBytes(Charsets.UTF_8);
        return new ByteArrayContent("text/trig; charset=utf-8", contentBytes);
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
    public final void deleteNanopublications() {
        throw new UnsupportedOperationException("not supported via the API");
    }

    @Override
    @SuppressWarnings("unchecked")
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
            return getAssertions(httpRequestFactory.buildGetRequest(new GenericUrl(serverBaseUrl + "/assertions")));
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    private Model getAssertions(final HttpRequest request) {
        request.setHeaders(new HttpHeaders().setAccept("text/trig"));
        try {
            final HttpResponse response = request.execute();
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
    public final TwksVersion getClientVersion() {
        return TwksLibraryVersion.getInstance();
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        try {
            final HttpResponse response = httpRequestFactory.buildGetRequest(newNanopublicationUrl(uri)).setHeaders(new HttpHeaders().setAccept("text/trig")).execute();
            checkState(response.getStatusCode() == 200);
            try (final InputStream inputStream = response.getContent()) {
                final Lang lang = RDFLanguages.contentTypeToLang(response.getContentType());
                final NanopublicationParser nanopublicationParser = NanopublicationParser.builder().setDialect(NanopublicationDialect.SPECIFICATION).setLang(lang).build();
                try {
                    return Optional.of(nanopublicationParser.parseInputStream(inputStream).get(0));
                } catch (final MalformedNanopublicationRuntimeException e) {
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

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        try {
            final GenericUrl url = new GenericUrl(serverBaseUrl + "/assertions/ontology");
            url.set("uri", ontologyUris);
            return getAssertions(httpRequestFactory.buildGetRequest(url));
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public final TwksVersion getServerVersion() {
        try {
            final HttpResponse response = httpRequestFactory.buildGetRequest(new GenericUrl(serverBaseUrl + "/version")).execute();
            final GenericJson json = response.parseAs(GenericJson.class);
            return new TwksVersion(((BigDecimal) json.get("incremental")).intValue(), ((BigDecimal) json.get("major")).intValue(), ((BigDecimal) json.get("minor")).intValue());
        } catch (final HttpResponseException e) {
            throw wrapException(e);
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
    public final ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        try {
            final GenericUrl url = new GenericUrl(serverBaseUrl + "/nanopublication/");
            final HttpResponse response = httpRequestFactory.buildPostRequest(url, toContent(nanopublications.toArray(new Nanopublication[nanopublications.size()]))).execute();
            final List<String> resultStrings = (List<String>) response.parseAs(new TypeToken<List<String>>() {
            }.getType());
            return resultStrings.stream().map(resultString -> PutNanopublicationResult.valueOf(resultString)).collect(ImmutableList.toImmutableList());
        } catch (final HttpResponseException e) {
            throw wrapException(e);
        } catch (final IOException e) {
            throw wrapException(e);
        }
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final HttpResponse response;
        try {
            response = httpRequestFactory.buildPutRequest(new GenericUrl(serverBaseUrl + "/nanopublication/"), toContent(nanopublication)).execute();
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
