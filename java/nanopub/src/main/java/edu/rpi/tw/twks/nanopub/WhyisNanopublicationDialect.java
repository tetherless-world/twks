package edu.rpi.tw.twks.nanopub;

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
}
