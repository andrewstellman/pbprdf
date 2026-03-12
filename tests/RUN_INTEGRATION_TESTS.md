# pbprdf Integration Test Protocol

## Safety Constraints

If running with elevated permissions:

- DO NOT modify source code.
- DO NOT delete files.
- ONLY create files in `tests/results/`.
- If something fails, record it and continue. DO NOT fix code during this run.

## Pre-Flight Check

1. Python env is active and project is installed (`pip install -e .`).
2. Test dependencies installed (`pip install pytest`).
3. Fixtures exist in `tests/fixtures/` for all four leagues.
4. If running live triplestore integration tests:
   - `PBPRDF_SPARQL_ENDPOINT` is set, reachable, and accepts SPARQL.
   - Optional: `PBPRDF_SPARQL_UPDATE_ENDPOINT`, `PBPRDF_GRAPH_STORE_ENDPOINT`.
5. `tests/results/` exists and is writable.

## Test Matrix

| Check | Method | Pass Criteria |
|-------|--------|---------------|
| Functional/regression baseline | `pytest tests/ -v` | All tests pass (or only intentional skips for triplestore absence). |
| Functional suite only | `pytest tests/test_functional.py -v` | All functional/spec-derived tests pass. |
| Ontology generation + map smoke | `python -m pbprdf.cli ontology --output tests/results/ontology_smoke.ttl && python -m pbprdf.cli map tests/fixtures/nba_401810770.json --output tests/results/nba_smoke.ttl` | Both files created and non-empty Turtle. |
| Cross-variant map pipeline | `python -m pbprdf.cli map tests/fixtures --output tests/results/all_variants.ttl` | Output includes all four `pbprdf:Game` instances. |
| Cross-variant structure validation | `pytest tests/test_functional.py -k cross_variant -v` | Cross-variant invariants pass for NBA/WNBA/NCAAM/NCAAW. |
| Venue + officials integration | `pytest tests/test_mapper_venue.py -v` | Venue/attendance/official mappings pass for fixture with data. |
| Win probability integration | `pytest tests/test_mapper_winprob.py -v` | Snapshot presence/link/value tests pass. |
| Live triplestore load/query (optional) | `PBPRDF_SPARQL_ENDPOINT=... pytest tests/test_integration.py -v` | Tests run and pass; if endpoint unavailable they skip with clear reason. |

## Manual Verification Steps (When Needed)

1. Open `tests/results/all_variants.ttl`.
2. Confirm each fixture event ID appears as `pbprdf:espnEventId`.
3. Confirm there are no obvious malformed literals (e.g., invalid attendance text in integer fields).
4. For NBA output, spot-check:
   - `pbprdf:hasWinProbabilitySnapshot`
   - `pbprdf:venue`, `pbprdf:hasOfficial`
   - `pbprdf:secondsIntoGame`, `pbprdf:eventNumber`

## Reporting Format

Write report to: `tests/results/integration_report_YYYY-MM-DD.md`

Use:

```markdown
# pbprdf Integration Report (YYYY-MM-DD)

## Summary
| Check | Status (PASS/FAIL/SKIP) | Notes |
|------|------|------|
| ... | ... | ... |

## Environment
- Python:
- OS:
- Endpoint configured:

## Detailed Findings
- [Check name] Expected: ... Actual: ...

## Artifacts
- tests/results/ontology_smoke.ttl
- tests/results/nba_smoke.ttl
- tests/results/all_variants.ttl

## Overall
- READY / NOT READY
```
