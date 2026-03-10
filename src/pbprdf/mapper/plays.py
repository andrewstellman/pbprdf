from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Iterable

from rdflib import Literal, URIRef
from rdflib.namespace import RDF, RDFS, XSD

from pbprdf.mapper.ids import GameContext, RosterContext, play_iri, player_iri
from pbprdf.models.espn import Play, SummaryResponse
from pbprdf.ontology import PBPRDF


@dataclass
class EventRecord:
    event_iri: URIRef
    sequence_value: int
    period: int
    seconds_left_in_period: int | None


SHOT_KEYWORDS = [
    "shot",
    "jumper",
    "jump shot",
    "jumpshot",
    "layup",
    "layupshot",
    "dunk",
    "freethrow",
    "free throw",
    "hook",
    "tip",
    "finger roll",
    "putback",
]


def _norm_spaces(text: str) -> str:
    return re.sub(r"\s+", " ", text.replace("\n", " ")).strip()


def _norm_key(text: str) -> str:
    lowered = _norm_spaces(text).lower()
    return re.sub(r"[\s\-_]+", "", lowered)


def _parse_clock_seconds_left(clock_display: str) -> int | None:
    m = re.match(r"^(\d+):(\d+)$", clock_display.strip())
    if m:
        minutes = int(m.group(1))
        seconds = int(m.group(2))
        return minutes * 60 + seconds
    m = re.match(r"^(\d+)\.(\d+)$", clock_display.strip())
    if m:
        return int(m.group(1))
    return None


def _compute_seconds(period: int, clock_display: str, game_ctx: GameContext) -> tuple[int | None, int | None]:
    seconds_left = _parse_clock_seconds_left(clock_display)
    if seconds_left is None:
        return None, None

    reg_len = game_ctx.regulation_clock_seconds
    reg_periods = game_ctx.regulation_periods
    ot_len = game_ctx.overtime_clock_seconds

    if period <= reg_periods:
        elapsed = (period - 1) * reg_len + (reg_len - seconds_left)
        return int(elapsed), int(seconds_left)

    elapsed = (
        reg_periods * reg_len
        + (period - reg_periods - 1) * ot_len
        + (ot_len - seconds_left)
    )
    return int(elapsed), int(seconds_left)


def _is_valid_coordinate(play: Play) -> bool:
    if play.coordinate is None:
        return False
    x = play.coordinate.x
    y = play.coordinate.y
    if x < 0 or y < 0:
        return False
    if abs(x) > 500 or abs(y) > 500:
        return False
    return True


def _add_involved(graph, event_iri: URIRef, predicate: URIRef, p_iri: URIRef) -> None:
    graph.add((event_iri, predicate, p_iri))
    graph.add((event_iri, PBPRDF.involvedPlayer, p_iri))


def _participant_ids(play: Play) -> list[str]:
    result: list[str] = []
    for participant in play.participants:
        if participant.athlete and participant.athlete.id:
            result.append(str(participant.athlete.id))
    return result


def _player_iri_for_id(graph, roster_ctx: RosterContext, athlete_id: str) -> URIRef:
    if athlete_id in roster_ctx.player_iri_by_id:
        return roster_ctx.player_iri_by_id[athlete_id]
    p_iri = player_iri(athlete_id)
    roster_ctx.player_iri_by_id[athlete_id] = p_iri
    graph.add((p_iri, RDF.type, PBPRDF.Player))
    graph.add((p_iri, PBPRDF.espnAthleteId, Literal(athlete_id)))
    return p_iri


def _infer_shot_points(play: Play, normalized_type: str) -> int:
    if play.pointsAttempted is not None:
        return int(play.pointsAttempted)
    low = normalized_type.lower()
    low_key = _norm_key(low)
    if "freethrow" in low_key:
        return 1
    if "threepoint" in low_key or "3point" in low_key:
        return 3
    return 2


