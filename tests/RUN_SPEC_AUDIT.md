# pbprdf Spec Audit Protocol (Council of Three)

## Definitive Audit Prompt

Use this exact prompt with three independent AI reviewers:

```text
Act as the Tester. Read the actual pbprdf code and strictly compare it against the listed specs.

Context files to read first:
- tests/QUALITY.md
- specs/V2_ARCHITECTURE_SPEC.md
- specs/V2_TARGET_ONTOLOGY_DELTA.md
- specs/V1_SPEC.md
- AGENTS.md

Code scope:
- src/pbprdf/models/espn.py
- src/pbprdf/mapper/game.py
- src/pbprdf/mapper/roster.py
- src/pbprdf/mapper/plays.py
- src/pbprdf/mapper/events.py
- src/pbprdf/mapper/venue.py
- src/pbprdf/mapper/winprob.py
- src/pbprdf/ontology.py
- src/pbprdf/cli.py
- src/pbprdf/query/endpoint.py

Rules:
- ONLY list defects. Do not summarize what matches.
- For EVERY defect, cite specific file and line number(s).
  If you cannot cite a line number, do not include the finding.
- Before claiming missing, grep the codebase.
- Before claiming exists, read the actual function body.
- Classify each finding as exactly one of:
  MISSING / DIVERGENT / UNDOCUMENTED / PHANTOM

Project-specific scrutiny questions:
1) Does play classification rely on structured ESPN type/flags where available, and avoid false specialized typing for unknown plays?
2) Are event identity and linking fully ESPN-ID-based (`espnEventId`, `espnPlayId`, `espnTeamId`, `espnAthleteId`) without fallback collisions?
3) Is time computation correctly derived from `format` with defensible defaults across NBA/WNBA/NCAAM/NCAAW?
4) Are malformed or optional external fields (gameInfo/officials/attendance/coordinates/participants/sequenceNumber) handled without crashes or semantic pollution?
5) Are event chains deterministic when sequence numbers are missing or non-numeric?
6) Is win probability mapping correctly linked to existing plays, skipping unmatched IDs without orphan triples?
7) Do ontology class/property definitions align with mapped usage and stated V2 additions?
8) Do CLI behaviors (`fetch`, `map`, `ontology`, `load`, `query`) align with architecture intent and degrade safely on endpoint failures?
9) Are there behavior differences from `specs/V1_SPEC.md` that are undocumented intentional divergences?
10) Are there requirements in specs with no corresponding automated functional coverage in `tests/test_functional.py`?

Output format:
### path/to/file.py
- Line NNN: [MISSING|DIVERGENT|UNDOCUMENTED|PHANTOM] concise defect statement (expected vs actual, why it matters).
```

## Triage Process

1. Run three independent audits (Reviewer A/B/C) with the exact prompt.
2. Merge findings into `tests/spec_audits/YYYY-MM-DD_merged.md`.
3. Confidence:
   - 3/3 reviewers: highest confidence
   - 2/3 reviewers: high confidence
   - 1/3 reviewer: verify before action
4. Categorize each finding:
   - Spec bug (update spec)
   - Design decision (human decision needed)
   - Real code bug (fix in subsystem batches)
   - Documentation gap (update docs)
   - Missing test (add to `tests/test_functional.py`)

## Verification Probe

When reviewers disagree on factual claims:

1. Create a read-only probe prompt with disputed claim and relevant files.
2. Ask one model to report ground truth with line citations only.
3. Resolve based on probe evidence, not majority vote.

## Fix Execution Rules

- Group fixes by subsystem (e.g., plays/time/ontology), not by finding number.
- Avoid mega-prompts; execute small focused batches.
- For each batch: implement -> run tests -> re-audit diff with at least two reviewers.
- Do not mark complete until at least two reviewers confirm the fix set.
