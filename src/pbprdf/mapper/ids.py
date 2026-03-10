from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

from rdflib import BNode, URIRef

from pbprdf.ontology import PBPRDF_ENTITY


@dataclass
class GameContext:
    game_id: str
    game_iri: URIRef
    home_team_id: Optional[str]
    away_team_id: Optional[str]
    format_node: URIRef
    league_code: Optional[str]
    regulation_periods: int
    regulation_clock_seconds: float
    overtime_clock_seconds: float


@dataclass
class RosterContext:
    team_iri_by_id: dict[str, URIRef]
    player_iri_by_id: dict[str, URIRef]
    player_team_id_by_player_id: dict[str, str]
    player_iri_by_name_norm: dict[str, URIRef]
    home_roster_node: BNode
    away_roster_node: BNode


def game_iri(event_id: str) -> URIRef:
    return URIRef(f"{PBPRDF_ENTITY}games/{event_id}")


def play_iri(event_id: str, play_id: str) -> URIRef:
    return URIRef(f"{PBPRDF_ENTITY}games/{event_id}/plays/{play_id}")


def team_iri(team_id: str) -> URIRef:
    return URIRef(f"{PBPRDF_ENTITY}teams/{team_id}")


def player_iri(player_id: str) -> URIRef:
    return URIRef(f"{PBPRDF_ENTITY}players/{player_id}")

