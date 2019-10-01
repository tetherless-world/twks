package edu.rpi.tw.twdb.api;

import org.apache.jena.rdf.model.Model;

public final class NamedModel {
    private final Model model;
    private final String name;

    public NamedModel(final Model model, final String name) {
        this.model = model;
        this.name = name;
    }

    public Model getModel() {
        return model;
    }

    public String getName() {
        return name;
    }
}
