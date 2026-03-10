from __future__ import annotations

from pbprdf.query.endpoint import get_endpoint, get_graph_store_endpoint, get_update_endpoint


def test_get_endpoint_default(monkeypatch):
    monkeypatch.delenv("PBPRDF_SPARQL_ENDPOINT", raising=False)
    assert get_endpoint() == "http://localhost:3030/pbprdf/sparql"


def test_get_endpoint_from_env(monkeypatch):
    monkeypatch.setenv("PBPRDF_SPARQL_ENDPOINT", "http://example.com/sparql")
    assert get_endpoint() == "http://example.com/sparql"


def test_get_update_endpoint_fallback(monkeypatch):
    monkeypatch.setenv("PBPRDF_SPARQL_ENDPOINT", "http://example.com/sparql")
    monkeypatch.delenv("PBPRDF_SPARQL_UPDATE_ENDPOINT", raising=False)
    assert get_update_endpoint() == "http://example.com/sparql"


def test_get_graph_store_endpoint_fuseki(monkeypatch):
    monkeypatch.setenv("PBPRDF_SPARQL_ENDPOINT", "http://localhost:3030/pbprdf/sparql")
    monkeypatch.delenv("PBPRDF_GRAPH_STORE_ENDPOINT", raising=False)
    assert get_graph_store_endpoint() == "http://localhost:3030/pbprdf/data"

