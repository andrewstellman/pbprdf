# pbprdf Code Review Protocol

## Bootstrap: Read These First

1. `tests/QUALITY.md`
2. `AGENTS.md`
3. `specs/V2_ARCHITECTURE_SPEC.md`
4. `specs/V2_TARGET_ONTOLOGY_DELTA.md`
5. `specs/V1_SPEC.md`

## What to Check

### 1) Play/Event Semantic Correctness

- **Where:** `src/pbprdf/mapper/plays.py`, `src/pbprdf/mapper/events.py`
- **What:** Classification logic, participant role mapping, shot/foul/turnover semantics, coordinate filtering, event sequencing and delta calculations.
- **Why:** Wrong event typing or ordering silently corrupts RDF analytics and downstream SPARQL results.

### 2) Temporal and Format Logic

- **Where:** `src/pbprdf/mapper/game.py`, `src/pbprdf/mapper/plays.py`
- **What:** Regulation/OT period derivation from `format`, clock parsing (`MM:SS` and decimal forms), fallback defaults.
- **Why:** Time math defects propagate to every event and invalidate timeline-based queries.

### 3) Identity and Cross-Module Linking

- **Where:** `src/pbprdf/mapper/ids.py`, `src/pbprdf/mapper/roster.py`, `src/pbprdf/mapper/winprob.py`
- **What:** ESPN-ID-based IRIs, team/player ID propagation, `playId -> play IRI` linking for win probability snapshots.
- **Why:** Broken identity creates detached subgraphs and duplicate/missing entities.

### 4) External Data Robustness

- **Where:** `src/pbprdf/models/espn.py`, `src/pbprdf/mapper/venue.py`, `src/pbprdf/fetcher.py`
- **What:** Optional/malformed fields handling, schema assumptions, defensive guards around non-dict/missing structures.
- **Why:** ESPN payload variability is the main risk; brittle assumptions create runtime failures or silent data loss.

### 5) Ontology and Query Surface Consistency

- **Where:** `src/pbprdf/ontology.py`, `src/pbprdf/query/endpoint.py`, `src/pbprdf/cli.py`
- **What:** Class/property inventory, domains/ranges, endpoint defaults/env overrides, CLI command behavior and failures.
- **Why:** Vocabulary drift and endpoint misconfiguration break compatibility and operability.

## Guardrails

- **Line numbers are mandatory.** If you cannot cite a specific line, do not include the finding.
- **Read function bodies, not just signatures.** Don't assume a function works correctly based on its name.
- **If unsure whether something is a bug or intentional**, flag it as a QUESTION rather than a BUG.
- **Grep before claiming missing.** If you think a feature is absent, search the codebase. If found in a different file, that's a location defect, not a missing feature.
- **Do NOT suggest style changes, refactors, or improvements.** Only flag things that are incorrect or could cause failures.

## Output Format

Write output to: `tests/code_reviews/YYYY-MM-DD_<reviewer>_code_review.md`

Use this exact finding format:

```markdown
### filename.py
- **Line NNN:** [BUG / QUESTION / INCOMPLETE] Description. Expected vs. actual. Why it matters.
```

Finish with:

- Total findings by severity/type
- Files reviewed with no findings
- Overall assessment: `SHIP IT` / `FIX FIRST` / `NEEDS DISCUSSION`
