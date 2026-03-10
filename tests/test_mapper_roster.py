from __future__ import annotations

from rdflib.namespace import RDF

from pbprdf.ontology import PBPRDF


def test_roster_nodes_exist(nba_graph):
    rosters = list(nba_graph.subjects(RDF.type, PBPRDF.Roster))
    assert len(rosters) == 2
    for roster in rosters:
        assert (roster, PBPRDF.rosterTeam, None) in nba_graph
        assert (roster, PBPRDF.hasPlayer, None) in nba_graph


def test_player_and_team_iris_use_espn_ids(nba_graph):
    teams = list(nba_graph.subjects(RDF.type, PBPRDF.Team))
    players = list(nba_graph.subjects(RDF.type, PBPRDF.Player))
    assert teams
    assert players
    assert all("/teams/" in str(team) for team in teams)
    assert all("/players/" in str(player) for player in players)
    for team in teams[:5]:
        assert (team, PBPRDF.espnTeamId, None) in nba_graph
    for player in players[:5]:
        assert (player, PBPRDF.espnAthleteId, None) in nba_graph

