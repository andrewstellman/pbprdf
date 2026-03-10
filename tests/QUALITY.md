# pbprdf Quality Constitution

## 1) Purpose

Quality for `pbprdf` means generating RDF that is semantically correct, stable under ESPN schema drift, and safe for downstream SPARQL analytics. This project is high-risk for "silently wrong" behavior: a graph can serialize cleanly while carrying bad links, wrong IDs, or incorrect event semantics.

Deming applies here as persistent context plus executable tests: `AGENTS.md`, this constitution, and `test_functional.py` make the quality bar durable across AI sessions. Juran applies as fitness-for-use: the output must remain queryable and faithful to game events even when optional ESPN fields are missing or malformed. Crosby applies because up-front checks on mapper edge cases are far cheaper than post-hoc debugging of corrupted analytical graphs.

## 2) Coverage Targets

| Subsystem | Target | Why |
|-----------|--------|-----|
| `mapper/plays.py` | 92-95% | Most branching and classification logic; optional fields (`participants`, `coordinate`, `scoreValue`) can silently skew event semantics. |
| `mapper/game.py` + `mapper/events.py` | 88-92% | Identity, game format, and event ordering/timing underpin every downstream query. |
| `mapper/roster.py` + `mapper/winprob.py` + `mapper/venue.py` | 85-90% | External-data variability is high; malformed IDs, officials, attendance, or unmatched winprob rows must be handled defensively. |
| `models/espn.py` + CLI/query I/O | 78-85% | Contract and wiring layer; important, but less semantic branching than mapper core. |

## 3) Coverage Theater Prevention

The following are not acceptable tests for this project:

- Asserting graph length only (e.g., "triples > 1000") without checking semantic values.
- Checking one random `LIMIT 1` hit for entity existence without validating expected properties.
- Verifying only that mapping does not crash for malformed inputs, with no output assertions.
- Asserting only presence of triples like `(s, p, None)` for critical fields instead of validating exact literals or links.
- Asserting mocked triplestore responses without validating query/mapper contract behavior.

Project-specific anti-theater examples:

- Counting `pbprdf:Play` nodes without checking that `espnPlayId` and `inGame` are correct.
- Checking that `WinProbabilitySnapshot` exists without verifying `snapshotForPlay` links to known mapped play IRIs.
- Checking venue presence without validating `gameLocation` compatibility behavior.

## 4) Fitness-to-Purpose Scenarios

### Scenario 1: Event Identity Is ESPN-ID Anchored

**What happened:** Mapping logic in `mapper/game.py` and `mapper/plays.py` uses ESPN IDs (`espnEventId`, `espnPlayId`) as canonical identity.

**The requirement:** Game and play entities must retain ESPN IDs in RDF for deterministic joins and cross-run stability.

**How to verify:** `test_scenario_1_event_identity_is_espn_id_anchored`.

### Scenario 2: Unknown Play Types Must Stay Event-Only

**What happened:** `_classify()` in `mapper/plays.py` intentionally returns `"unrecognized"` for unknown types; mapper avoids adding `Play` semantics.

**The requirement:** Unrecognized plays must remain `Event` only (no `pbprdf:Play`, no `forTeam`).

**How to verify:** `test_scenario_2_unknown_play_types_stay_event_only`.

### Scenario 3: Invalid Coordinates Must Be Dropped

**What happened:** `_is_valid_coordinate()` in `mapper/plays.py` rejects negative and out-of-range coordinates.

**The requirement:** Invalid coordinate payloads must not produce `hasCoordinate` triples.

**How to verify:** `test_scenario_3_invalid_coordinates_are_dropped`.

### Scenario 4: Bad Clock Strings Must Not Create Wrong Time Metrics

**What happened:** `_parse_clock_seconds_left()` in `mapper/plays.py` returns `None` for malformed clocks.

**The requirement:** If clock parsing fails, no `secondsIntoGame`/`secondsLeftInPeriod` should be emitted for that event.

**How to verify:** `test_scenario_4_bad_clock_strings_do_not_emit_time_metrics`.

### Scenario 5: Sequence Fallback Must Preserve Deterministic Chain

**What happened:** `map_plays()` falls back to `1_000_000_000 + index` when `sequenceNumber` is missing/invalid.

**The requirement:** Event chain numbering remains contiguous and deterministic even with invalid sequence values.

**How to verify:** `test_scenario_5_sequence_fallback_keeps_chain_deterministic`.

### Scenario 6: Missing Team Context Must Not Fabricate Team Links

**What happened:** `_team_iri_for_play()` in `mapper/plays.py` returns `None` when team reference is absent/unmapped.

**The requirement:** Recognized plays can still map, but `forTeam` must be omitted when team context is missing.

**How to verify:** `test_scenario_6_missing_team_context_does_not_fabricate_for_team`.

### Scenario 7: Missing Participants Must Not Fabricate Actors

**What happened:** Participant extraction in `mapper/plays.py` only adds actor triples when valid athlete IDs exist.

**The requirement:** Play type semantics may remain, but actor properties (`shotBy`, etc.) must be absent if participants are missing.

**How to verify:** `test_scenario_7_missing_participants_do_not_fabricate_actors`.

### Scenario 8: Invalid Attendance Must Be Ignored

**What happened:** `map_venue()` in `mapper/venue.py` wraps attendance parsing in `try/except`.

**The requirement:** Non-numeric attendance values must not emit a bad `attendance` triple.

**How to verify:** `test_scenario_8_missing_attendance_produces_no_triple`.

### Scenario 9: Non-Dict GameInfo Must Not Break Mapping

**What happened:** `map_venue()` early-returns for non-dict `gameInfo`.

**The requirement:** Mapping must still produce a valid game graph with no venue/official triples from malformed gameInfo.

**How to verify:** `test_scenario_9_null_game_info_is_safely_ignored`.

### Scenario 10: Unmatched Win Probability Rows Must Be Skipped

**What happened:** `map_win_probability()` skips rows where `playId` is not in the mapped play lookup.

**The requirement:** No orphan snapshots should exist; every snapshot must link to a mapped event.

**How to verify:** `test_scenario_10_unmatched_winprob_rows_are_skipped`.

## 5) AI Session Quality Discipline

1. Read `tests/QUALITY.md` before substantive code changes.
2. Run `pytest tests/ -v` before marking work complete.
3. Add or update functional tests for every new mapping behavior and edge case.
4. Add new scenarios here when new defensive logic appears.
5. End sessions with a quality compliance checklist (tests run, scenarios touched, files updated).
6. Never delete scenarios; only supersede by adding clearer ones.

## 6) The Human Gate

The following still require human review:

- Semantic plausibility of basketball interpretation from play text when structured fields are ambiguous.
- Whether intentional ontology deltas are acceptable for consumers expecting exact V1 quirks.
- Triplestore operational concerns (performance, indexing, graph partitioning strategy).
- Release-level documentation completeness and examples.
- Security and operational decisions for deployed SPARQL endpoints.
