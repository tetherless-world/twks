package edu.rpi.tw.twdb.api;

public interface NanopublicationBuilder {
    NanopublicationBuilder setAssertion(NamedModel model);

    NanopublicationBuilder setProvenance(NamedModel model);

    NanopublicationBuilder setPublicationInfo(NamedModel model);
}
