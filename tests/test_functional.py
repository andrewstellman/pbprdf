from __future__ import annotations

import copy

from rdflib.namespace import OWL, RDF
from pydantic import ValidationError

from pbprdf.mapper.core import map_game_json
from pbprdf.mapper.ids import play_iri
from pbprdf.models.espn import SummaryResponse
from pbprdf.ontology import PBPRDF, generate_ontology


def _count(graph, query: str) -> int:
    return int(list(graph.query(query))[0][0])


def _game_id(raw: dict) -> str:
    return str(raw["header"]["id"])


def _first_play_iri(raw: dict):
    return play_iri(_game_id(raw), str(raw["plays"][0]["id"]))


class TestSpecRequirements:
    """Functional tests derived from V2/V1 intent specs and README guarantees."""

    def test_spec_fixture_models_parse_all_leagues(self, nba_raw, wnba_raw, ncaam_raw, ncaaw_raw):
        """Spec: V2 architecture requires fixture-driven validation across all leagues."""
        for raw in [nba_raw, wnba_raw, ncaam_raw, ncaaw_raw]:
            model = SummaryResponse.model_validate(raw)
            assert len(model.plays) > 0
            assert model.format is not None

    def test_spec_game_uses_espn_event_id_identity(self, nba_graph, nba_raw):
        """Spec: games use ESPN event ID identity."""
        game = next(nba_graph.subjects(PBPRDF.espnEventId, None))
        assert str(next(nba_graph.objects(game, PBPRDF.espnEventId))) == _game_id(nba_raw)
        assert (game, RDF.type, PBPRDF.Game) in nba_graph

    def test_spec_game_format_matrix_by_league(self, nba_graph, wnba_graph, ncaam_graph, ncaaw_graph):
        """Spec: league period/clock format must map correctly."""
        expected = [
            (nba_graph, 4, 12, "QUARTER"),
            (wnba_graph, 4, 10, "QUARTER"),
            (ncaam_graph, 2, 20, "HALF"),
            (ncaaw_graph, 4, 10, "QUARTER"),
        ]
        for graph, periods, reg_minutes, period_type in expected:
            fmt = next(graph.subjects(RDF.type, PBPRDF.GameFormat))
            assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodCount))) == periods
            assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodLengthMinutes))) == reg_minutes
            assert str(next(graph.objects(fmt, PBPRDF.regulationPeriodType))) == period_type
            assert int(next(graph.objects(fmt, PBPRDF.overtimePeriodLengthMinutes))) == 5

    def test_spec_roster_has_home_and_away_nodes(self, nba_graph):
        """Spec: game has home and away roster with team links."""
        rosters = list(nba_graph.subjects(RDF.type, PBPRDF.Roster))
        assert len(rosters) == 2
        for roster in rosters:
            assert (roster, PBPRDF.rosterTeam, None) in nba_graph
            assert (roster, PBPRDF.hasPlayer, None) in nba_graph

    def test_spec_players_and_teams_use_espn_id_properties(self, nba_graph):
        """Spec: players and teams keep source IDs for deterministic joins."""
        team_count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?t) AS ?c) WHERE {
              ?t a pbprdf:Team ;
                 pbprdf:espnTeamId ?id .
            }
            """,
        )
        player_count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?p) AS ?c) WHERE {
              ?p a pbprdf:Player ;
                 pbprdf:espnAthleteId ?id .
            }
            """,
        )
        assert team_count == 2
        assert player_count > 10

    def test_spec_events_have_core_time_and_period_fields(self, nba_graph):
        """Spec: every mapped event carries game, period, and clock context."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?e) AS ?c) WHERE {
              ?e a pbprdf:Event ;
                 pbprdf:inGame ?g ;
                 pbprdf:period ?period ;
                 pbprdf:time ?clock .
            }
            """,
        )
        assert count > 400

    def test_spec_play_metadata_is_materialized(self, nba_graph):
        """Spec: play type, scoring flags, and IDs are represented as structured data."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?play) AS ?c) WHERE {
              ?play a pbprdf:Play ;
                    pbprdf:espnPlayId ?pid ;
                    pbprdf:isScoringPlay ?scoring ;
                    pbprdf:isShootingPlay ?shooting ;
                    pbprdf:hasPlayType ?ptype .
              ?ptype pbprdf:playTypeId ?tid ;
                     pbprdf:playTypeText ?ttext .
            }
            """,
        )
        assert count > 300

    def test_spec_shot_semantics_include_actor_and_points(self, nba_graph):
        """Spec: shots include shooter, make/miss, and inferred/structured point value."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?shot) AS ?c) WHERE {
              ?shot a pbprdf:Shot ;
                    pbprdf:shotBy ?player ;
                    pbprdf:shotMade ?made ;
                    pbprdf:shotPoints ?pts .
              FILTER (?pts IN (1,2,3))
            }
            """,
        )
        assert count > 100

    def test_spec_event_chain_links_are_bidirectional(self, nba_graph):
        """Spec: previous/next links and event numbering are emitted consistently."""
        linked = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?e) AS ?c) WHERE {
              ?e pbprdf:eventNumber ?n ;
                 pbprdf:nextEvent ?next .
              ?next pbprdf:previousEvent ?e .
            }
            """,
        )
        assert linked > 100

    def test_spec_win_probability_snapshots_link_to_game_and_play(self, nba_graph):
        """Spec: win probability timeline snapshots are linked to game and event/play."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?snap) AS ?c) WHERE {
              ?game a pbprdf:Game ;
                    pbprdf:hasWinProbabilitySnapshot ?snap .
              ?snap a pbprdf:WinProbabilitySnapshot ;
                    pbprdf:homeWinProbability ?prob ;
                    pbprdf:snapshotForPlay ?play .
              ?play a pbprdf:Play .
            }
            """,
        )
        assert count > 100

    def test_spec_venue_attendance_officials_exist(self, nba_graph):
        """Spec: venue, attendance, and officials map from gameInfo."""
        venue_count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?v) AS ?c) WHERE {
              ?g a pbprdf:Game ;
                 pbprdf:venue ?v ;
                 pbprdf:attendance ?att .
              ?v a pbprdf:Venue ;
                 pbprdf:venueName ?name ;
                 pbprdf:venueId ?vid .
            }
            """,
        )
        officials = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?o) AS ?c) WHERE {
              ?g a pbprdf:Game ;
                 pbprdf:hasOfficial ?o .
              ?o a pbprdf:Official ;
                 pbprdf:officialName ?name .
            }
            """,
        )
        assert venue_count == 1
        assert officials >= 1

    def test_spec_ontology_contains_phase1_phase2_vocab(self):
        """Spec: ontology includes V1-compatible and V2-added classes/properties."""
        graph = generate_ontology()
        assert (PBPRDF.Play, RDF.type, OWL.Class) in graph
        assert (PBPRDF.WinProbabilitySnapshot, RDF.type, OWL.Class) in graph
        assert (PBPRDF.Venue, RDF.type, OWL.Class) in graph
        assert (PBPRDF.homeWinProbability, RDF.type, OWL.DatatypeProperty) in graph
        assert (PBPRDF.snapshotForPlay, RDF.type, OWL.ObjectProperty) in graph

    def test_spec_game_format_has_league_code_for_all_variants(self, nba_graph, wnba_graph, ncaam_graph, ncaaw_graph):
        """Spec: GameFormat nodes include leagueCode for all supported leagues."""
        expected_codes = {"NBA", "WNBA", "NCAAM", "NCAAW"}
        seen: set[str] = set()
        for graph in [nba_graph, wnba_graph, ncaam_graph, ncaaw_graph]:
            fmt = next(graph.subjects(RDF.type, PBPRDF.GameFormat))
            seen.add(str(next(graph.objects(fmt, PBPRDF.leagueCode))))
        assert seen == expected_codes

    def test_spec_v1_game_location_compatibility_retained(self, nba_graph):
        """Spec: V1-compatible gameLocation flat string remains available."""
        game = next(nba_graph.subjects(RDF.type, PBPRDF.Game))
        location = str(next(nba_graph.objects(game, PBPRDF.gameLocation)))
        assert location
        assert "," in location

    def test_spec_involved_player_superproperty_materialized(self, nba_graph):
        """Spec: role properties are also materialized as involvedPlayer links."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?play) AS ?c) WHERE {
              ?play pbprdf:shotBy ?p ;
                    pbprdf:involvedPlayer ?p .
            }
            """,
        )
        assert count > 50

    def test_spec_structured_play_type_nodes_have_required_fields(self, nba_graph):
        """Spec: hasPlayType links include both playTypeId and playTypeText."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?play) AS ?c) WHERE {
              ?play pbprdf:hasPlayType ?t .
              ?t pbprdf:playTypeId ?id ;
                 pbprdf:playTypeText ?text .
            }
            """,
        )
        assert count > 300

    def test_spec_win_probability_includes_tie_probability(self, nba_graph):
        """Spec: win probability snapshots include tieProbability field."""
        count = _count(
            nba_graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?snap) AS ?c) WHERE {
              ?snap a pbprdf:WinProbabilitySnapshot ;
                    pbprdf:tieProbability ?tp .
            }
            """,
        )
        assert count > 100


