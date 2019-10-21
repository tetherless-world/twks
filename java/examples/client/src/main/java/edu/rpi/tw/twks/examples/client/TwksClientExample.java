package edu.rpi.tw.twks.examples.client;

import edu.rpi.tw.twks.client.TwksClient;
import edu.rpi.tw.twks.client.TwksClientConfiguration;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import org.apache.jena.riot.Lang;

import java.io.IOException;
import java.io.StringReader;

public final class TwksClientExample {
    public final static String SPEC_NANOPUBLICATION_TRIG = "@prefix : <http://example.org/pub1#> .\n" +
            "@prefix ex: <http://example.org/> .\n" +
            "@prefix np:  <http://www.nanopub.org/nschema#> .\n" +
            "@prefix prov: <http://www.w3.org/ns/prov#> .\n" +
            "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n" +
            "\n" +
            ":head {\n" +
            "    ex:pub1 a np:Nanopublication .\n" +
            "    ex:pub1 np:hasAssertion :assertion .\n" +
            "    ex:pub1 np:hasProvenance :provenance .\n" +
            "    ex:pub1 np:hasPublicationInfo :pubInfo .\n" +
            "}\n" +
            "\n" +
            ":assertion {\n" +
            "    ex:trastuzumab ex:is-indicated-for ex:breast-cancer .\n" +
            "}\n" +
            "\n" +
            ":provenance {\n" +
            "    :assertion prov:generatedAtTime \"2012-02-03T14:38:00Z\"^^xsd:dateTime .\n" +
            "    :assertion prov:wasDerivedFrom :experiment .\n" +
            "    :assertion prov:wasAttributedTo :experimentScientist .\n" +
            "}\n" +
            "\n" +
            ":pubInfo {\n" +
            "    ex:pub1 prov:wasAttributedTo ex:paul .\n" +
            "    ex:pub1 prov:generatedAtTime \"2012-10-26T12:45:00Z\"^^xsd:dateTime .\n" +
            "}\n";

    public static void main(final String[] argv) throws IOException, MalformedNanopublicationException {
        final TwksClientConfiguration clientConfiguration = new TwksClientConfiguration();
        // Set the configuration from system properties. See that class for documentation.
        clientConfiguration.setFromSystemProperties();
        // Can also call setters directly on the configururation

        final TwksClient client = new TwksClient(clientConfiguration);

        // Parse a nanopublication to use
        // The parser may return more than one, but we know there's only one.
        final Nanopublication nanopublication = new NanopublicationParser().setLang(Lang.TRIG).parse(new StringReader(SPEC_NANOPUBLICATION_TRIG)).get(0);


    }
}
