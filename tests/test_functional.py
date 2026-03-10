from __future__ import annotations

from copy import deepcopy

import pytest
from rdflib import Literal, URIRef
from rdflib.namespace import RDF

from pbprdf.mapper.core import map_game_json
from pbprdf.ontology import PBPRDF, PBPRDF_ENTITY


ALL_VARIANTS = [
    ("nba_graph", "nba_raw"),
    ("wnba_graph", "wnba_raw"),
    ("ncaam_graph", "ncaam_raw"),
    ("ncaaw_graph", "ncaaw_raw"),
]


@pytest.fixture
def graph_variant(request):
    return request.getfixturevalue(request.param)


@pytest.fixture
def raw_variant(request):
    return request.getfixturevalue(request.param)


def _single_subject(graph, rdf_type: URIRef) -> URIRef:
    values = list(graph.subjects(RDF.type, rdf_type))
    assert values, f"Expected at least one {rdf_type}"
    return values[0]


def _event_iri_for_play(raw: dict, play_index: int = 0) -> URIRef:
    game_id = str(raw["header"]["id"])
    play_id = str(raw["plays"][play_index]["id"])
    return URIRef(f"{PBPRDF_ENTITY}games/{game_id}/plays/{play_id}")


def _matched_winprob_count(raw: dict) -> int:
    play_ids = {str(play["id"]) for play in raw.get("plays", [])}
    return sum(1 for row in raw.get("winprobability", []) if str(row.get("playId")) in play_ids)


def _event_numbers(graph) -> list[int]:
    values = []
    for event in graph.subjects(RDF.type, PBPRDF.Event):
        for val in graph.objects(event, PBPRDF.eventNumber):
            values.append(int(val))
    return sorted(values)


