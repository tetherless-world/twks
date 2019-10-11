package edu.rpi.tw.twks.client;

import com.google.api.client.http.javanet.NetHttpTransport;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.SparqlQueryApi;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import java.util.Optional;

/**
 * Client for a TWKS server.
 */
public final class TwksClient implements NanopublicationCrudApi, SparqlQueryApi {
    private final NetHttpTransport httpTransport;

    public TwksClient() {
        httpTransport = new NetHttpTransport();
    }

    @Override
    public DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        return null;
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        return Optional.empty();
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        return null;
    }

    @Override
    public QueryExecution queryAssertions(final Query query) {
        return null;
    }

    @Override
    public QueryExecution queryNanopublications(final Query query) {
        return null;
    }
}
