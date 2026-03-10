# pbprdf Agent Bootstrap

`pbprdf` is a Python CLI that converts ESPN basketball summary JSON into RDF/Turtle using a project ontology. It supports NBA, WNBA, NCAAM, and NCAAW, and emphasizes stable ESPN-ID-based identity plus queryable event semantics. Primary input is ESPN summary JSON (fixtures or fetched data); primary output is RDF graphs serialized as Turtle and optionally loaded into a SPARQL endpoint.

Core stack: Python 3.10+, `rdflib`, `pydantic`, `httpx`, `typer`, `SPARQLWrapper`. Core risk surface is mapper correctness under schema variability (optional/malformed fields), because silent semantic drift is worse than explicit failures.

## Setup

```bash
git clone https://github.com/andrewstellman/pbprdf.git
cd pbprdf
python -m venv .venv
source .venv/bin/activate
pip install -e .
pip install pytest
```

## Build and Test

```bash
# Run all tests (functional + regression)
pytest tests/ -v

# Run with coverage
pytest tests/ --cov=pbprdf --cov-report=term-missing

# Quick smoke test (primary flow)
python -m pbprdf.cli map tests/fixtures/nba_401810770.json --output tests/results/smoke_nba.ttl
```

## Architecture

Data flow: ESPN JSON -> `SummaryResponse` validation (`models/espn.py`) -> game/venue/roster/play/winprob mapping (`mapper/`) -> event chain linking (`mapper/events.py`) -> RDF graph serialization (`cli.py map`) -> optional load/query via SPARQL endpoint (`query/endpoint.py`, `cli.py load/query`).

Modules:
- `src/pbprdf/models/espn.py`: typed contract for ESPN summary payloads.
- `src/pbprdf/mapper/game.py`: game identity, teams, game format metadata.
- `src/pbprdf/mapper/roster.py`: team/player materialization and roster membership.
- `src/pbprdf/mapper/plays.py`: event/play typing, semantics, coordinates, actors.
- `src/pbprdf/mapper/events.py`: deterministic event ordering and prev/next/delta links.
- `src/pbprdf/mapper/venue.py`: venue, officials, attendance, V1 location compatibility.
- `src/pbprdf/mapper/winprob.py`: win-probability snapshots linked to plays.
- `src/pbprdf/ontology.py`: TBox generation for V1+V2 vocabulary.
- `src/pbprdf/query/endpoint.py`: triplestore query/load abstraction.
- `src/pbprdf/cli.py`: operational entrypoint (`fetch`, `map`, `ontology`, `load`, `query`).

## Key Design Decisions

- ESPN IDs are canonical identity (`espnEventId`, `espnPlayId`, `espnTeamId`, `espnAthleteId`).
- Unrecognized play types remain `Event` only (avoid false semantic typing).
- Time calculations use payload `format` values; defaults are defensive fallbacks.
- Mapping separates fetch from transform so fixtures are reproducible and offline-testable.
- V1 compatibility properties are preserved where useful (e.g., `gameLocation`).
- Win-probability rows with unknown `playId` are skipped to prevent orphan snapshots.

## Known Quirks / Gotchas

- ESPN schema is unofficial and may drift without notice.
- `clock.displayValue` can be `MM:SS` or decimal seconds-like format (`SS.fraction`).
- Some play fields are optional per play type (`participants`, `coordinate`, `scoreValue`, `team`).
- `sequenceNumber` may be missing/non-numeric; mapper falls back to deterministic ordering.
- Venue/gameInfo sections can be absent or malformed in some payloads.

## Quality Docs

- Quality playbook (generates quality infrastructure): `QUALITY_PLAYBOOK.md`
- Intent/spec documents: `specs/V2_ARCHITECTURE_SPEC.md`, `specs/V2_TARGET_ONTOLOGY_DELTA.md`, `specs/V1_SPEC.md`, `specs/CURSOR_PHASE2_TRIPLESTORE.md`
