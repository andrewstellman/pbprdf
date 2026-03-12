from __future__ import annotations

import copy

import pytest
from rdflib import Literal
from rdflib.namespace import RDF, RDFS

from pbprdf.mapper.core import map_game_json
from pbprdf.ontology import PBPRDF

VARIANTS = ["nba", "wnba", "ncaam", "ncaaw"]
EXPECTED_REG_PERIODS = {"nba": 4, "wnba": 4, "ncaam": 2, "ncaaw": 4}
EXPECTED_REG_MINUTES = {"nba": 12, "wnba": 10, "ncaam": 20, "ncaaw": 10}


def _game(graph):
    return next(graph.subjects(RDF.type, PBPRDF.Game))


def _variant_obj(request: pytest.FixtureRequest, variant: str, suffix: str):
    return request.getfixturevalue(f"{variant}_{suffix}")


def _event_by_play_id(graph, play_id: str):
    return next(graph.subjects(PBPRDF.espnPlayId, Literal(str(play_id))))


@pytest.fixture(params=VARIANTS)
def variant(request: pytest.FixtureRequest) -> str:
    return request.param


@pytest.fixture
def variant_graph(request: pytest.FixtureRequest, variant: str):
    return _variant_obj(request, variant, "graph")


@pytest.fixture
def variant_raw(request: pytest.FixtureRequest, variant: str):
    return _variant_obj(request, variant, "raw")


