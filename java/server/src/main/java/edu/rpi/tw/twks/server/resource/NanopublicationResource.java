package edu.rpi.tw.twks.server.resource;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationDialect;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.server.AcceptLists;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Path("nanopublication")
public class NanopublicationResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationResource.class);

    public NanopublicationResource() {
    }

    public NanopublicationResource(final Twks twks) {
        super(twks);
    }

    @DELETE
    @Path("{nanopublicationUri}")
    public Response
    deleteNanopublication(
            @PathParam("nanopublicationUri") final String nanopublicationUriString
    ) {
        final Uri nanopublicationUri = Uri.parse(nanopublicationUriString);

        final Twks.DeleteNanopublicationResult result = getTwks().deleteNanopublication(nanopublicationUri);

        switch (result) {
            case DELETED:
                return Response.noContent().build();
            case NOT_FOUND:
                return Response.status(Response.Status.NOT_FOUND).build();
            default:
                throw new IllegalStateException();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public List<NanopublicationCrudApi.DeleteNanopublicationResult>
    deleteNanopublications(
            @QueryParam("uri") final List<String> nanopublicationUriStrings
    ) {
        final ImmutableList<Uri> nanopublicationUris = nanopublicationUriStrings.stream().map(uriString -> Uri.parse(uriString)).collect(ImmutableList.toImmutableList());

        return getTwks().deleteNanopublications(nanopublicationUris);
    }

    @GET
    @Path("{nanopublicationUri}")
    public Response
    getNanopublication(
            @HeaderParam("Accept") @Nullable final String accept,
            @PathParam("nanopublicationUri") final String nanopublicationUriString
    ) {
        final Uri nanopublicationUri = Uri.parse(nanopublicationUriString);

        final Optional<Nanopublication> nanopublication = getTwks().getNanopublication(nanopublicationUri);
        if (!nanopublication.isPresent()) {
            logger.info("nanopublication not found: {}", nanopublicationUri);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Lang responseLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_DATASET, AcceptLists.getProposeAcceptList(accept));

        final Response.ResponseBuilder responseBuilder = Response.ok();
        responseBuilder.header("Content-Type", responseLang.getContentType().getContentType());
        final StringWriter responseStringWriter = new StringWriter();
        RDFDataMgr.write(responseStringWriter, nanopublication.get().toDataset(), responseLang);
        responseBuilder.entity(responseStringWriter.toString());

        return responseBuilder.build();
    }

    private Lang parseLang(@Nullable final String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            throw new WebApplicationException("Missing Content-Type", Response.Status.BAD_REQUEST);
        }

        final ContentType contentTypeParsed = ContentType.create(contentType);

        @Nullable final Lang lang = RDFLanguages.contentTypeToLang(contentTypeParsed);
        if (lang == null) {
            logger.error("non-RDF Content-Type: {}", contentType);
            throw new WebApplicationException("non-RDF Content-Type: " + contentType, Response.Status.BAD_REQUEST);
        }

        return lang;
    }

    private Optional<NanopublicationDialect> parseNanopublicationDialect(@Nullable final String nanopublicationDialectString) {
        if (nanopublicationDialectString == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(NanopublicationDialect.valueOf(nanopublicationDialectString.toUpperCase()));
        } catch (final IllegalArgumentException e) {
            throw new WebApplicationException("Unknown nanopublication dialect: " + nanopublicationDialectString, Response.Status.BAD_REQUEST);
        }
    }

    private ImmutableList<Nanopublication> parseNanopublications(
            @Nullable final String contentType,
            @Nullable final String nanopublicationDialectString,
            final String requestBody
    ) {
        final Lang lang = parseLang(contentType);

        final NanopublicationParser parser = new NanopublicationParser();
        parser.setLang(lang);

        final Optional<NanopublicationDialect> dialect = parseNanopublicationDialect(nanopublicationDialectString);
        if (dialect.isPresent()) {
            parser.setDialect(dialect.get());
        }

        try {
            return parser.parseAll(new StringReader(requestBody));
        } catch (final MalformedNanopublicationException e) {
            logger.info("error parsing nanopublication: ", e);
            throw new WebApplicationException("Malformed nanopublication", Response.Status.BAD_REQUEST);
        }
    }

    @PUT
    public Response
    putNanopublication(
            @HeaderParam("Content-Type") @Nullable final String contentType,
            @HeaderParam("X-Nanopublication-Dialect") @Nullable final String nanopublicationDialectString,
            final String requestBody,
            @Context final UriInfo uriInfo
    ) {
        final ImmutableList<Nanopublication> nanopublications = parseNanopublications(contentType, nanopublicationDialectString, requestBody);

        if (nanopublications.size() != 1) {
            return Response.status(400, "Only a single nanopublication can be PUT").build();
        }
        final Nanopublication nanopublication = nanopublications.get(0);

        final Twks.PutNanopublicationResult result = getTwks().putNanopublication(nanopublication);

        final URI location;
        try {
            location = uriInfo.getAbsolutePathBuilder().path(URLEncoder.encode(nanopublication.getUri().toString(), "UTF-8")).build();
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        switch (result) {
            case CREATED:
                return Response.created(location).build();
            case OVERWROTE:
                return Response.noContent().header("Location", location.toString()).build();
            default:
                throw new IllegalStateException();
        }
    }

}
