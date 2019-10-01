package edu.rpi.tw.twdb.api;

import org.apache.jena.rdf.model.Model;
import org.dmfs.rfc3986.Uri;

public final class NamedModel {
    private final Model model;
    private final Uri name;

    public NamedModel(final Model model, final Uri name) {
        this.model = model;
        this.name = name;
    }

    public Model getModel() {
        return model;
    }

    public Uri getName() {
        return name;
    }
}
