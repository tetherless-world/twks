package edu.rpi.tw.twks.servlet;

import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaRange;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AcceptLists {
    public final static AcceptList OFFER_DATASET = toAcceptList(Lang.TRIG, Lang.NQ, Lang.NQUADS);

    public final static AcceptList OFFER_GRAPH = toAcceptList(Lang.RDFXML, Lang.NTRIPLES, Lang.NT, Lang.N3, Lang.TURTLE, Lang.TTL, Lang.JSONLD, Lang.RDFJSON, Lang.NQUADS, Lang.NQ, Lang.TRIG);

    private AcceptLists() {
    }

    public final static Lang calculateResponseLang(final Lang defaultResponseLang, final AcceptList offerAcceptList, final Optional<AcceptList> proposeAcceptList) {
        if (!proposeAcceptList.isPresent()) {
            return defaultResponseLang;
        }
        final MediaType respMediaType = AcceptList.match(proposeAcceptList.get(), offerAcceptList);
        if (respMediaType == null) {
            return defaultResponseLang;
        }
        final Lang respLang = RDFLanguages.contentTypeToLang(respMediaType.getContentType());
        if (respLang != null) {
            return respLang;
        } else {
            return defaultResponseLang;
        }
    }

    public final static AcceptList toAcceptList(final Lang... languages) {
        final List<MediaRange> mediaRanges = new ArrayList<>();
        for (final Lang lang : languages) {
            final String contentType = lang.getContentType().getContentType();
            mediaRanges.add(new MediaRange(contentType));
        }
        return new AcceptList(mediaRanges);
    }
}
