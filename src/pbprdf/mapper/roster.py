from __future__ import annotations

from typing import Any

from rdflib import BNode, Literal
from rdflib.namespace import RDF, RDFS

from pbprdf.mapper.ids import GameContext, RosterContext, player_iri, team_iri
from pbprdf.ontology import PBPRDF


def _norm_name(name: str) -> str:
    return " ".join(name.strip().lower().split())


def map_roster(graph, raw: dict[str, Any], game_ctx: GameContext) -> RosterContext:
    team_iri_by_id: dict[str, any] = {}
    player_iri_by_id: dict[str, any] = {}
    player_team_id_by_player_id: dict[str, str] = {}
    player_iri_by_name_norm: dict[str, any] = {}

    home_roster = BNode()
    away_roster = BNode()
    graph.add((home_roster, RDF.type, PBPRDF.Roster))
    graph.add((away_roster, RDF.type, PBPRDF.Roster))
    graph.add((game_ctx.game_iri, PBPRDF.hasHomeTeamRoster, home_roster))
    graph.add((game_ctx.game_iri, PBPRDF.hasAwayTeamRoster, away_roster))

    # Team objects from competitors and boxscore.
    competitors = raw.get("header", {}).get("competitions", [{}])[0].get("competitors", [])
    for competitor in competitors:
        team = competitor.get("team", {})
        tid = team.get("id")
        if not tid:
            continue
        tid = str(tid)
        iri = team_iri(tid)
        team_iri_by_id[tid] = iri
        graph.add((iri, RDF.type, PBPRDF.Team))
        graph.add((iri, PBPRDF.espnTeamId, Literal(tid)))
        label = team.get("displayName") or team.get("name")
        if label:
            graph.add((iri, RDFS.label, Literal(label)))

    for team_entry in raw.get("boxscore", {}).get("teams", []):
        team = team_entry.get("team", {})
        tid = team.get("id")
        if not tid:
            continue
        tid = str(tid)
        iri = team_iri(tid)
        team_iri_by_id[tid] = iri
        graph.add((iri, RDF.type, PBPRDF.Team))
        graph.add((iri, PBPRDF.espnTeamId, Literal(tid)))
        label = team.get("displayName") or team.get("shortDisplayName") or team.get("name")
        if label:
            graph.add((iri, RDFS.label, Literal(label)))

    if game_ctx.home_team_id and game_ctx.home_team_id in team_iri_by_id:
        graph.add((home_roster, PBPRDF.rosterTeam, team_iri_by_id[game_ctx.home_team_id]))
        home_label = next(graph.objects(team_iri_by_id[game_ctx.home_team_id], RDFS.label), None)
        if home_label:
            graph.add((home_roster, RDFS.label, home_label))
    if game_ctx.away_team_id and game_ctx.away_team_id in team_iri_by_id:
        graph.add((away_roster, PBPRDF.rosterTeam, team_iri_by_id[game_ctx.away_team_id]))
        away_label = next(graph.objects(team_iri_by_id[game_ctx.away_team_id], RDFS.label), None)
        if away_label:
            graph.add((away_roster, RDFS.label, away_label))

    # Build players from boxscore.players stats rows.
    for player_entry in raw.get("boxscore", {}).get("players", []):
        team_info = player_entry.get("team", {})
        team_id = str(team_info.get("id")) if team_info.get("id") is not None else None
        stat_blocks = player_entry.get("statistics", [])
        for stat_block in stat_blocks:
            for athlete_row in stat_block.get("athletes", []):
                athlete = athlete_row.get("athlete", {})
                aid = athlete.get("id")
                if not aid:
                    continue
                aid = str(aid)
                p_iri = player_iri(aid)
                player_iri_by_id[aid] = p_iri
                graph.add((p_iri, RDF.type, PBPRDF.Player))
                graph.add((p_iri, PBPRDF.espnAthleteId, Literal(aid)))

                display_name = athlete.get("displayName")
                if display_name:
                    graph.add((p_iri, RDFS.label, Literal(display_name)))
                    player_iri_by_name_norm[_norm_name(display_name)] = p_iri

                if team_id:
                    player_team_id_by_player_id[aid] = team_id
                    if team_id == game_ctx.home_team_id:
                        graph.add((home_roster, PBPRDF.hasPlayer, p_iri))
                    elif team_id == game_ctx.away_team_id:
                        graph.add((away_roster, PBPRDF.hasPlayer, p_iri))

    return RosterContext(
        team_iri_by_id=team_iri_by_id,
        player_iri_by_id=player_iri_by_id,
        player_team_id_by_player_id=player_team_id_by_player_id,
        player_iri_by_name_norm=player_iri_by_name_norm,
        home_roster_node=home_roster,
        away_roster_node=away_roster,
    )

