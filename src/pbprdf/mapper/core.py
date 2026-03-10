from __future__ import annotations

import json
from pathlib import Path

from rdflib import Graph
from rdflib.namespace import OWL, RDFS, XSD

from pbprdf.mapper.events import add_event_chain
from pbprdf.mapper.game import map_game as map_game_level
from pbprdf.mapper.plays import map_plays
from pbprdf.mapper.roster import map_roster
from pbprdf.mapper.venue import map_venue
from pbprdf.mapper.winprob import map_win_probability
from pbprdf.models.espn import SummaryResponse
from pbprdf.ontology import PBPRDF


def map_game_json(raw: dict) -> Graph:
    graph = Graph()
    graph.bind("pbprdf", PBPRDF)
    graph.bind("rdfs", RDFS)
    graph.bind("owl", OWL)
    graph.bind("xsd", XSD)

    summary = SummaryResponse.model_validate(raw)
    game_ctx = map_game_level(graph, raw)
    map_venue(game_ctx.game_iri, raw.get("gameInfo"), graph)
    roster_ctx = map_roster(graph, raw, game_ctx)
    records, play_id_to_iri = map_plays(graph, summary, game_ctx, roster_ctx)
    add_event_chain(graph, records)
    if summary.winprobability:
        map_win_probability(game_ctx.game_iri, play_id_to_iri, summary.winprobability, graph)
    return graph


def map_game(json_path: Path) -> Graph:
    raw = json.loads(json_path.read_text())
    return map_game_json(raw)