def _classify(normalized_type: str, play: Play) -> str:
    low = normalized_type.lower()
    compact = _norm_key(normalized_type)
    if "blockshot" in compact or low == "block shot":
        return "block"
    if "technicalfoul" in compact or "delayofgame" in compact or "three-second" in low:
        return "technical_foul"
    if "foul" in low or "charge" in low:
        return "foul"
    if "rebound" in low:
        return "rebound"
    if low.strip() == "steal":
        return "steal"
    if "turnover" in low or "traveling" in low or "shotclock" in compact:
        return "turnover"
    if low.strip() == "substitution":
        return "substitution"
    if low.strip() == "jumpball":
        return "jumpball"
    if "timeout" in low:
        return "timeout"
    if low.strip() == "end game":
        return "end_game"
    if low.strip() == "end period":
        return "end_period"
    if low.strip() == "ejection":
        return "ejection"
    if "challenge" in low or "review" in low:
        return "challenge"
    if play.shootingPlay:
        return "shot"
    if any(key in low for key in SHOT_KEYWORDS) or any(key in compact for key in ["jumpshot", "layupshot", "dunkshot"]):
        return "shot"
    return "unrecognized"


def _bool_flag(graph, event_iri: URIRef, prop: URIRef, value: bool = True) -> None:
    graph.add((event_iri, prop, Literal(bool(value))))


def _participant_iris(graph, roster_ctx: RosterContext, play: Play) -> list[URIRef]:
    return [_player_iri_for_id(graph, roster_ctx, pid) for pid in _participant_ids(play)]


def _team_iri_for_play(roster_ctx: RosterContext, play: Play) -> URIRef | None:
    if play.team and play.team.id and play.team.id in roster_ctx.team_iri_by_id:
        return roster_ctx.team_iri_by_id[play.team.id]
    return None


