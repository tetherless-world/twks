package edu.rpi.tw.twks.nanopub;

import org.apache.jena.riot.Lang;

public final class WhyisNanopublicationDialect extends NanopublicationDialect {
    @Override
    final boolean allowDefaultModelStatements() {
        return true;
    }

    @Override
    final boolean allowEmptyPart() {
        return true;
    }

    @Override
    final boolean allowPartUriReuse() {
        return true;
    }

    @Override
    public final Lang getDefaultLang() {
        return Lang.NQUADS;
    }
}