class TestFitnessScenarios:
    """1:1 automated tests for QUALITY.md scenarios."""

    def test_scenario_1_event_id_required(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["header"]["id"] = ""
        try:
            map_game_json(raw)
            assert False, "Expected ValueError for missing event id"
        except ValueError:
            pass

    def test_scenario_2_invalid_game_time_ignored(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["header"]["competitions"][0]["date"] = "not-a-date"
        graph = map_game_json(raw)
        game = next(graph.subjects(PBPRDF.espnEventId, None))
        assert (game, PBPRDF.gameTime, None) not in graph

    def test_scenario_3_negative_coordinates_rejected(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": -1, "y": 20}
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_scenario_4_out_of_range_coordinates_rejected(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["coordinate"] = {"x": 9999, "y": 3}
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, PBPRDF.hasCoordinate, None) not in graph

    def test_scenario_5_non_numeric_sequence_fallback_ordering(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["sequenceNumber"] = "not-an-int"
        raw["plays"][1]["sequenceNumber"] = "still-not-an-int"
        graph = map_game_json(raw)
        event_count = len(list(graph.subjects(RDF.type, PBPRDF.Event)))
        number_count = _count(
            graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?e) AS ?c) WHERE { ?e pbprdf:eventNumber ?n . }
            """,
        )
        assert number_count == event_count

    def test_scenario_6_unmatched_winprob_play_id_skipped(self, nba_raw):
        baseline = map_game_json(copy.deepcopy(nba_raw))
        baseline_count = _count(
            baseline,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?s) AS ?c) WHERE { ?s a pbprdf:WinProbabilitySnapshot . }
            """,
        )
        raw = copy.deepcopy(nba_raw)
        raw["winprobability"].append(
            {
                "playId": "THIS_PLAY_ID_DOES_NOT_EXIST",
                "homeWinPercentage": 0.5,
                "tiePercentage": 0.0,
                "secondsLeft": 3000,
            }
        )
        graph = map_game_json(raw)
        mutated_count = _count(
            graph,
            """
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?s) AS ?c) WHERE { ?s a pbprdf:WinProbabilitySnapshot . }
            """,
        )
        assert mutated_count == baseline_count

    def test_scenario_7_invalid_attendance_ignored(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["gameInfo"]["attendance"] = "unknown"
        try:
            map_game_json(raw)
            assert False, "Expected schema validation failure for invalid attendance type"
        except ValidationError:
            pass

    def test_scenario_8_missing_gameinfo_graceful(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["gameInfo"] = None
        graph = map_game_json(raw)
        # Core mapping still exists.
        assert any(graph.triples((None, RDF.type, PBPRDF.Game)))
        # Venue/official entities are absent.
        assert not any(graph.triples((None, RDF.type, PBPRDF.Venue)))
        assert not any(graph.triples((None, RDF.type, PBPRDF.Official)))

    def test_scenario_9_unrecognized_play_event_only(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["type"]["text"] = "Completely Unknown Action"
        raw["plays"][0]["shootingPlay"] = False
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, RDF.type, PBPRDF.Event) in graph
        assert (event, RDF.type, PBPRDF.Play) not in graph

    def test_scenario_10_decimal_clock_supported(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "5.9"
        raw["plays"][0]["period"]["number"] = 1
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        # 12-minute regulation period -> 720 - 5 = 715 elapsed.
        assert (event, PBPRDF.secondsLeftInPeriod, None) in graph
        assert int(next(graph.objects(event, PBPRDF.secondsLeftInPeriod))) == 5
        assert int(next(graph.objects(event, PBPRDF.secondsIntoGame))) == 715


class TestBoundariesAndEdgeCases:
    """Boundary and negative tests for defensive mapper paths."""

    def test_negative_missing_team_reference_does_not_emit_for_team(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["team"] = None
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, RDF.type, PBPRDF.Play) in graph
        assert (event, PBPRDF.forTeam, None) not in graph

    def test_negative_malformed_clock_omits_seconds_fields(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["clock"]["displayValue"] = "not-a-clock"
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, PBPRDF.secondsIntoGame, None) not in graph
        assert (event, PBPRDF.secondsLeftInPeriod, None) not in graph

    def test_boundary_ncaam_second_half_starts_after_20_minutes(self, ncaam_graph):
        rows = list(
            ncaam_graph.query(
                """
                PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
                SELECT ?s WHERE {
                  ?e a pbprdf:Event ;
                     pbprdf:period 2 ;
                     pbprdf:secondsIntoGame ?s .
                } ORDER BY ASC(?s) LIMIT 1
                """
            )
        )
        assert rows
        assert int(rows[0][0]) >= 1200

    def test_boundary_win_probability_values_stay_in_range(self, nba_graph):
        rows = list(
            nba_graph.query(
                """
                PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
                SELECT ?prob WHERE {
                  ?snap a pbprdf:WinProbabilitySnapshot ;
                        pbprdf:homeWinProbability ?prob .
                }
                """
            )
        )
        assert rows
        for row in rows:
            prob = float(row[0])
            assert 0.0 <= prob <= 1.0

    def test_negative_unknown_team_id_does_not_emit_for_team(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["team"] = {"id": "NON_EXISTENT_TEAM"}
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert (event, PBPRDF.forTeam, None) not in graph

    def test_negative_unknown_actor_id_is_still_typed_player(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        unknown_id = "999999999"
        raw["plays"][0]["participants"] = [{"athlete": {"id": unknown_id}, "type": "shooter"}]
        graph = map_game_json(raw)
        count = _count(
            graph,
            f"""
            PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
            SELECT (COUNT(?p) AS ?c) WHERE {{
              ?p a pbprdf:Player ;
                 pbprdf:espnAthleteId "{unknown_id}" .
            }}
            """,
        )
        assert count == 1

    def test_boundary_missing_format_uses_default_period_lengths(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["format"] = None
        graph = map_game_json(raw)
        fmt = next(graph.subjects(RDF.type, PBPRDF.GameFormat))
        assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodCount))) == 4
        assert int(next(graph.objects(fmt, PBPRDF.regulationPeriodLengthMinutes))) == 12
        assert int(next(graph.objects(fmt, PBPRDF.overtimePeriodLengthMinutes))) == 5

    def test_boundary_invalid_format_values_fallback_to_defaults(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["format"]["regulation"]["periods"] = "oops"
        raw["format"]["regulation"]["clock"] = "nope"
        raw["format"]["overtime"]["clock"] = "bad"
        try:
            map_game_json(raw)
            assert False, "Expected schema validation failure for invalid format values"
        except ValidationError:
            pass

    def test_boundary_overtime_seconds_uses_ot_clock(self, nba_raw):
        raw = copy.deepcopy(nba_raw)
        raw["plays"][0]["period"]["number"] = 5
        raw["plays"][0]["clock"]["displayValue"] = "4:00"
        graph = map_game_json(raw)
        event = _first_play_iri(raw)
        assert int(next(graph.objects(event, PBPRDF.secondsLeftInPeriod))) == 240
        assert int(next(graph.objects(event, PBPRDF.secondsIntoGame))) == 2940
