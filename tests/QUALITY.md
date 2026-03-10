# pbprdf Quality Constitution

## 1) Purpose

`pbprdf` quality means generating RDF that is faithful to the ESPN source payload and the ontology intent docs, across NBA, WNBA, NCAAM, and NCAAW, without silently corrupting semantics. This project is a data-to-knowledge pipeline: subtle mapping drift is more dangerous than hard crashes because wrong triples can still "look valid" in SPARQL results.

Following Deming, quality is built into the repo context and test system, not inspected in at the end. This file, `tests/test_functional.py`, and the run protocols define a persistent bar that every AI session inherits before writing code.

Following Juran and Crosby, fitness for use here means: map real ESPN fixtures into stable, queryable RDF with correct IDs, event sequencing, play semantics, venue/official/win-probability modeling, and defensive handling for malformed or missing fields. Investing in this infrastructure is cheaper than debugging incorrect analytics after bad triples are loaded into a triplestore.

## 2) Coverage Targets

| Subsystem | Target | Why |
|-----------|--------|-----|
| `src/pbprdf/mapper/plays.py` | 92-95% | Most branching and classification logic (`_classify`, `_compute_seconds`, coordinate filtering, play-type semantics) where silent semantic drift is likely. |
| `src/pbprdf/mapper/game.py` + `src/pbprdf/mapper/roster.py` | 88-92% | Core identity and context mapping (game/team/player IDs, format, roster attachment) drives all downstream joins. |
| `src/pbprdf/mapper/events.py` + `src/pbprdf/mapper/winprob.py` + `src/pbprdf/mapper/venue.py` | 85-90% | Event chain and supplemental timelines/entities are cross-cutting integration points and common regression surfaces. |
| `src/pbprdf/models/espn.py` + `src/pbprdf/ontology.py` | 80-85% | Contract/schema and ontology integrity catch upstream API drift and vocabulary regressions. |
| `src/pbprdf/cli.py` + `src/pbprdf/fetcher.py` + `src/pbprdf/query/endpoint.py` | 75-80% | I/O-heavy boundary layer; correctness is important but much behavior is integration/environment dependent. |

## 3) Coverage Theater Prevention

The following do **not** count as meaningful tests in this project:

- Asserting graph size is "non-zero" without checking specific required triples.
- Verifying one arbitrary result with `LIMIT 1` for properties that should hold broadly (unless paired with count/value checks).
- Checking only that a class exists without verifying required properties and links.
- Positive-only mapping checks that never mutate malformed input.
- Asserting triplestore queries "returned rows" without validating row values.

Project-specific anti-patterns:

- Counting `pbprdf:Shot` nodes without checking `shotMade`, `shotPoints`, or actor links.
- Checking a venue node exists but not `venueId`/`venueName` consistency.
- Confirming win probability snapshots exist but not that unmatched `playId` entries are dropped.

## 4) Fitness-to-Purpose Scenarios

### Scenario 1: Event ID Is Mandatory

**What happened:** `map_game()` in `src/pbprdf/mapper/game.py` raises on missing `header.id`; without it, game and play IRIs become unstable.

**The requirement:** Mapping must fail fast if no event ID exists.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_1_event_id_required"`

### Scenario 2: Invalid Game Time Must Not Pollute RDF

**What happened:** `map_game()` validates ISO dateTime before emitting `pbprdf:gameTime`.

**The requirement:** Invalid date strings must be ignored, not emitted as bad literals.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_2_invalid_game_time_ignored"`

### Scenario 3: Negative Coordinates Are Sentinel Noise

**What happened:** `_is_valid_coordinate()` in `src/pbprdf/mapper/plays.py` rejects `< 0` values.

**The requirement:** Negative coordinate values must not produce `CourtCoordinate` triples.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_3_negative_coordinates_rejected"`

### Scenario 4: Out-of-Range Coordinates Must Be Filtered

**What happened:** `_is_valid_coordinate()` rejects absurd magnitudes (`abs(x|y) > 500`) to prevent polluted shot charts.

**The requirement:** Outlier coordinates must not map to coordinate nodes.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_4_out_of_range_coordinates_rejected"`

### Scenario 5: Bad Sequence Numbers Must Not Break Event Chain

**What happened:** `map_plays()` falls back to deterministic ordering when `sequenceNumber` is non-numeric.

**The requirement:** Event chain numbering and prev/next links remain complete with malformed sequence values.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_5_non_numeric_sequence_fallback_ordering"`

### Scenario 6: Unmatched Win-Probability Entries Must Be Skipped

**What happened:** `map_win_probability()` skips entries whose `playId` has no mapped play.

**The requirement:** Pre-game/unmatched entries must not create orphan snapshots.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_6_unmatched_winprob_play_id_skipped"`

### Scenario 7: Non-Numeric Attendance Must Be Ignored

**What happened:** `SummaryResponse` enforces `gameInfo.attendance` as an integer contract before mapping.

**The requirement:** Invalid attendance types must fail schema validation early (instead of producing inconsistent RDF).

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_7_invalid_attendance_ignored"`

### Scenario 8: Missing gameInfo Must Not Crash Mapping

**What happened:** `map_venue()` returns early for non-dict `gameInfo`.

**The requirement:** Core mapping must succeed even when venue/official metadata is absent.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_8_missing_gameinfo_graceful"`

### Scenario 9: Unknown Play Types Stay Event-Only

**What happened:** `_classify()` can return `unrecognized`; mapper keeps Event triples but omits Play typing.

**The requirement:** Unknown play types must not be force-classified into wrong basketball classes.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_9_unrecognized_play_event_only"`

### Scenario 10: Decimal Clock Format Must Parse Correctly

**What happened:** `_parse_clock_seconds_left()` accepts `SS.fraction` and truncates seconds for timing properties.

**The requirement:** Decimal clock strings must emit valid `secondsLeftInPeriod` and `secondsIntoGame`.

**How to verify:** `pytest tests/test_functional.py -k "test_scenario_10_decimal_clock_supported"`

## 5) AI Session Quality Discipline

1. Read `tests/QUALITY.md` before making mapper/model/ontology changes.
2. Run `pytest tests/ -v` before marking work complete.
3. Add/extend requirement-derived tests for every new behavior and edge case.
4. Add new fitness scenarios when new defensive patterns appear.
5. Output a Quality Compliance Checklist in task summaries (tests run, scenarios touched, docs updated).
6. Never delete scenarios; only append and refine wording.

## 6) The Human Gate

The following still require human judgment:

- Domain-meaning correctness of basketball semantics that are inferential, not explicit in ESPN fields.
- Backward-compatibility decisions for legacy V1 query behavior.
- Triplestore deployment/security/network configuration.
- Whether spec intent should change when ESPN payload behavior shifts.
- Release decisions when static audits disagree on spec vs implementation.