class TestSpecRequirements:
    """Functional requirements derived from specs/V1+V2 docs."""

    def test_spec_cross_variant_summary_contains_plays(self, variant, request):
        """Spec: summary payload contains plays as primary mapping input."""
        raw = _variant_obj(request, variant, "raw")
        assert isinstance(raw["plays"], list)
        assert len(raw["plays"]) > 50

    def test_spec_cross_variant_single_game_node(self, variant_graph):
        """Spec: each summary maps to one pbprdf:Game."""
        assert len(list(variant_graph.subjects(RDF.type, PBPRDF.Game))) == 1

    def test_spec_cross_variant_game_id_matches_header(self, variant_graph, variant_raw):
        """Spec: espnEventId on Game must match header.id."""
        game = _game(variant_graph)
        assert str(next(variant_graph.objects(game, PBPRDF.espnEventId))) == str(variant_raw["header"]["id"])

    def test_spec_cross_variant_game_has_home_and_away_links(self, variant_graph):
        """Spec: game must link homeTeam and awayTeam."""
        game = _game(variant_graph)
        assert (game, PBPRDF.homeTeam, None) in variant_graph
        assert (game, PBPRDF.awayTeam, None) in variant_graph

    def test_spec_cross_variant_game_has_format_node(self, variant_graph):
        """Spec: map a GameFormat node from summary.format."""
        game = _game(variant_graph)
        assert (game, PBPRDF.hasGameFormat, None) in variant_graph
        fmt = next(variant_graph.objects(game, PBPRDF.hasGameFormat))
        assert (fmt, RDF.type, PBPRDF.GameFormat) in variant_graph

    def test_spec_cross_variant_regulation_period_count(self, variant, variant_graph):
        """Spec: regulation period counts differ by league variant."""
        fmt = next(variant_graph.subjects(RDF.type, PBPRDF.GameFormat))
        periods = int(next(variant_graph.objects(fmt, PBPRDF.regulationPeriodCount)))
        assert periods == EXPECTED_REG_PERIODS[variant]

    def test_spec_cross_variant_regulation_period_minutes(self, variant, variant_graph):
        """Spec: regulation period minutes come from format by league."""
        fmt = next(variant_graph.subjects(RDF.type, PBPRDF.GameFormat))
        minutes = int(next(variant_graph.objects(fmt, PBPRDF.regulationPeriodLengthMinutes)))
        assert minutes == EXPECTED_REG_MINUTES[variant]

    def test_spec_cross_variant_overtime_period_minutes(self, variant_graph):
        """Spec: overtime period length is captured and mapped."""
        fmt = next(variant_graph.subjects(RDF.type, PBPRDF.GameFormat))
        ot = int(next(variant_graph.objects(fmt, PBPRDF.overtimePeriodLengthMinutes)))
        assert ot == 5

    def test_spec_cross_variant_event_count_matches_input_play_count(self, variant_graph, variant_raw):
        """Spec: one event node emitted per input play row."""
        assert len(set(variant_graph.subjects(RDF.type, PBPRDF.Event))) == len(variant_raw["plays"])

    def test_spec_cross_variant_every_event_links_to_game(self, variant_graph):
        """Spec: all events include inGame link."""
        game = _game(variant_graph)
        events = list(variant_graph.subjects(RDF.type, PBPRDF.Event))
        assert all((event, PBPRDF.inGame, game) in variant_graph for event in events)

    def test_spec_cross_variant_every_event_has_period_time_and_label(self, variant_graph):
        """Spec: universal event triples include period/time/rdfs:label."""
        events = list(variant_graph.subjects(RDF.type, PBPRDF.Event))
        assert all((event, PBPRDF.period, None) in variant_graph for event in events)
        assert all((event, PBPRDF.time, None) in variant_graph for event in events)
        assert all((event, RDFS.label, None) in variant_graph for event in events)

    def test_spec_cross_variant_every_event_has_espn_play_id(self, variant_graph):
        """Spec: espnPlayId should exist on each mapped event."""
        events = list(variant_graph.subjects(RDF.type, PBPRDF.Event))
        assert all((event, PBPRDF.espnPlayId, None) in variant_graph for event in events)

    def test_spec_cross_variant_play_type_nodes_exist(self, variant_graph):
        """Spec: structured PlayType metadata is materialized."""
        play_types = list(variant_graph.subjects(RDF.type, PBPRDF.PlayType))
        assert len(play_types) > 0
        one = play_types[0]
        assert (one, PBPRDF.playTypeId, None) in variant_graph
        assert (one, PBPRDF.playTypeText, None) in variant_graph

    def test_spec_cross_variant_event_numbers_cover_all_events(self, variant_graph, variant_raw):
        """Spec: deterministic event chain numbering across all events."""
        values = sorted(int(v) for v in variant_graph.objects(None, PBPRDF.eventNumber))
        assert values[0] == 1
        assert values[-1] == len(variant_raw["plays"])
        assert len(values) == len(variant_raw["plays"])

    def test_spec_cross_variant_two_rosters_linked_to_game(self, variant_graph):
        """Spec: each game has home and away roster nodes."""
        rosters = list(variant_graph.subjects(RDF.type, PBPRDF.Roster))
        assert len(rosters) == 2
        game = _game(variant_graph)
        assert len(set(variant_graph.objects(game, PBPRDF.hasHomeTeamRoster))) == 1
        assert len(set(variant_graph.objects(game, PBPRDF.hasAwayTeamRoster))) == 1

    def test_spec_cross_variant_team_nodes_have_espn_team_id(self, variant_graph):
        """Spec: team identity is ESPN-ID-first."""
        teams = list(variant_graph.subjects(RDF.type, PBPRDF.Team))
        assert len(teams) >= 2
        assert all((team, PBPRDF.espnTeamId, None) in variant_graph for team in teams)

    def test_spec_cross_variant_player_nodes_have_espn_athlete_id(self, variant_graph):
        """Spec: player identity is ESPN-ID-first."""
        players = list(variant_graph.subjects(RDF.type, PBPRDF.Player))
        assert len(players) > 10
        assert all((player, PBPRDF.espnAthleteId, None) in variant_graph for player in players)

    def test_spec_cross_variant_graph_size_nontrivial(self, variant_graph):
        """Spec: full pipeline should produce substantial RDF graph."""
        assert len(variant_graph) > 1000

    def test_spec_nba_win_probability_snapshots_exist(self, nba_graph):
        """Spec: NBA mapping includes win probability timeline snapshots."""
        snapshots = list(nba_graph.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot))
        assert len(snapshots) > 0
        sample = snapshots[0]
        assert (sample, PBPRDF.homeWinProbability, None) in nba_graph
        assert (sample, PBPRDF.snapshotForPlay, None) in nba_graph

    def test_spec_nba_venue_attendance_and_officials_exist(self, nba_graph):
        """Spec: venue, attendance, and officials from gameInfo are mapped."""
        game = _game(nba_graph)
        venue = next(nba_graph.objects(game, PBPRDF.venue))
        assert (venue, PBPRDF.venueName, None) in nba_graph
        assert (venue, PBPRDF.venueId, None) in nba_graph
        assert (game, PBPRDF.attendance, None) in nba_graph
        assert len(set(nba_graph.objects(game, PBPRDF.hasOfficial))) >= 1

    def test_spec_nba_shots_have_made_and_points_values(self, nba_graph):
        """Spec: shot semantics include made flag and shot points."""
        shots = list(nba_graph.subjects(RDF.type, PBPRDF.Shot))
        assert len(shots) > 0
        sample = shots[0]
        assert (sample, PBPRDF.shotMade, None) in nba_graph
        assert int(next(nba_graph.objects(sample, PBPRDF.shotPoints))) in {1, 2, 3}

    def test_spec_nba_turnovers_emit_turnover_type(self, nba_graph):
        """Spec: turnover plays include turnoverType classification."""
        turnover = next(nba_graph.subjects(RDF.type, PBPRDF.Turnover))
        assert (turnover, PBPRDF.turnoverType, None) in nba_graph

    def test_spec_nba_event_chain_links_bidirectionally(self, nba_graph):
        """Spec: nextEvent/previousEvent links are coherent."""
        chain = list(
            nba_graph.query(
                """
                PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
                SELECT ?e WHERE {
                  ?e pbprdf:nextEvent ?n .
                  ?n pbprdf:previousEvent ?e .
                } LIMIT 5
                """
            )
        )
        assert len(chain) > 0

    def test_spec_nba_coordinates_within_valid_range(self, nba_graph):
        """Spec: mapped court coordinates stay in expected finite range."""
        rows = list(
            nba_graph.query(
                """
                PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
                SELECT ?x ?y WHERE {
                  ?c a pbprdf:CourtCoordinate ;
                     pbprdf:coordinateX ?x ;
                     pbprdf:coordinateY ?y .
                } LIMIT 30
                """
            )
        )
        assert rows
        for x, y in rows:
            assert 0 <= int(x) <= 500
            assert 0 <= int(y) <= 500


