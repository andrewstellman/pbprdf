from __future__ import annotations

from pbprdf.ontology import PBPRDF


def test_event_chain_links(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?e WHERE {
      ?e pbprdf:eventNumber ?n ;
         pbprdf:nextEvent ?next .
      ?next pbprdf:previousEvent ?e .
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_seconds_into_game_exists(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?e ?s WHERE {
      ?e pbprdf:secondsIntoGame ?s ;
         pbprdf:period 1 .
    } ORDER BY ?s LIMIT 1
    """
    rows = list(nba_graph.query(query))
    assert rows
    _, sec = rows[0]
    assert int(sec) >= 0