def map_plays(
    graph, summary: SummaryResponse, game_ctx: GameContext, roster_ctx: RosterContext
) -> tuple[list[EventRecord], dict[str, URIRef]]:
    records: list[EventRecord] = []
    play_id_to_iri: dict[str, URIRef] = {}
    for index, play in enumerate(summary.plays):
        event_id = str(play.id)
        event_iri = play_iri(game_ctx.game_id, event_id)
        play_id_to_iri[event_id] = event_iri

        # Always emit Event-level triples.
        graph.add((event_iri, RDF.type, PBPRDF.Event))
        graph.add((event_iri, PBPRDF.inGame, game_ctx.game_iri))
        graph.add((event_iri, PBPRDF.period, Literal(play.period.number)))
        graph.add((event_iri, PBPRDF.time, Literal(play.clock.displayValue)))
        graph.add((event_iri, RDFS.label, Literal(play.text)))
        graph.add((event_iri, PBPRDF.espnPlayId, Literal(str(play.id))))
        if play.sequenceNumber is not None:
            graph.add((event_iri, PBPRDF.espnSequenceNumber, Literal(str(play.sequenceNumber))))
        graph.add((event_iri, PBPRDF.isScoringPlay, Literal(bool(play.scoringPlay))))
        graph.add((event_iri, PBPRDF.isShootingPlay, Literal(bool(play.shootingPlay))))
        if play.wallclock:
            graph.add((event_iri, PBPRDF.wallclockTime, Literal(play.wallclock, datatype=XSD.dateTime)))
        if play.awayScore is not None:
            graph.add((event_iri, PBPRDF.awayScore, Literal(int(play.awayScore))))
        if play.homeScore is not None:
            graph.add((event_iri, PBPRDF.homeScore, Literal(int(play.homeScore))))
        if play.shortText:
            graph.add((event_iri, PBPRDF.shortDescription, Literal(play.shortText)))
        if play.pointsAttempted is not None:
            graph.add((event_iri, PBPRDF.pointsAttempted, Literal(int(play.pointsAttempted))))
        if play.scoreValue is not None:
            graph.add((event_iri, PBPRDF.scoreValue, Literal(int(play.scoreValue))))

        seconds_into, seconds_left = _compute_seconds(play.period.number, play.clock.displayValue, game_ctx)
        if seconds_into is not None:
            graph.add((event_iri, PBPRDF.secondsIntoGame, Literal(seconds_into)))
        if seconds_left is not None:
            graph.add((event_iri, PBPRDF.secondsLeftInPeriod, Literal(seconds_left)))

        if _is_valid_coordinate(play):
            coord_node = URIRef(f"{event_iri}/coordinate")
            graph.add((coord_node, RDF.type, PBPRDF.CourtCoordinate))
            graph.add((coord_node, PBPRDF.coordinateX, Literal(int(play.coordinate.x))))
            graph.add((coord_node, PBPRDF.coordinateY, Literal(int(play.coordinate.y))))
            graph.add((event_iri, PBPRDF.hasCoordinate, coord_node))

        if play.type:
            ptype_node = URIRef(f"{event_iri}/playType/{play.type.id}")
            graph.add((ptype_node, RDF.type, PBPRDF.PlayType))
            graph.add((ptype_node, PBPRDF.playTypeId, Literal(str(play.type.id))))
            graph.add((ptype_node, PBPRDF.playTypeText, Literal(_norm_spaces(play.type.text))))
            graph.add((event_iri, PBPRDF.hasPlayType, ptype_node))

        normalized_type = _norm_spaces(play.type.text if play.type else "")
        category = _classify(normalized_type, play)
        participants = _participant_iris(graph, roster_ctx, play)
        team_iri = _team_iri_for_play(roster_ctx, play)

        # Unrecognized plays are event-only by spec.
        if category != "unrecognized":
            graph.add((event_iri, RDF.type, PBPRDF.Play))
            if team_iri is not None:
                graph.add((event_iri, PBPRDF.forTeam, team_iri))

        if category == "shot":
            graph.add((event_iri, RDF.type, PBPRDF.Shot))
            graph.add((event_iri, PBPRDF.shotMade, Literal(bool(play.scoringPlay))))
            graph.add((event_iri, PBPRDF.shotType, Literal(normalized_type)))
            graph.add((event_iri, PBPRDF.shotPoints, Literal(_infer_shot_points(play, normalized_type))))
            if play.scoreValue is not None:
                graph.add((event_iri, PBPRDF.scoreValue, Literal(int(play.scoreValue))))
            if participants:
                _add_involved(graph, event_iri, PBPRDF.shotBy, participants[0])
            if play.scoringPlay and len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.shotAssistedBy, participants[1])

        elif category == "block":
            graph.add((event_iri, RDF.type, PBPRDF.Shot))
            graph.add((event_iri, RDF.type, PBPRDF.Block))
            graph.add((event_iri, PBPRDF.shotMade, Literal(False)))
            if len(participants) > 0:
                _add_involved(graph, event_iri, PBPRDF.shotBlockedBy, participants[0])
            if len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.shotBy, participants[1])

        elif category == "foul":
            graph.add((event_iri, RDF.type, PBPRDF.Foul))
            low = normalized_type.lower()
            if participants:
                _add_involved(graph, event_iri, PBPRDF.foulCommittedBy, participants[0])
            if len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.foulDrawnBy, participants[1])
            if "shooting" in low:
                _bool_flag(graph, event_iri, PBPRDF.isShootingFoul)
            if "offensive" in low:
                _bool_flag(graph, event_iri, PBPRDF.isOffensive)
            if "charge" in low:
                _bool_flag(graph, event_iri, PBPRDF.isCharge)
            if "loose ball" in low:
                _bool_flag(graph, event_iri, PBPRDF.isLooseBallFoul)
            if "personal block" in low or "blocking" in low:
                _bool_flag(graph, event_iri, PBPRDF.isPersonalBlockingFoul)

        elif category == "technical_foul":
            graph.add((event_iri, RDF.type, PBPRDF.TechnicalFoul))
            low = normalized_type.lower()
            if participants:
                _add_involved(graph, event_iri, PBPRDF.foulCommittedBy, participants[0])
            if "delay" in low:
                _bool_flag(graph, event_iri, PBPRDF.isDelayOfGame)
            if "three-second" in low or "3-second" in low:
                _bool_flag(graph, event_iri, PBPRDF.isThreeSecond)

        elif category == "rebound":
            graph.add((event_iri, RDF.type, PBPRDF.Rebound))
            low = normalized_type.lower()
            if participants:
                _add_involved(graph, event_iri, PBPRDF.reboundedBy, participants[0])
            if "offensive" in low:
                graph.add((event_iri, PBPRDF.isOffensive, Literal(True)))
            elif "defensive" in low:
                graph.add((event_iri, PBPRDF.isOffensive, Literal(False)))

        elif category == "steal":
            graph.add((event_iri, RDF.type, PBPRDF.Turnover))
            graph.add((event_iri, PBPRDF.turnoverType, Literal("steal")))
            if participants:
                _add_involved(graph, event_iri, PBPRDF.stolenBy, participants[0])
            if len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.turnedOverBy, participants[1])

        elif category == "turnover":
            graph.add((event_iri, RDF.type, PBPRDF.Turnover))
            graph.add((event_iri, PBPRDF.turnoverType, Literal(_norm_spaces(normalized_type).lower())))
            if participants:
                _add_involved(graph, event_iri, PBPRDF.turnedOverBy, participants[0])
            if len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.stolenBy, participants[1])

        elif category == "substitution":
            graph.add((event_iri, RDF.type, PBPRDF.Enters))
            if participants:
                _add_involved(graph, event_iri, PBPRDF.playerEntering, participants[0])
            if len(participants) > 1:
                _add_involved(graph, event_iri, PBPRDF.playerExiting, participants[1])

        elif category == "jumpball":
            graph.add((event_iri, RDF.type, PBPRDF.JumpBall))
            # By table: participant1=home, participant2=away. If team context suggests reverse, flip.
            home_p = participants[0] if participants else None
            away_p = participants[1] if len(participants) > 1 else None
            if home_p is not None:
                _add_involved(graph, event_iri, PBPRDF.jumpBallHomePlayer, home_p)
            if away_p is not None:
                _add_involved(graph, event_iri, PBPRDF.jumpBallAwayPlayer, away_p)

        elif category == "timeout":
            graph.add((event_iri, RDF.type, PBPRDF.Timeout))
            low = normalized_type.lower()
            if "official" in low:
                _bool_flag(graph, event_iri, PBPRDF.isOfficial)
            if "full" in low:
                graph.add((event_iri, PBPRDF.timeoutDuration, Literal("Full")))
            elif "short" in low:
                graph.add((event_iri, PBPRDF.timeoutDuration, Literal("Short")))
            elif "20" in low:
                graph.add((event_iri, PBPRDF.timeoutDuration, Literal("20 Sec.")))

        elif category == "end_game":
            graph.add((event_iri, RDF.type, PBPRDF.EndOfGame))
        elif category == "end_period":
            graph.add((event_iri, RDF.type, PBPRDF.EndOfPeriod))
        elif category == "ejection":
            graph.add((event_iri, RDF.type, PBPRDF.Ejection))
            if participants:
                _add_involved(graph, event_iri, PBPRDF.playerEjected, participants[0])
        elif category == "challenge":
            # Keep as generic Play only.
            pass

        seq = None
        if play.sequenceNumber is not None:
            try:
                seq = int(play.sequenceNumber)
            except ValueError:
                seq = None
        if seq is None:
            seq = 1_000_000_000 + index
        records.append(
            EventRecord(
                event_iri=event_iri,
                sequence_value=seq,
                period=play.period.number,
                seconds_left_in_period=seconds_left,
            )
        )

    return records, play_id_to_iri

