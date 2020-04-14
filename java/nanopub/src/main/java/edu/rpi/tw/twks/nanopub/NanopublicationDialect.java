package edu.rpi.tw.twks.nanopub;

public abstract class NanopublicationDialect {
    // http://nanopub.org/guidelines/working_draft/
    public final static NanopublicationDialect SPECIFICATION = new SpecificationNanopublicationDialect();
    // https://github.com/tetherless-world/whyis
    public final static NanopublicationDialect WHYIS = new WhyisNanopublicationDialect();

    public static NanopublicationDialect valueOf(final String name) {
        switch (name.toUpperCase()) {
            case "SPECIFICATION":
                return SPECIFICATION;
            case "WHYIS":
                return WHYIS;
            default:
                throw new IllegalArgumentException(name);
        }
    }

    abstract boolean allowDefaultModelStatements();

    abstract boolean allowEmptyPart();

    abstract boolean allowPartUriReuse();
}
