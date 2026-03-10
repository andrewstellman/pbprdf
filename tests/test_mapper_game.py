from __future__ import annotations

from rdflib.namespace import RDF, RDFS

from pbprdf.ontology import PBPRDF


def test_game_node_and_core_fields(nba_graph, nba_raw):
    event_id = nba_raw["header"]["id"]
    game = next(nba_graph.subjects(PBPRDF.espnEventId, None))
    assert (game, RDF.type, PBPRDF.Game) in nba_graph
    assert (game, PBPRDF.espnEventId, None) in nba_graph
    assert (game, PBPRDF.gameTime, None) in nba_graph
    assert (game, PBPRDF.homeTeam, None) in nba_graph
    assert (game, PBPRDF.awayTeam, None) in nba_graph
    assert str(next(nba_graph.objects(game, PBPRDF.espnEventId))) == str(event_id)
    assert (game, RDFS.label, None) in nba_graph


def test_game_format_values(nba_graph):
    fmt = next(nba_graph.subjects(RDF.type, PBPRDF.GameFormat))
    periods = int(next(nba_graph.objects(fmt, PBPRDF.regulationPeriodCount)))
    reg_minutes = int(next(nba_graph.objects(fmt, PBPRDF.regulationPeriodLengthMinutes)))
    ot_minutes = int(next(nba_graph.objects(fmt, PBPRDF.overtimePeriodLengthMinutes)))
    assert periods == 4
    assert reg_minutes == 12
    assert ot_minutes == 5