class TestSpecRequirements:
    """Functional tests derived from V1/V2 architecture and ontology specs."""

    @pytest.mark.parametrize("graph_variant,raw_variant", ALL_VARIANTS, indirect=True)
    def test_spec_identity_game_has_espn_event_id(self, graph_variant, raw_variant):
        """Spec: V2 IDs must anchor identity using ESPN event IDs."""
        game = _single_subject(graph_variant, PBPRDF.Game)
        espn_event_ids = {str(v) for v in graph_variant.objects(game, PBPRDF.espnEventId)}
        assert espn_event_ids == {str(raw_variant["header"]["id"])}

    @pytest.mark.parametrize("graph_variant,raw_variant", ALL_VARIANTS, indirect=True)
    def test_spec_game_format_matches_payload(self, graph_variant, raw_variant):
        """Spec: Game format must be sourced from payload format section."""
        game = _single_subject(graph_variant, PBPRDF.Game)
        format_node = _single_subject(graph_variant, PBPRDF.GameFormat)
        assert (game, PBPRDF.hasGameFormat, format_node) in graph_variant

        regulation = raw_variant["format"]["regulation"]
        overtime = raw_variant["format"]["overtime"]
        expected_period_count = int(regulation["periods"])
        expected_reg_minutes = int(round(float(regulation["clock"]) / 60.0))
        expected_ot_minutes = int(round(float(overtime["clock"]) / 60.0))
        expected_period_type = "HALF" if "half" in str(regulation["displayName"]).lower() else "QUARTER"

        assert (format_node, PBPRDF.regulationPeriodCount, Literal(expected_period_count)) in graph_variant
        assert (format_node, PBPRDF.regulationPeriodLengthMinutes, Literal(expected_reg_minutes)) in graph_variant
        assert (format_node, PBPRDF.overtimePeriodLengthMinutes, Literal(expected_ot_minutes)) in graph_variant
        assert (format_node, PBPRDF.regulationPeriodType, Literal(expected_period_type)) in graph_variant

    @pytest.mark.parametrize("graph_variant,raw_variant", ALL_VARIANTS, indirect=True)
    def test_spec_one_event_node_per_play(self, graph_variant, raw_variant):
        """Spec: Every payload play must map to one event with espnPlayId."""
        expected_count = len(raw_variant.get("plays", []))
        actual_count = len(set(graph_variant.subjects(PBPRDF.espnPlayId, None)))
        assert actual_count == expected_count

    @pytest.mark.parametrize("graph_variant,raw_variant", ALL_VARIANTS, indirect=True)
    def test_spec_event_chain_numbering_is_contiguous(self, graph_variant, raw_variant):
        """Spec: Event chain emits contiguous event numbers after ordering."""
        numbers = _event_numbers(graph_variant)
        assert numbers == list(range(1, len(raw_variant["plays"]) + 1))

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_spec_rosters_exist_and_link_to_game(self, graph_variant):
        """Spec: Home and away roster nodes must be attached to game."""
        game = _single_subject(graph_variant, PBPRDF.Game)
        home_rosters = list(graph_variant.objects(game, PBPRDF.hasHomeTeamRoster))
        away_rosters = list(graph_variant.objects(game, PBPRDF.hasAwayTeamRoster))
        assert len(home_rosters) == 1
        assert len(away_rosters) == 1
        assert (home_rosters[0], RDF.type, PBPRDF.Roster) in graph_variant
        assert (away_rosters[0], RDF.type, PBPRDF.Roster) in graph_variant

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_spec_players_materialized_with_espn_athlete_id(self, graph_variant):
        """Spec: Player identity must retain ESPN athlete ID."""
        players = list(graph_variant.subjects(RDF.type, PBPRDF.Player))
        assert players
        for player in players[:50]:
            assert list(graph_variant.objects(player, PBPRDF.espnAthleteId))

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_spec_coordinates_are_in_reasonable_range(self, graph_variant):
        """Spec: CourtCoordinate values should represent valid mapped coordinates."""
        for coord in graph_variant.subjects(RDF.type, PBPRDF.CourtCoordinate):
            xs = list(graph_variant.objects(coord, PBPRDF.coordinateX))
            ys = list(graph_variant.objects(coord, PBPRDF.coordinateY))
            assert len(xs) == 1 and len(ys) == 1
            assert 0 <= int(xs[0]) <= 500
            assert 0 <= int(ys[0]) <= 500

    @pytest.mark.parametrize("graph_variant,raw_variant", ALL_VARIANTS, indirect=True)
    def test_spec_winprob_rows_link_only_to_known_plays(self, graph_variant, raw_variant):
        """Spec: Win probability snapshots must link to mapped play entities."""
        expected = _matched_winprob_count(raw_variant)
        actual = len(set(graph_variant.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot)))
        assert actual == expected
        for snap in graph_variant.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot):
            linked_plays = list(graph_variant.objects(snap, PBPRDF.snapshotForPlay))
            assert len(linked_plays) == 1
            assert (linked_plays[0], RDF.type, PBPRDF.Event) in graph_variant

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_spec_event_time_triples_exist_for_parsable_plays(self, graph_variant):
        """Spec: secondsIntoGame and secondsLeftInPeriod are emitted when parsable."""
        with_seconds = list(graph_variant.subjects(PBPRDF.secondsIntoGame, None))
        assert with_seconds
        for event in with_seconds[:100]:
            secs_left = list(graph_variant.objects(event, PBPRDF.secondsLeftInPeriod))
            assert len(secs_left) == 1

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_spec_venue_official_and_attendance_model(self, graph_variant):
        """Spec: Venue/official/attendance model should map structured gameInfo data when present."""
        game = _single_subject(graph_variant, PBPRDF.Game)
        venues = list(graph_variant.objects(game, PBPRDF.venue))
        assert len(venues) == 1
        venue = venues[0]
        assert (venue, RDF.type, PBPRDF.Venue) in graph_variant
        assert list(graph_variant.objects(venue, PBPRDF.venueId))
        assert list(graph_variant.objects(venue, PBPRDF.venueName))
        assert list(graph_variant.objects(game, PBPRDF.gameLocation))
        assert list(graph_variant.objects(game, PBPRDF.attendance))
        assert list(graph_variant.objects(game, PBPRDF.hasOfficial))


