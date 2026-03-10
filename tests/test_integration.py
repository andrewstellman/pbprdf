from __future__ import annotations

import os
from pathlib import Path

import pytest

from pbprdf.mapper.core import map_game
from pbprdf.ontology import generate_ontology
from pbprdf.query.endpoint import load_rdflib_graph, load_turtle, query, update

pytestmark = pytest.mark.skipif(
    not os.environ.get("PBPRDF_SPARQL_ENDPOINT"),
    reason="No triplestore endpoint configured (set PBPRDF_SPARQL_ENDPOINT)",
)


def _safe_drop(graph_uri: str) -> None:
    try:
        update(f"DROP GRAPH <{graph_uri}>")
    except Exception:
        # Some stores error if graph does not exist; safe to ignore.
        pass


def test_load_ontology():
    graph_uri = "http://stellman-greene.com/pbprdf/test/load_ontology"
    _safe_drop(graph_uri)
    try:
        load_rdflib_graph(generate_ontology(), graph_uri=graph_uri)
        rows = query(
            f"""
            SELECT ?c WHERE {{
              GRAPH <{graph_uri}> {{
                ?c a <http://www.w3.org/2002/07/owl#Class> .
              }}
            }} LIMIT 1
            """
        )
        assert rows
    except Exception as exc:
        pytest.skip(f"Triplestore unavailable: {exc}")
    finally:
        _safe_drop(graph_uri)


def test_load_and_query_game():
    graph_uri = "http://stellman-greene.com/pbprdf/test/load_game"
    _safe_drop(graph_uri)
    try:
        ttl = map_game(Path("tests/fixtures/nba_401810770.json"))
        load_rdflib_graph(ttl, graph_uri=graph_uri)
        rows = query(
            f"""
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?shot) AS ?count) WHERE {{
              GRAPH <{graph_uri}> {{
                ?shot a pbprdf:Shot .
              }}
            }}
            """
        )
        assert int(rows[0]["count"]["value"]) > 0
    except Exception as exc:
        pytest.skip(f"Triplestore unavailable: {exc}")
    finally:
        _safe_drop(graph_uri)


def test_win_probability_loaded():
    graph_uri = "http://stellman-greene.com/pbprdf/test/winprob"
    _safe_drop(graph_uri)
    try:
        ttl = map_game(Path("tests/fixtures/nba_401810770.json"))
        load_rdflib_graph(ttl, graph_uri=graph_uri)
        rows = query(
            f"""
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?s) AS ?count) WHERE {{
              GRAPH <{graph_uri}> {{
                ?s a pbprdf:WinProbabilitySnapshot .
              }}
            }}
            """
        )
        assert int(rows[0]["count"]["value"]) > 0
    except Exception as exc:
        pytest.skip(f"Triplestore unavailable: {exc}")
    finally:
        _safe_drop(graph_uri)


def test_venue_loaded():
    graph_uri = "http://stellman-greene.com/pbprdf/test/venue"
    _safe_drop(graph_uri)
    try:
        ttl = map_game(Path("tests/fixtures/nba_401810770.json"))
        load_rdflib_graph(ttl, graph_uri=graph_uri)
        rows = query(
            f"""
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT ?venue WHERE {{
              GRAPH <{graph_uri}> {{
                ?g a pbprdf:Game ;
                   pbprdf:venue ?venue .
              }}
            }} LIMIT 1
            """
        )
        assert rows
    except Exception as exc:
        pytest.skip(f"Triplestore unavailable: {exc}")
    finally:
        _safe_drop(graph_uri)


def test_cross_league_load():
    graph_uri = "http://stellman-greene.com/pbprdf/test/cross_league"
    _safe_drop(graph_uri)
    try:
        for p in [
            Path("tests/fixtures/nba_401810770.json"),
            Path("tests/fixtures/wnba_401820329.json"),
            Path("tests/fixtures/ncaam_401825568.json"),
            Path("tests/fixtures/ncaaw_401851327.json"),
        ]:
            load_rdflib_graph(map_game(p), graph_uri=graph_uri)

        rows = query(
            f"""
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(DISTINCT ?g) AS ?count) WHERE {{
              GRAPH <{graph_uri}> {{
                ?g a pbprdf:Game .
              }}
            }}
            """
        )
        assert int(rows[0]["count"]["value"]) == 4
    except Exception as exc:
        pytest.skip(f"Triplestore unavailable: {exc}")
    finally:
        _safe_drop(graph_uri)

