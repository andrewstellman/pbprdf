from __future__ import annotations

from rdflib.namespace import RDF

from pbprdf.ontology import PBPRDF


def _count(graph, query: str) -> int:
    return int(list(graph.query(query))[0][0])


def test_shot_with_assist_and_involved_player(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?shot ?shooter ?assister WHERE {
      ?shot a pbprdf:Shot ;
            pbprdf:shotMade true ;
            pbprdf:shotBy ?shooter ;
            pbprdf:shotAssistedBy ?assister .
    } LIMIT 1
    """
    rows = list(nba_graph.query(query))
    assert rows
    shot, shooter, assister = rows[0]
    assert (shot, PBPRDF.involvedPlayer, shooter) in nba_graph
    assert (shot, PBPRDF.involvedPlayer, assister) in nba_graph
    assert (shot, PBPRDF.shotPoints, None) in nba_graph
    assert (shot, PBPRDF.scoreValue, None) in nba_graph


def test_missed_shot_no_assist(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?shot WHERE {
      ?shot a pbprdf:Shot ;
            pbprdf:shotMade false .
      FILTER NOT EXISTS { ?shot pbprdf:shotAssistedBy ?a }
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_block_play(ncaam_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?play WHERE {
      ?play a pbprdf:Block ;
            a pbprdf:Shot ;
            pbprdf:shotBlockedBy ?b .
    } LIMIT 1
    """
    assert list(ncaam_graph.query(query))


def test_foul_play(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?play WHERE {
      ?play a pbprdf:Foul ;
            pbprdf:foulCommittedBy ?p ;
            pbprdf:isShootingFoul true .
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_rebound_play(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?play WHERE {
      ?play a pbprdf:Rebound ;
            pbprdf:reboundedBy ?p ;
            pbprdf:isOffensive ?o .
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_turnover_with_steal(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?play WHERE {
      ?play a pbprdf:Turnover ;
            pbprdf:turnoverType ?t ;
            pbprdf:stolenBy ?s ;
            pbprdf:turnedOverBy ?p .
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_substitution_play(nba_graph):
    query = """
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?play WHERE {
      ?play a pbprdf:Enters ;
            pbprdf:playerEntering ?in ;
            pbprdf:playerExiting ?out .
    } LIMIT 1
    """
    assert list(nba_graph.query(query))


def test_coordinate_filtering_shots_have_coordinates_substitutions_do_not(nba_graph):
    shot_coords = _count(
        nba_graph,
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?shot) as ?c) WHERE {
          ?shot a pbprdf:Shot ;
                pbprdf:hasCoordinate ?coord .
          ?coord a pbprdf:CourtCoordinate .
        }
        """,
    )
    assert shot_coords > 0
    subs_with_coords = _count(
        nba_graph,
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?s) as ?c) WHERE {
          ?s a pbprdf:Enters ;
             pbprdf:hasCoordinate ?coord .
        }
        """,
    )
    assert subs_with_coords == 0


def test_cross_league_mapping_non_empty(nba_graph, wnba_graph, ncaam_graph, ncaaw_graph):
    for graph in [nba_graph, wnba_graph, ncaam_graph, ncaaw_graph]:
        assert len(graph) > 1000
        assert any(graph.triples((None, RDF.type, PBPRDF.Game)))

