package edu.rpi.tw.twdb.server.resource.nanopublication;

import edu.rpi.tw.nanopub.*;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.AcceptLists;
import edu.rpi.tw.twdb.server.resource.AbstractResource;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

@Path("nanopublication")
public class NanopublicationResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationResource.class);

    public NanopublicationResource() {
    }

    public NanopublicationResource(final Twdb db) {
        super(db);
    }

    @GET
    @Path("{nanopublicationUri}")
    public Response
    getNanopublication(
            @HeaderParam("Accept") @Nullable final String accept,
            @PathParam("nanopublicationUri") final String nanopublicationUriString
    ) {
        final Uri nanopublicationUri = Uris.parse(nanopublicationUriString);

        final Optional<Nanopublication> nanopublication = getDb().getNanopublication(nanopublicationUri);
        if (!nanopublication.isPresent()) {
            logger.info("nanopublication not found: {}", Uris.toString(nanopublicationUri));
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Lang responseLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_GRAPH, AcceptLists.getProposeAcceptList(accept));

        final Response.ResponseBuilder responseBuilder = Response.ok();
        responseBuilder.header("Content-Type", responseLang.getContentType().getContentType());
        final StringWriter responseStringWriter = new StringWriter();
        RDFDataMgr.write(responseStringWriter, nanopublication.get().toDataset(), responseLang);
        responseBuilder.entity(responseStringWriter.toString());

        return responseBuilder.build();
    }

    @PUT
    public Response
    putNanopublication(
            @HeaderParam("Content-Type") @Nullable final String contentType,
            @HeaderParam("X-Nanopublication-Dialect") @Nullable final String nanopublicationDialectString,
            final String requestBody,
            @Context final UriInfo uriInfo
    ) {
        return putNanopublication(contentType, nanopublicationDialectString, Optional.empty(), requestBody, uriInfo);
    }

    @PUT
    @Path("{nanopublicationUri}")
    public Response
    putNanopublicationWithUri(
            @HeaderParam("Content-Type") @Nullable final String contentType,
            @HeaderParam("X-Nanopublication-Dialect") @Nullable final String nanopublicationDialectString,
            @PathParam("nanopublicationUri") final String nanopublicationUriString,
            final String requestBody,
            @Context final UriInfo uriInfo
    ) {
        final Uri nanopublicationUri = Uris.parse(nanopublicationUriString);
        return putNanopublication(contentType, nanopublicationDialectString, Optional.of(nanopublicationUri), requestBody, uriInfo);
    }

    private Response
    putNanopublication(
            @Nullable final String contentType,
            @Nullable final String nanopublicationDialectString,
            final Optional<Uri> nanopublicationUri,
            final String requestBody,
            final UriInfo uriInfo
    ) {
        final Nanopublication nanopublication = parseNanopublication(contentType, nanopublicationDialectString, nanopublicationUri, requestBody);

        final Twdb.PutNanopublicationResult result = getDb().putNanopublication(nanopublication);
        switch (result) {
            case CREATED:
                return Response.created(uriInfo.getAbsolutePathBuilder().path(NanopublicationResource.class).path(Uris.toString(nanopublication.getUri())).build()).build();
            case OVERWROTE:
                return Response.noContent().build();
            default:
                throw new IllegalStateException();
        }
    }

    private Nanopublication parseNanopublication(
            @Nullable final String contentType,
            @Nullable final String nanopublicationDialectString,
            final Optional<Uri> nanopublicationUri,
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
            return parser.parse(new StringReader(requestBody), nanopublicationUri);
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.info("error parsing nanopublication: ", e);
            throw new WebApplicationException("Malformed nanopublication", Response.Status.BAD_REQUEST);
        }
    }

    private Lang parseLang(@Nullable final String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            throw new WebApplicationException("Missing Content-Type", Response.Status.BAD_REQUEST);
        }

        @Nullable final Lang lang = RDFLanguages.contentTypeToLang(contentType);
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

}
