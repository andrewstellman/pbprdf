from __future__ import annotations

from datetime import datetime
from typing import Any

from rdflib import Literal
from rdflib.namespace import RDF, RDFS, XSD
from rdflib.term import URIRef

from pbprdf.mapper.ids import GameContext, game_iri, team_iri
from pbprdf.ontology import PBPRDF


def _safe_int(value: Any, default: int) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def _safe_float(value: Any, default: float) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def _league_code(raw: dict[str, Any]) -> str | None:
    code = raw.get("header", {}).get("league", {}).get("abbreviation")
    if isinstance(code, str) and code:
        return code.upper()
    return None


def _competition(raw: dict[str, Any]) -> dict[str, Any]:
    competitions = raw.get("header", {}).get("competitions", [])
    return competitions[0] if competitions else {}


def _venue_location(raw: dict[str, Any]) -> str | None:
    venue = raw.get("gameInfo", {}).get("venue", {})
    if not isinstance(venue, dict):
        return None
    bits: list[str] = []
    if venue.get("fullName"):
        bits.append(str(venue["fullName"]))
    address = venue.get("address", {})
    if isinstance(address, dict):
        city = address.get("city")
        state = address.get("state")
        location_bits = [str(part) for part in [city, state] if part]
        if location_bits:
            bits.append(", ".join(location_bits))
    if not bits:
        return None
    return ", ".join(bits)


def _extract_home_away_team_ids(comp: dict[str, Any]) -> tuple[str | None, str | None]:
    home_team_id = None
    away_team_id = None
    for competitor in comp.get("competitors", []):
        home_away = competitor.get("homeAway")
        tid = competitor.get("team", {}).get("id")
        if not tid:
            continue
        if home_away == "home":
            home_team_id = str(tid)
        elif home_away == "away":
            away_team_id = str(tid)
    return home_team_id, away_team_id


def map_game(graph, raw: dict[str, Any]) -> GameContext:
    event_id = str(raw.get("header", {}).get("id") or raw.get("id") or "")
    if not event_id:
        raise ValueError("Summary response missing event id in header.id")

    comp = _competition(raw)
    home_team_id, away_team_id = _extract_home_away_team_ids(comp)
    g_iri = game_iri(event_id)

    away_name = "Away"
    home_name = "Home"
    for competitor in comp.get("competitors", []):
        name = (
            competitor.get("team", {}).get("displayName")
            or competitor.get("team", {}).get("name")
            or "Unknown Team"
        )
        if competitor.get("homeAway") == "away":
            away_name = name
        elif competitor.get("homeAway") == "home":
            home_name = name

    graph.add((g_iri, RDF.type, PBPRDF.Game))
    graph.add((g_iri, RDFS.label, Literal(f"{away_name} at {home_name}")))
    graph.add((g_iri, PBPRDF.espnEventId, Literal(event_id)))

    game_time = comp.get("date")
    if isinstance(game_time, str):
        try:
            datetime.fromisoformat(game_time.replace("Z", "+00:00"))
            graph.add((g_iri, PBPRDF.gameTime, Literal(game_time, datatype=XSD.dateTime)))
        except ValueError:
            pass

    location = _venue_location(raw)
    if location:
        graph.add((g_iri, PBPRDF.gameLocation, Literal(location)))

    if home_team_id:
        graph.add((g_iri, PBPRDF.homeTeam, team_iri(home_team_id)))
    if away_team_id:
        graph.add((g_iri, PBPRDF.awayTeam, team_iri(away_team_id)))

    fmt = raw.get("format", {})
    regulation = fmt.get("regulation", {}) if isinstance(fmt, dict) else {}
    overtime = fmt.get("overtime", {}) if isinstance(fmt, dict) else {}

    regulation_periods = _safe_int(regulation.get("periods"), 4)
    regulation_clock = _safe_float(regulation.get("clock"), 720.0)
    overtime_clock = _safe_float(overtime.get("clock"), 300.0)
    display_name = str(regulation.get("displayName") or "Quarter")
    period_type = "HALF" if "half" in display_name.lower() else "QUARTER"

    format_node = URIRef(f"{g_iri}/format")
    graph.add((format_node, RDF.type, PBPRDF.GameFormat))
    graph.add((g_iri, PBPRDF.hasGameFormat, format_node))
    graph.add((format_node, PBPRDF.regulationPeriodType, Literal(period_type)))
    graph.add((format_node, PBPRDF.regulationPeriodCount, Literal(regulation_periods)))
    graph.add((format_node, PBPRDF.regulationPeriodLengthMinutes, Literal(int(round(regulation_clock / 60.0)))))
    graph.add((format_node, PBPRDF.overtimePeriodLengthMinutes, Literal(int(round(overtime_clock / 60.0)))))
    league_code = _league_code(raw)
    if league_code:
        graph.add((format_node, PBPRDF.leagueCode, Literal(league_code)))

    return GameContext(
        game_id=event_id,
        game_iri=g_iri,
        home_team_id=home_team_id,
        away_team_id=away_team_id,
        format_node=format_node,
        league_code=league_code,
        regulation_periods=regulation_periods,
        regulation_clock_seconds=regulation_clock,
        overtime_clock_seconds=overtime_clock,
    )

