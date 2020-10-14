import urllib
from typing import Optional, Set
from urllib.error import HTTPError
from urllib.parse import quote, urlencode

import rdflib
from rdflib import URIRef
from rdflib.plugins.stores.sparqlstore import SPARQLStore

from twks.nanopub.nanopublication import Nanopublication


class TwksClient:
    """
    Client for the TWKS server.

    The client mirrors the primary TWKS API: CRUD operations on nanopublications, querying assertions and nanopublications via SPARQL.
    """

    def __init__(self, *, server_base_url=None):
        """
        Construct a TWKS client.
        :param server_base_url: base URL of the server, excluding path e.g., http://localhost:8080"
        """
        if not server_base_url:
            server_base_url = "http://localhost:8080"
        self.__server_base_url = server_base_url
        assertions_sparql_query_endpoint = server_base_url + "/sparql/assertions"
        self.assertions_sparql_store = SPARQLStore(endpoint=assertions_sparql_query_endpoint,
                                                   query_endpoint=assertions_sparql_query_endpoint)
        nanopublications_sparql_query_endpoint = server_base_url + "/sparql/nanopublications"
        self.nanopublications_sparql_store = SPARQLStore(endpoint=nanopublications_sparql_query_endpoint,
                                                         query_endpoint=nanopublications_sparql_query_endpoint)

    def delete_nanopublication(self, nanopublication_uri: str) -> bool:
        """
        Delete a nanopublication by its URI
        :param nanopublication_uri: nanopublication URI
        :return: True if the nanopublication was deleted, else False
        """

        request = urllib.request.Request(url=self.__nanopublication_url(nanopublication_uri), method="DELETE")

        try:
            with urllib.request.urlopen(request) as _:
                return True
        except HTTPError as e:
            if e.code == 404:
                return False
            else:
                raise

    def dump(self) -> None:
        """
        Tell the server to dump the contents of the store to its (local) disk.
        """

        request = urllib.request.Request(url=self.__server_base_url + "/dump", method="POST")

        with urllib.request.urlopen(request) as _:
            return

    def get_assertions(self, store='default') -> rdflib.Graph:
        """
        Get the union of all assertions in the store, as a new Graph.
        :param store: store for the returned Graph
        """

        request = urllib.request.Request(url=self.__server_base_url + "/assertions", headers={"Accept": "text/trig"},
                                         method="GET")

        with urllib.request.urlopen(request) as f:
            response_trig = f.read()
            result = rdflib.Graph(store=store)
            result.parse(format="trig",
                         data=response_trig)
            return result

    def get_nanopublication(self, nanopublication_uri: str) -> Optional[Nanopublication]:
        """
        Get a nanopublication by its URI.
        :param nanopublication_uri: nanopublication URI
        :return: the nanopublication if present, else None
        """

        request = urllib.request.Request(url=self.__nanopublication_url(nanopublication_uri),
                                         headers={"Accept": "text/trig"})

        try:
            with urllib.request.urlopen(request) as f:
                response_trig = f.read()
                return Nanopublication.parse(format="trig",
                                             data=response_trig)
        except HTTPError as e:
            if e.code == 404:
                return None
            else:
                raise

    def get_ontology_assertions(self, ontology_uris: Set[URIRef], store='default') -> rdflib.Graph:
        """
        Get the union of all assertions in the store, as a new Graph.
        :param store: store for the returned Graph
        """

        if not ontology_uris:
            return rdflib.Graph(store=store)

        url = self.__server_base_url + "/assertions/ontology?" + urlencode(
            tuple(("uri", str(ontology_uri)) for ontology_uri in ontology_uris))
        # print(url)

        request = urllib.request.Request(url=url,
                                         headers={"Accept": "text/trig"},
                                         method="GET")

        with urllib.request.urlopen(request) as f:
            response_trig = f.read()
            result = rdflib.Graph(store=store)
            result.parse(format="trig",
                         data=response_trig)
            return result

    def __nanopublication_url(self, nanopublication_uri: str) -> str:
        return self.__server_base_url + "/nanopublication/" + quote(str(nanopublication_uri), safe="")

    def put_nanopublication(self, nanopublication: Nanopublication) -> None:
        """
        Put a nanopublication.

        :param nanopublication: the nanopublication
        """

        request = urllib.request.Request(url=self.__server_base_url + "/nanopublication",
                                         data=nanopublication.serialize(format="trig").encode("utf-8"),
                                         headers={"Content-Type": "text/trig; charset=utf-8"}, method="PUT")
        with urllib.request.urlopen(request) as _:
            pass

    def query_assertions(self, query: str, **kwds):
        """
        Query (only) the assertions in the store.
        :param query: SPARQL query string
        :param kwds: see rdflib.SPARQLStore.query
        :return: depends on query type
        """
        return self.assertions_sparql_store.query(query=query, **kwds)

    def query_nanopublications(self, query: str, **kwds):
        """
        Query all nanopublications in the store.
        :param query: SPARQL query string
        :param kwds: see rdflib.SPARQLStore.query
        :return: depends on query type
        """
        return self.nanopublications_sparql_store.query(query=query, **kwds)
