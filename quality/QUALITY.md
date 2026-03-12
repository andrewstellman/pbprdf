# pbprdf Quality Constitution

## 1) Purpose

`pbprdf` quality means producing semantically correct RDF from ESPN summary payloads across NBA, WNBA, NCAAM, and NCAAW, even when upstream data is missing, malformed, or variant-specific. The primary failure mode is not a crash; it is silently wrong triples that look plausible and break downstream analysis.

Following Deming, quality is built into the codebase through durable context (`AGENTS.md`, this constitution) and executable tests (`test_functional.py`), not inspected in at release time. Following Juran, fitness for use in this project means stable ESPN-ID identity, correct event semantics, coherent temporal ordering, and resilient handling of optional or drifting ESPN fields. Following Crosby, this infrastructure is cheaper than post-hoc debugging of wrong analytical conclusions from bad graphs.

## 2) Coverage Targets

| Subsystem | Target | Why |
|-----------|--------|-----|
| `src/pbprdf/mapper/plays.py` | 92-95% | Most complex mapping logic (classification, clock parsing, coordinates, participant roles); small mistakes create high-volume semantic drift. |
| `src/pbprdf/mapper/game.py`, `src/pbprdf/mapper/events.py` | 88-92% | Time/ordering and game-format defaults are cross-cutting; wrong period math corrupts every temporal query. |
| `src/pbprdf/mapper/roster.py`, `src/pbprdf/mapper/venue.py`, `src/pbprdf/mapper/winprob.py` | 85-90% | External optional sections (`boxscore`, `gameInfo`, `winprobability`) are fragile and frequently sparse by league/game. |
| `src/pbprdf/models/espn.py` | 80-85% | Schema drift defense layer; model optionality choices determine whether mapper sees valid boundary mutations. |
| `src/pbprdf/cli.py`, `src/pbprdf/query/endpoint.py` | 75-80% | Operational surface area (I/O, endpoint config); important but less semantically dense than mapper core. |

## 3) Coverage Theater Prevention

Tests are considered weak for this project if they:

- Assert only that a graph or query has "some result" without checking values or invariants.
- Check file existence/output existence only, without validating RDF semantics.
- Assert a mock returns configured values.
- Only verify schema validation errors instead of mapper output behavior.
- Use `LIMIT 1` style existence checks where the requirement is population-wide.

Project-specific anti-patterns:

- Counting `pbprdf:Play` or `pbprdf:Shot` nodes without verifying required attached semantics (IDs, team links, flags, points, or participants).
- Verifying a game has a venue link but not checking `venueId`/`venueName` consistency and V1 compatibility `gameLocation`.
- Checking event chain links exist without validating event numbering continuity and delta constraints.

## 4) Fitness-to-Purpose Scenarios

### Scenario 1: Missing `gameInfo` Must Not Break Mapping

**What happened:** `map_venue()` in `src/pbprdf/mapper/venue.py` guards with `if not isinstance(game_info, dict): return`.

**The requirement:** Missing or malformed `gameInfo` must not crash mapping; game and event triples still materialize.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_1_missing_gameinfo_is_safe`

### Scenario 2: Missing Attendance Should Be Ignored, Not Fatal

**What happened:** Attendance conversion in `src/pbprdf/mapper/venue.py` is wrapped in `try/except (TypeError, ValueError)`.

**The requirement:** Absent attendance must be skipped cleanly and must not crash mapping.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_2_invalid_attendance_is_ignored`

### Scenario 3: Unmatched Win-Probability Play IDs Are Skipped

**What happened:** `map_win_probability()` in `src/pbprdf/mapper/winprob.py` skips rows when `playId` is not in mapped plays.

**The requirement:** Pregame/unknown winprob entries must not create orphan snapshot links.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_3_unmatched_winprob_is_skipped`

### Scenario 4: Unknown Clock Format Must Not Corrupt Time Fields

**What happened:** `_parse_clock_seconds_left()` in `src/pbprdf/mapper/plays.py` returns `None` when clock text is unparsable.

**The requirement:** Event stays present, but `secondsIntoGame`/`secondsLeftInPeriod` are omitted for unparsable clock values.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_4_unparseable_clock_omits_seconds_fields`

### Scenario 5: Sentinel/Bad Coordinates Must Be Dropped

**What happened:** `_is_valid_coordinate()` in `src/pbprdf/mapper/plays.py` rejects missing, negative, or extreme coordinates.

**The requirement:** Coordinates outside accepted bounds never emit `CourtCoordinate` triples.

**How to verify:** `pytest tests/test_functional.py -k "scenario_5 or boundary_coordinate"`

### Scenario 6: Non-Numeric `sequenceNumber` Needs Deterministic Fallback

**What happened:** `map_plays()` falls back to `1_000_000_000 + index` when `sequenceNumber` is missing/non-numeric.

**The requirement:** Event chain and event numbers remain complete and contiguous despite bad sequence numbers.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_6_non_numeric_sequence_still_orders`

### Scenario 7: Unknown Play Types Must Stay Generic Events

**What happened:** `_classify()` may return `"unrecognized"` and `map_plays()` keeps event-only typing.

**The requirement:** Unknown play types should not be falsely typed as `Play` subclasses.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_7_unrecognized_type_stays_event_only`

### Scenario 8: Game Format Parsing Needs Defensive Defaults

**What happened:** `map_game()` uses `_safe_int/_safe_float` defaults for `format.regulation` and `format.overtime`.

**The requirement:** Malformed format values fall back to regulation 4x12 and OT 5 minutes equivalent defaults, preserving temporal mapping.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_8_malformed_format_uses_defaults`

### Scenario 9: Missing Participant Lists Must Not Break Play Mapping

**What happened:** Participant extraction in `map_plays()` handles empty lists and optional fields.

**The requirement:** Typed play events still map without actor links when participants are absent.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_9_missing_participants_still_maps_play`

### Scenario 10: Empty Officials List Must Be Safe

**What happened:** `map_venue()` only emits official nodes when official objects are present.

**The requirement:** Games with no officials should map without crashes and without phantom official triples.

**How to verify:** `pytest tests/test_functional.py -k test_scenario_10_empty_officials_list_is_safe`

## 5) AI Session Quality Discipline

1. Read `tests/QUALITY.md` before any mapper/model/query change.
2. Run `pytest tests/ -v` before marking work complete.
3. Add or update functional tests for every behavior change, including negative/boundary cases.
4. Update this file when a new failure mode or defensive pattern is discovered.
5. End each coding session with a Quality Compliance Checklist (tests run, scenarios touched, docs updated).
6. Never delete fitness-to-purpose scenarios; only append or refine with evidence.

## 6) Human Gate

Human review is required for:

- Whether mapped semantics match basketball domain intent when ESPN text/typing conflicts.
- Backward compatibility decisions for V1 query behavior and ontology naming.
- Triplestore operational choices (endpoint topology, auth, performance tuning).
- Acceptability of ontology/model evolution when ESPN schema drifts.
- Release readiness when integration tests are skipped due to unavailable external endpoints.
