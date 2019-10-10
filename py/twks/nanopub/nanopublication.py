import rdflib


class Nanopublication:
    """
    Nanopublication Guidelines-compliant nanopublication class.
    http://nanopub.org/guidelines/working_draft/

    This class wraps the [rdflib](https://github.com/RDFLib/rdflib) ConjunctiveGraph. This class is designed to be opaque and immutable, parsing and serializing well-formed nanopublications but not exposing the underlying ConjunctiveGraph. Manipulation of the graph should happen elsewhere.
    """

    __NANOPUB_NAMESPACE = rdflib.Namespace("http://www.nanopub.org/nschema#")
    __PROV_NAMESPACE = rdflib.Namespace("http://www.w3.org/ns/prov#")

    def __init__(self, conjunctive_graph: rdflib.ConjunctiveGraph):
        """
        Construct a nanopublication from a well-formed conjunctive graph, which contains the named graphs according to the guidelines.
        """
        self.__conjunctive_graph = conjunctive_graph
        self.__uri = self.__validate()

    def isomorphic(self, other):
        if not isinstance(other, Nanopublication):
            return False
        return self.__conjunctive_graph.isomorphic(other.__conjunctive_graph)

    @classmethod
    def parse(cls, *, format="trig", **kwds):
        """
        Parse a nanopublication.
        :param kwds: see ConjunctiveGraph.parse
        :return a new Nanopublication
        """
        conjunctive_graph = rdflib.ConjunctiveGraph()
        conjunctive_graph.parse(format=format, **kwds)
        return cls(conjunctive_graph)

    def __get_part_context(self, *, head_context: rdflib.Graph, nanopublication_uri: rdflib.URIRef,
                           part_property_name: str) -> rdflib.Graph:
        part_uri_triples = tuple(head_context.triples(
            (nanopublication_uri, self.__NANOPUB_NAMESPACE[part_property_name], None)))
        if len(part_uri_triples) == 0:
            raise ValueError("missing nanopub:%s statement" % part_property_name)
        elif len(part_uri_triples) > 1:
            raise ValueError("multiple nanopub:%s statements" % part_property_name)
        part_uri = part_uri_triples[0][2]
        if part_uri is None or not isinstance(part_uri, rdflib.URIRef):
            raise ValueError("nanopub:%s must refer to a URI-named graph" % part_property_name)
        part_context = self.__conjunctive_graph.get_context(part_uri)
        if part_context is None:
            raise ValueError("missing part named graph: %s" % part_uri)
        elif not len(part_context):
            raise ValueError("empty part named graph: %s" % part_uri)
        return part_context

    def serialize(self, *, encoding="utf-8", format="trig", **kwds) -> str:
        """
        Serialize the nanopublication.
        :param kwds: see ConjunctiveGraph.serialize
        """
        result = self.__conjunctive_graph.serialize(encoding=encoding, format=format, **kwds)
        if isinstance(result, bytes):
            result = result.decode("utf-8")
        return result

    def __validate(self):
        conjunctive_graph = self.__conjunctive_graph

        # All triples must be placed in one of [H] or [A] or [P] or [I]
        for _ in conjunctive_graph.quads((None, None, None, conjunctive_graph.identifier)):
            raise ValueError("must not contain quads in the default graph")

        contexts = tuple(conjunctive_graph.contexts())
        if len(contexts) != 4:
            raise ValueError("extraneous contexts")

        nanopublication_quads = tuple(
            conjunctive_graph.quads((None, rdflib.RDF["type"], self.__NANOPUB_NAMESPACE["Nanopublication"])))
        if len(nanopublication_quads) == 0:
            raise ValueError("no rdf:type nanopub:Nanopublication statement")
        elif len(nanopublication_quads) > 1:
            raise ValueError("more than one rdf:type nanopub:Nanopublication statement")

        nanopublication_uri = nanopublication_quads[0][0]
        if nanopublication_uri is None or not isinstance(nanopublication_uri, rdflib.URIRef):
            raise ValueError("nanopublication must be a URIRef")

        head_context = nanopublication_quads[0][3]
        if not isinstance(head_context.identifier, rdflib.URIRef):
            raise ValueError("head graph name must be a URIRef")

        # Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
        assertion_context = self.__get_part_context(head_context=head_context, nanopublication_uri=nanopublication_uri,
                                                    part_property_name="hasAssertion")
        # Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
        provenance_context = self.__get_part_context(head_context=head_context, nanopublication_uri=nanopublication_uri,
                                                     part_property_name="hasProvenance")
        # Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
        publication_info_context = self.__get_part_context(head_context=head_context,
                                                           nanopublication_uri=nanopublication_uri,
                                                           part_property_name="hasPublicationInfo")

        # The URIs for [N], [H], [A], [P], [I] must all be different
        uri_set = set()
        for uri in (
                nanopublication_uri, head_context.identifier, assertion_context.identifier,
                provenance_context.identifier,
                publication_info_context.identifier):
            if uri in uri_set:
                raise ValueError("duplicate named graph URI " + uri)
            uri_set.add(uri)

        # Triples in [P] have at least one reference to [A]
        satisfied = False
        if not any(provenance_context.predicate_objects(assertion_context.identifier)):
            raise ValueError("provenance must refer to the assertion part")

        # Triples in [I] have at least one reference to [N]
        if not any(publication_info_context.predicate_objects(nanopublication_uri)):
            raise ValueError("publication info must refer to the nanopublication")

        return nanopublication_uri

    @property
    def uri(self) -> rdflib.URIRef:
        return self.__uri
