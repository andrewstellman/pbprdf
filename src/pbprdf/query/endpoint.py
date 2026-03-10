"""
Triplestore-agnostic SPARQL query layer.

Uses SPARQLWrapper to talk to any SPARQL 1.1 compliant endpoint
over HTTP. Works identically against Fuseki, Blazegraph, or Virtuoso.
"""

from __future__ import annotations

import os
from pathlib import Path
from urllib.parse import urlencode

import httpx
from rdflib import Graph
from SPARQLWrapper import JSON, POST, SPARQLWrapper


def get_endpoint() -> str:
    return os.environ.get("PBPRDF_SPARQL_ENDPOINT", "http://localhost:3030/pbprdf/sparql")


def get_update_endpoint() -> str:
    return os.environ.get("PBPRDF_SPARQL_UPDATE_ENDPOINT", get_endpoint())


def get_graph_store_endpoint() -> str:
    explicit = os.environ.get("PBPRDF_GRAPH_STORE_ENDPOINT")
    if explicit:
        return explicit
    endpoint = get_endpoint()
    if endpoint.endswith("/sparql"):
        return endpoint[: -len("/sparql")] + "/data"
    return endpoint


def query(sparql_query: str) -> list[dict]:
    wrapper = SPARQLWrapper(get_endpoint())
    wrapper.setQuery(sparql_query)
    wrapper.setReturnFormat(JSON)
    result = wrapper.query().convert()
    return result.get("results", {}).get("bindings", [])


def query_ask(sparql_query: str) -> bool:
    wrapper = SPARQLWrapper(get_endpoint())
    wrapper.setQuery(sparql_query)
    wrapper.setReturnFormat(JSON)
    result = wrapper.query().convert()
    return bool(result.get("boolean", False))


def update(sparql_update: str) -> None:
    wrapper = SPARQLWrapper(get_update_endpoint())
    wrapper.setMethod(POST)
    wrapper.setQuery(sparql_update)
    wrapper.query()


def _graph_store_url(graph_uri: str | None) -> str:
    base = get_graph_store_endpoint()
    if graph_uri:
        return f"{base}?{urlencode({'graph': graph_uri})}"
    return f"{base}?default"


def load_turtle(turtle_path: Path, graph_uri: str | None = None) -> None:
    payload = turtle_path.read_bytes()
    url = _graph_store_url(graph_uri)
    with httpx.Client(timeout=30.0, trust_env=False) as client:
        response = client.post(url, content=payload, headers={"Content-Type": "text/turtle"})
    if response.status_code >= 400:
        raise RuntimeError(
            f"Failed to load Turtle to graph store endpoint.\n"
            f"URL: {url}\nStatus: {response.status_code}\nBody: {response.text}"
        )


def load_rdflib_graph(graph: Graph, graph_uri: str | None = None) -> None:
    payload = graph.serialize(format="turtle")
    if isinstance(payload, str):
        payload_bytes = payload.encode("utf-8")
    else:
        payload_bytes = payload
    url = _graph_store_url(graph_uri)
    with httpx.Client(timeout=30.0, trust_env=False) as client:
        response = client.post(url, content=payload_bytes, headers={"Content-Type": "text/turtle"})
    if response.status_code >= 400:
        raise RuntimeError(
            f"Failed to load rdflib graph to graph store endpoint.\n"
            f"URL: {url}\nStatus: {response.status_code}\nBody: {response.text}"
        )