class TestFitnessScenarios:
    """1:1 tests for scenarios in tests/QUALITY.md."""

    def test_scenario_1_missing_gameinfo_is_safe(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["gameInfo"] = None
        graph = map_game_json(raw)
        assert len(list(graph.subjects(RDF.type, PBPRDF.Game))) == 1
        assert len(list(graph.subjects(RDF.type, PBPRDF.Event))) == len(raw["plays"])

    def test_scenario_2_invalid_attendance_is_ignored(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw.setdefault("gameInfo", {})["attendance"] = None
        graph = map_game_json(raw)
        game = _game(graph)
        assert (game, PBPRDF.attendance, None) not in graph

    def test_scenario_3_unmatched_winprob_is_skipped(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["winprobability"] = [{"playId": "missing-play-id", "homeWinPercentage": 0.5, "tiePercentage": 0.0}]
        graph = map_game_json(raw)
        assert len(list(graph.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot))) == 0

    def test_scenario_4_unparseable_clock_omits_seconds_fields(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "bad-clock"
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, PBPRDF.secondsIntoGame, None) not in graph
        assert (event, PBPRDF.secondsLeftInPeriod, None) not in graph

    def test_scenario_5_invalid_coordinate_is_dropped(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": -1, "y": 20}
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_scenario_6_non_numeric_sequence_still_orders(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["sequenceNumber"] = "not-a-number"
        graph = map_game_json(raw)
        values = sorted(int(v) for v in graph.objects(None, PBPRDF.eventNumber))
        assert values[0] == 1
        assert values[-1] == len(raw["plays"])

    def test_scenario_7_unrecognized_type_stays_event_only(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["type"] = {"id": "999999", "text": "Unexpected Neutral Event"}
        raw["plays"][0]["shootingPlay"] = False
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, RDF.type, PBPRDF.Event) in graph
        assert (event, RDF.type, PBPRDF.Play) not in graph

    def test_scenario_8_malformed_format_uses_defaults(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["format"] = None
        graph = map_game_json(raw)
        fmt = next(graph.subjects(RDF.type, PBPRDF.GameFormat))
        assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodCount))) == 4
        assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodLengthMinutes))) == 12
        assert int(next(graph.objects(fmt, PBPRDF.overtimePeriodLengthMinutes))) == 5

    def test_scenario_9_missing_participants_still_maps_play(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        shot = next(play for play in raw["plays"] if play.get("shootingPlay"))
        shot["participants"] = []
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, shot["id"])
        assert (event, RDF.type, PBPRDF.Shot) in graph
        assert (event, PBPRDF.shotBy, None) not in graph

    def test_scenario_10_empty_officials_list_is_safe(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw.setdefault("gameInfo", {})["officials"] = []
        graph = map_game_json(raw)
        game = _game(graph)
        assert len(set(graph.objects(game, PBPRDF.hasOfficial))) == 0


class TestBoundariesAndEdgeCases:
    """Boundary and negative tests for defensive code paths."""

    def test_boundary_decimal_clock_format_parses_seconds(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "42.3"
        raw["plays"][0]["period"]["number"] = 1
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert int(next(graph.objects(event, PBPRDF.secondsLeftInPeriod))) == 42

    def test_boundary_huge_coordinate_dropped(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": 9999, "y": 9999}
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_boundary_gameinfo_missing_is_ignored(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["gameInfo"] = None
        graph = map_game_json(raw)
        game = _game(graph)
        assert (game, PBPRDF.venue, None) not in graph
        assert (game, PBPRDF.attendance, None) not in graph

    def test_boundary_venue_missing_is_ignored(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw.setdefault("gameInfo", {})["venue"] = None
        graph = map_game_json(raw)
        game = _game(graph)
        assert (game, PBPRDF.venue, None) not in graph

    def test_boundary_missing_sequence_number_still_gets_event_number(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["sequenceNumber"] = None
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, PBPRDF.eventNumber, None) in graph

    def test_boundary_missing_short_text_does_not_emit_short_description(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["shortText"] = None
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, raw["plays"][0]["id"])
        assert (event, PBPRDF.shortDescription, None) not in graph

    def test_boundary_missing_team_reference_keeps_event_without_for_team(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        candidate = next(play for play in raw["plays"] if play.get("team"))
        candidate["team"] = None
        graph = map_game_json(raw)
        event = _event_by_play_id(graph, candidate["id"])
        assert (event, RDF.type, PBPRDF.Event) in graph
        assert (event, PBPRDF.forTeam, None) not in graph

    def test_boundary_unmatched_winprob_does_not_drop_valid_entries(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        valid_play_id = str(raw["plays"][0]["id"])
        raw["winprobability"] = [
            {"playId": "missing", "homeWinPercentage": 0.4, "tiePercentage": 0.0},
            {"playId": valid_play_id, "homeWinPercentage": 0.6, "tiePercentage": 0.0},
        ]
        graph = map_game_json(raw)
        assert len(list(graph.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot))) == 1