class TestFitnessScenarios:
    """1:1 automated checks for scenarios in tests/QUALITY.md."""

    def test_scenario_1_event_identity_is_espn_id_anchored(self, nba_raw):
        raw = deepcopy(nba_raw)
        graph = map_game_json(raw)
        game = _single_subject(graph, PBPRDF.Game)
        assert (game, PBPRDF.espnEventId, Literal(str(raw["header"]["id"]))) in graph

    def test_scenario_2_unknown_play_types_stay_event_only(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Completely Unknown Action"
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, RDF.type, PBPRDF.Event) in graph
        assert (event, RDF.type, PBPRDF.Play) not in graph
        assert (event, PBPRDF.forTeam, None) not in graph

    def test_scenario_3_invalid_coordinates_are_dropped(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": -1, "y": 12}
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_scenario_4_bad_clock_strings_do_not_emit_time_metrics(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "xx:yy"
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.secondsIntoGame, None) not in graph
        assert (event, PBPRDF.secondsLeftInPeriod, None) not in graph

    def test_scenario_5_sequence_fallback_keeps_chain_deterministic(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["sequenceNumber"] = "invalid-seq"
        raw["plays"][1]["sequenceNumber"] = None
        graph = map_game_json(raw)
        assert _event_numbers(graph) == list(range(1, len(raw["plays"]) + 1))

    def test_scenario_6_missing_team_context_does_not_fabricate_for_team(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Three Point Jumper"
        raw["plays"][0]["shootingPlay"] = True
        raw["plays"][0]["team"] = None
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, RDF.type, PBPRDF.Play) in graph
        assert (event, PBPRDF.forTeam, None) not in graph

    def test_scenario_7_missing_participants_do_not_fabricate_actors(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Three Point Jumper"
        raw["plays"][0]["shootingPlay"] = True
        raw["plays"][0]["participants"] = []
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, RDF.type, PBPRDF.Shot) in graph
        assert (event, PBPRDF.shotBy, None) not in graph
        assert (event, PBPRDF.involvedPlayer, None) not in graph

    def test_scenario_8_missing_attendance_produces_no_triple(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["gameInfo"]["attendance"] = None
        graph = map_game_json(raw)
        game = _single_subject(graph, PBPRDF.Game)
        assert (game, PBPRDF.attendance, None) not in graph

    def test_scenario_9_null_game_info_is_safely_ignored(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["gameInfo"] = None
        graph = map_game_json(raw)
        game = _single_subject(graph, PBPRDF.Game)
        assert (game, PBPRDF.venue, None) not in graph
        assert (game, PBPRDF.hasOfficial, None) not in graph
        assert (game, PBPRDF.attendance, None) not in graph

    def test_scenario_10_unmatched_winprob_rows_are_skipped(self, nba_raw):
        raw = deepcopy(nba_raw)
        expected = _matched_winprob_count(raw)
        raw["winprobability"].append(
            {
                "playId": "999999999999",
                "homeWinPercentage": 0.5,
                "tiePercentage": 0.0,
            }
        )
        graph = map_game_json(raw)
        actual = len(set(graph.subjects(RDF.type, PBPRDF.WinProbabilitySnapshot)))
        assert actual == expected


class TestBoundariesAndEdgeCases:
    """Boundary and negative tests for mapper defensive logic."""

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_boundary_event_numbers_start_at_one(self, graph_variant):
        nums = _event_numbers(graph_variant)
        assert nums[0] == 1

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_boundary_last_event_number_equals_event_count(self, graph_variant):
        nums = _event_numbers(graph_variant)
        assert nums[-1] == len(nums)

    @pytest.mark.parametrize("graph_variant", [v[0] for v in ALL_VARIANTS], indirect=True)
    def test_boundary_game_has_home_and_away_team(self, graph_variant):
        game = _single_subject(graph_variant, PBPRDF.Game)
        assert len(list(graph_variant.objects(game, PBPRDF.homeTeam))) == 1
        assert len(list(graph_variant.objects(game, PBPRDF.awayTeam))) == 1

    def test_negative_missing_event_id_fails_fast(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["header"]["id"] = ""
        with pytest.raises(ValueError):
            map_game_json(raw)

    def test_negative_missing_short_text_is_allowed(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["shortText"] = None
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, RDF.type, PBPRDF.Event) in graph

    def test_negative_zero_attendance_still_emits_triple(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["gameInfo"]["attendance"] = 0
        graph = map_game_json(raw)
        game = _single_subject(graph, PBPRDF.Game)
        assert (game, PBPRDF.attendance, Literal(0)) in graph

    def test_negative_empty_officials_list_produces_no_official_triples(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["gameInfo"]["officials"] = []
        graph = map_game_json(raw)
        game = _single_subject(graph, PBPRDF.Game)
        officials = list(graph.objects(game, PBPRDF.hasOfficial))
        assert len(officials) == 0

    def test_negative_out_of_range_coordinate_is_ignored(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": 501, "y": 10}
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_negative_missing_participant_id_does_not_create_player(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["participants"] = [{"athlete": {"id": ""}, "type": "shooter"}]
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.involvedPlayer, None) not in graph

    def test_boundary_unparsable_clock_keeps_event_label_and_time_string(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "100.9.2"
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert list(graph.objects(event, PBPRDF.time))
        assert list(graph.objects(event, PBPRDF.espnPlayId))

    def test_boundary_shot_points_inferred_from_free_throw_text(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Free Throw"
        raw["plays"][0]["shootingPlay"] = True
        raw["plays"][0]["pointsAttempted"] = None
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.shotPoints, Literal(1)) in graph

    def test_boundary_shot_points_inferred_from_three_point_text(self, nba_raw):
        raw = deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Three Point Jumper"
        raw["plays"][0]["shootingPlay"] = True
        raw["plays"][0]["pointsAttempted"] = None
        graph = map_game_json(raw)
        event = _event_iri_for_play(raw, 0)
        assert (event, PBPRDF.shotPoints, Literal(3)) in graph
