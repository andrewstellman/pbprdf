# pbprdf V2 Architecture Specification

## Purpose

This document replaces the Gemini-generated `ARCHITECTURE_SPEC.md` with a grounded specification based on actual ESPN API responses and the existing V1/V2 ontology specs.

It resolves open questions, specifies the real JSON schema, defines the technical stack with rationale, and lays out an incremental build plan.

---

## 1. ESPN API: Verified Schema (from live responses)

### Endpoint Pattern

```
https://site.web.api.espn.com/apis/site/v2/sports/basketball/{league}/summary?event={game_id}
```

League slugs:
- `nba`
- `wnba`
- `mens-college-basketball`
- `womens-college-basketball`

Game discovery (for building fetch scripts):
```
https://site.api.espn.com/apis/site/v2/sports/basketball/{league}/scoreboard?dates={YYYYMMDD}
```

### Top-Level Response Sections (Verified)

| Section | Description | Present in NBA? | Notes |
|---------|-------------|-----------------|-------|
| `boxscore` | Team and player stats | Yes | Structured stat arrays per player |
| `format` | Period/clock configuration | Yes | Machine-readable regulation and OT rules |
| `gameInfo` | Venue, attendance, officials | Yes | Structured address, referee names/positions |
| `leaders` | Per-team stat leaders | Yes | Points, assists, rebounds leaders with full stat lines |
| `plays` | Play-by-play array | Yes | Core mapping target |
| `header` | Competition metadata | Yes | Teams, status, linescores |
| `winprobability` | Win probability timeline | Yes | Keyed by play ID |
| `standings` | Team standings context | Yes | |
| `news` | Related articles | Yes | Ignore for V2 |
| `videos` | Highlight clips | Yes | Ignore for V2 |
| `odds` | Betting data | Varies | Ignore for V2 |

### `format` Section (Verified from NBA game 400878160)

```json
{
  "regulation": {
    "periods": 4,
    "displayName": "Quarter",
    "slug": "quarter",
    "clock": 720.0
  },
  "overtime": {
    "clock": 300.0
  }
}
```

This is machine-readable and removes the need for hardcoded `GamePeriodInfo` presets. The mapper should read this directly from the response.

Expected values by league:

| League | regulation.periods | regulation.clock | regulation.displayName | overtime.clock |
|--------|-------------------|-----------------|----------------------|---------------|
| NBA | 4 | 720.0 (12 min) | Quarter | 300.0 |
| WNBA | 4 | 600.0 (10 min) | Quarter | 300.0 |
| NCAAW | 4 | 600.0 (10 min) | Quarter | 300.0 |
| NCAAM | 2 | 1200.0 (20 min) | Half | 300.0 |

**Action item**: The fetcher must capture and cache real responses from each league to verify these values. Do not hardcode — read from `format` at map time.

### `boxscore.players[]` Schema (Verified)

Each entry in `boxscore.players[]` contains:

```
team: { id, displayName, abbreviation, ... }
statistics[0]:
  names: ["MIN","PTS","FG","3PT","FT","REB","AST","TO","STL","BLK","OREB","DREB","PF","+/-"]
  keys: ["minutes","points","fieldGoalsMade-fieldGoalsAttempted", ...]
  athletes[]:
    athlete:
      id: "1966"              # ESPN athlete ID (string)
      displayName: "LeBron James"
      shortName: "L. James"
      jersey: "23"
      position: { name, displayName, abbreviation }
    starter: true|false
    didNotPlay: true|false
    ejected: true|false
    stats: ["47","27","9-24","1-5","8-10","11","11","5","2","3","1","10","1","+4"]
```

Key observations:
- Stats are parallel arrays (names/keys define columns, stats[] is the row)
- `athlete.id` is the stable ESPN identifier
- `starter`, `didNotPlay`, `ejected` are structured booleans — no text parsing needed
- Position data is structured

### `gameInfo` Schema (Verified)

```
venue:
  id: "2465"
  fullName: "Oracle Arena"
  address: { city, state, zipCode }
  grass: false
  images: [...]
attendance: 19596
officials[]:
  fullName: "Mike Callahan"
  displayName: "Mike Callahan"
  position: { name: "Referee", displayName: "Referee" }
  order: 1
```

### `plays[]` Schema (From V2 Delta + API documentation)

Each play in the `plays[]` array:

```json
{
  "id": "4018104681234",
  "sequenceNumber": "12340",
  "type": {
    "id": "574",
    "text": "Three Point Jumper"
  },
  "text": "Stephen Curry makes three point jumper (Draymond Green assists)",
  "shortText": "S. Curry 3PT (D. Green AST)",
  "awayScore": 22,
  "homeScore": 25,
  "period": {
    "number": 1,
    "displayValue": "1st Quarter"
  },
  "clock": {
    "displayValue": "8:42"
  },
  "scoringPlay": true,
  "shootingPlay": true,
  "scoreValue": 3,
  "team": {
    "id": "9"
  },
  "participants": [
    {
      "athlete": {
        "id": "3975"
      },
      "type": "shooter"
    },
    {
      "athlete": {
        "id": "6589"
      },
      "type": "assister"
    }
  ],
  "coordinate": {
    "x": 24,
    "y": 33
  },
  "wallclock": "2016-06-20T02:14:32Z"
}
```

**Field presence varies by play type.** Not all plays have `coordinate`, `participants`, `scoreValue`, or `team`. The mapper must handle missing fields gracefully.

**Known participant role values** (from community documentation): `shooter`, `assister`, `blocker`, `fouler`, `drawer`, `rebounder`, `stealer`, `turnover`. These need verification across all four leagues.

---

## 2. Resolved Open Questions

These were flagged in the V2 Ontology Delta. Decisions below:

### Q: Should V2 IRIs be ESPN-ID-first or name-based?

**Decision: ESPN-ID-first for all new entities.**

- Games: `.../games/{espn_event_id}` (e.g., `.../games/400878160`)
- Teams: `.../teams/{espn_team_id}` (e.g., `.../teams/9`)
- Players: `.../players/{espn_athlete_id}` (e.g., `.../players/1966`)
- Events: `.../games/{espn_event_id}/plays/{espn_play_id}`

Rationale: ESPN IDs are stable, unique, and deterministic. Name-based IRIs have collision issues (e.g., multiple "James Jones" in NBA history). Human-readable names go in `rdfs:label`.

V1 backward compatibility: V1 name-based IRIs are not preserved. This is a clean break. If someone needs to join V1 and V2 graphs, they can match on `rdfs:label` or player/team names.

### Q: Should `scoreValue` override or complement `shotPoints`?

**Decision: Keep both, sourced differently.**

- `pbprdf:scoreValue` — from `plays[].scoreValue` (points actually awarded on this play)
- `pbprdf:shotPoints` — from `plays[].type.text` classification or `plays[].pointsAttempted` (type of shot attempted)

Example: A made 3-pointer has `scoreValue=3` and `shotPoints=3`. A missed 3-pointer has `scoreValue=0` (or absent) and `shotPoints=3`.

### Q: Full boxscore/stat ontology now or Phase 2?

**Decision: Phase 2.** The boxscore stat structure is complex (parallel arrays, position-dependent stat sets). Phase 1 focuses on the play graph. Boxscore stats can be modeled later as `PlayerGameStats` / `TeamGameStats` entities.

### Q: Model participants with explicit roles now or role-agnostic?

**Decision: Use roles from the start, but make them optional.**

The ESPN API provides typed participants (`shooter`, `assister`, etc.). These should map to `Participation` nodes with a `participantRole` property when the role is present. If the API doesn't provide a role for a participant, emit the `Participation` node without a role.

Also preserve V1 convenience properties (`shotBy`, `shotAssistedBy`, etc.) as shortcuts derived from participation data. This keeps simple SPARQL queries simple.

---

## 3. Technical Stack

### Language: Python 3.11+

Rationale: `rdflib` is the mature Python RDF library; `requests`/`httpx` for HTTP; AI integration via `anthropic` SDK; Jupyter for the query notebook interface; the data science ecosystem (pandas, matplotlib) is available for analysis and visualization.

### Core Dependencies

| Package | Purpose | Version |
|---------|---------|---------|
| `rdflib` | Graph construction, Turtle serialization, namespace management | >= 7.0 |
| `httpx` | HTTP client (async-capable, better than requests for batch fetching) | >= 0.27 |
| `typer` | CLI framework | >= 0.12 |
| `pydantic` | JSON response validation and typed models | >= 2.0 |
| `anthropic` | Claude API for SPARQL generation and result interpretation | >= 0.40 |

### Development Dependencies

| Package | Purpose |
|---------|---------|
| `pytest` | Testing |
| `ruff` | Linting and formatting |

### Why Pydantic?

The ESPN API is undocumented and can change without notice. Pydantic models for the API response give us:
- Typed access to nested fields
- Clear documentation of what we expect from each response
- Validation errors when the schema changes (instead of silent `KeyError` at runtime)
- Optional fields handled cleanly with `Optional[T]` / default `None`

---

## 4. Project Structure

```
pbprdf-v2/
├── README.md
├── pyproject.toml                # Project config, dependencies
├── specs/
│   ├── V1_SPEC.md               # V1 baseline (existing)
│   ├── V2_TARGET_ONTOLOGY_DELTA.md  # Ontology changes (existing)
│   └── V2_ARCHITECTURE_SPEC.md  # This document
├── src/
│   └── pbprdf/
│       ├── __init__.py
│       ├── cli.py               # Typer CLI: fetch, map, ontology, query commands
│       ├── config.py            # League definitions, namespace URIs, constants
│       ├── fetcher.py           # ESPN API client, scoreboard discovery, JSON caching
│       ├── models/
│       │   ├── __init__.py
│       │   ├── espn.py          # Pydantic models for ESPN summary response
│       │   └── game_format.py   # GameFormat from ESPN format section
│       ├── ontology.py          # Namespace definitions + TBox generation
│       ├── mapper/
│       │   ├── __init__.py
│       │   ├── core.py          # Main mapping loop: JSON → rdflib.Graph
│       │   ├── game.py          # Game-level triple generation
│       │   ├── roster.py        # Team/player/roster triple generation
│       │   ├── plays.py         # Play-by-play triple generation
│       │   ├── events.py        # Event chain (ordering, time deltas)
│       │   └── winprob.py       # Win probability timeline
│       └── query/
│           ├── __init__.py
│           ├── sparql_gen.py    # LLM-based natural language → SPARQL
│           └── interpret.py     # LLM-based result interpretation
├── tests/
│   ├── conftest.py
│   ├── fixtures/                # Captured ESPN JSON for each league
│   │   ├── nba_400878160.json
│   │   ├── wnba_XXXXXXX.json
│   │   ├── ncaam_XXXXXXX.json
│   │   └── ncaaw_XXXXXXX.json
│   ├── test_models.py           # Pydantic model parsing tests
│   ├── test_mapper_game.py
│   ├── test_mapper_plays.py
│   ├── test_mapper_roster.py
│   └── test_ontology.py
├── notebooks/
│   └── query_assistant.ipynb    # AI-assisted SPARQL notebook (Phase 3)
└── data/
    ├── raw/                     # Cached ESPN JSON (git-ignored)
    └── rdf/                     # Generated .ttl files (git-ignored)
```

---

## 5. Execution Pipeline

### Step 1: Discover Games

```bash
pbprdf discover --league nba --date 2025-03-08
# Queries scoreboard endpoint, prints game IDs and matchups
```

### Step 2: Fetch Game Data

```bash
pbprdf fetch --league nba --event 401810468
# Downloads summary JSON to data/raw/nba_401810468.json
# Skips if file already exists (use --force to re-fetch)

pbprdf fetch --league nba --date 2025-03-08
# Fetches all games from that date

pbprdf fetch --league nba --season 2025 --season-type 2
# Fetches all regular season games (iterates scoreboard by date)
```

### Step 3: Generate Ontology

```bash
pbprdf ontology --output data/rdf/ontology.ttl
# Generates TBox (OWL class/property definitions) for V2 vocabulary
```

### Step 4: Map to RDF

```bash
pbprdf map data/raw/nba_401810468.json --output data/rdf/nba_401810468.ttl
# Maps single game

pbprdf map data/raw/ --output data/rdf/all_games.ttl
# Maps all cached JSON files into a single graph

pbprdf map data/raw/ --output-dir data/rdf/
# Maps each file to its own .ttl
```

### Step 5: Query (Phase 3)

```bash
pbprdf query --db http://localhost:8080/rdf4j-server/repositories/pbprdf \
  "Which player has the highest 3-point percentage after the opposing team missed a 3?"
# Generates SPARQL, executes, interprets results
```

---

## 6. Key Design Decisions

### Fetch-then-Map Separation

The fetcher and mapper are completely independent. Fetching writes JSON to disk; mapping reads JSON from disk. This means:
- You can iterate on mapping logic without re-fetching
- You can share cached JSON between developers
- Rate limiting ESPN is a non-issue during development
- Test fixtures are just captured JSON files

### Pydantic Models as the Contract

The Pydantic models in `src/pbprdf/models/espn.py` serve as the documented contract between the ESPN API and the mapper. When ESPN changes their schema, the Pydantic validation will fail immediately with a clear error, rather than producing silently wrong RDF.

Sketch of key models:

```python
from pydantic import BaseModel
from typing import Optional

class PlayType(BaseModel):
    id: str
    text: str

class PlayClock(BaseModel):
    displayValue: str  # "8:42" or "42.3"

class PlayPeriod(BaseModel):
    number: int
    displayValue: Optional[str] = None

class PlayTeamRef(BaseModel):
    id: str

class AthleteRef(BaseModel):
    id: str

class PlayParticipant(BaseModel):
    athlete: AthleteRef
    type: Optional[str] = None  # "shooter", "assister", etc.

class Coordinate(BaseModel):
    x: int
    y: int

class Play(BaseModel):
    id: str
    sequenceNumber: Optional[str] = None
    type: PlayType
    text: str
    shortText: Optional[str] = None
    awayScore: Optional[int] = None
    homeScore: Optional[int] = None
    period: PlayPeriod
    clock: PlayClock
    scoringPlay: bool = False
    shootingPlay: bool = False
    scoreValue: Optional[int] = None
    pointsAttempted: Optional[int] = None
    team: Optional[PlayTeamRef] = None
    participants: list[PlayParticipant] = []
    coordinate: Optional[Coordinate] = None
    wallclock: Optional[str] = None

class RegulationFormat(BaseModel):
    periods: int
    displayName: str
    slug: str
    clock: float  # seconds

class OvertimeFormat(BaseModel):
    clock: float  # seconds

class GameFormat(BaseModel):
    regulation: RegulationFormat
    overtime: OvertimeFormat
```

### Time Computation

Clock parsing and `secondsIntoGame` computation use the `GameFormat` from the response, not hardcoded league presets:

```python
def compute_seconds_into_game(
    period: int,
    clock_display: str,
    game_format: GameFormat
) -> int:
    period_length = game_format.regulation.clock
    reg_periods = game_format.regulation.periods
    ot_length = game_format.overtime.clock

    seconds_left = parse_clock(clock_display)

    if period <= reg_periods:
        elapsed_full_periods = (period - 1) * period_length
        current_elapsed = period_length - seconds_left
    else:
        elapsed_regulation = reg_periods * period_length
        elapsed_ot_periods = (period - reg_periods - 1) * ot_length
        current_elapsed = ot_length - seconds_left
        elapsed_full_periods = elapsed_regulation + elapsed_ot_periods

    return int(elapsed_full_periods + current_elapsed)
```

### Ontology (TBox) Generation

V1 generated the TBox via Scala reflection on annotated classes. V2 generates it programmatically from a Python module that defines all classes and properties:

```python
# ontology.py (sketch)
from rdflib import Graph, Namespace, OWL, RDF, RDFS, XSD, Literal

PBPRDF = Namespace("http://stellman-greene.com/pbprdf#")

def generate_ontology() -> Graph:
    g = Graph()
    g.bind("pbprdf", PBPRDF)

    # Classes
    for cls in [
        "Game", "Event", "Play", "Team", "Player", "Roster",
        "Shot", "Block", "Foul", "TechnicalFoul", "Turnover",
        "Rebound", "JumpBall", "Timeout", "Enters", "Ejection",
        "EndOfPeriod", "EndOfGame", "FiveSecondViolation",
        # V2 additions
        "PlayType", "CourtCoordinate", "Participation",
        "WinProbabilitySnapshot", "Venue", "Official", "GameFormat",
    ]:
        g.add((PBPRDF[cls], RDF.type, OWL.Class))
        g.add((PBPRDF[cls], RDFS.label, Literal(cls)))

    # Subclass relationships
    g.add((PBPRDF.Block, RDFS.subClassOf, PBPRDF.Shot))
    g.add((PBPRDF.Play, RDFS.subClassOf, PBPRDF.Event))
    # ...

    return g
```

### V1 Compatibility Properties

The mapper generates BOTH V1-style convenience properties AND V2 participation nodes:

```turtle
# V1-style (kept for backward compatibility and query simplicity)
<.../plays/123> pbprdf:shotBy <.../players/3975> .
<.../plays/123> pbprdf:shotAssistedBy <.../players/6589> .

# V2-style (richer, supports multi-actor plays)
<.../plays/123> pbprdf:hasParticipation _:p1 .
_:p1 pbprdf:participantPlayer <.../players/3975> .
_:p1 pbprdf:participantRole "shooter" .

<.../plays/123> pbprdf:hasParticipation _:p2 .
_:p2 pbprdf:participantPlayer <.../players/6589> .
_:p2 pbprdf:participantRole "assister" .
```

---

## 7. Testing Strategy

### Test Fixtures

Real captured ESPN JSON responses, one per league. These are committed to the repo (they're public data) and serve as the ground truth for all mapping tests.

**Fixture capture process:**
1. Run `pbprdf fetch` for one completed game per league
2. Copy the JSON to `tests/fixtures/`
3. Commit to git

### Test Structure

**Model tests** (`test_models.py`): Parse each fixture through Pydantic models. Verify field extraction for plays, boxscore, format, gameInfo. These tests break first when ESPN changes their schema.

**Mapper tests** (`test_mapper_*.py`): Load fixture JSON, run mapper, query the resulting graph with SPARQL to verify expected triples. Example:

```python
def test_nba_game_has_correct_format(nba_graph):
    results = nba_graph.query("""
        SELECT ?periods ?clock WHERE {
            ?fmt a pbprdf:GameFormat .
            ?fmt pbprdf:regulationPeriodCount ?periods .
            ?fmt pbprdf:regulationPeriodLengthMinutes ?clock .
        }
    """)
    row = list(results)[0]
    assert int(row.periods) == 4
    assert int(row.clock) == 12

def test_shot_has_coordinates(nba_graph):
    results = nba_graph.query("""
        SELECT (COUNT(?shot) AS ?count) WHERE {
            ?shot a pbprdf:Shot .
            ?shot pbprdf:hasCoordinate ?coord .
            ?coord pbprdf:coordinateX ?x .
            ?coord pbprdf:coordinateY ?y .
        }
    """)
    count = int(list(results)[0][0])
    assert count > 0  # At least some shots have coordinates
```

**Ontology tests** (`test_ontology.py`): Generate ontology, verify all V1 + V2 classes and properties are present.

### Cross-League Diff Tests

A dedicated test that loads all four fixtures and verifies:
- All four produce valid graphs with no errors
- `GameFormat` values differ as expected (quarters vs halves, clock lengths)
- Field presence differences are handled (some leagues may lack coordinates)

---

## 8. AI-Assisted Query Interface (Phase 3)

### Architecture

```
User question (natural language)
    ↓
[sparql_gen.py] — sends ontology + example queries + question to Claude
    ↓
Generated SPARQL query
    ↓
[Execute against RDF4J via HTTP SPARQL endpoint]
    ↓
Raw SPARQL results (JSON)
    ↓
[interpret.py] — sends results + original question to Claude
    ↓
- Formatted table
- Natural language interpretation
- Suggested follow-up queries
```

### Prompt Design for SPARQL Generation

The prompt to Claude includes:
1. The full V2 ontology (Turtle format, ~200 triples)
2. 5-10 curated example SPARQL queries (from the V1 README + new V2 examples)
3. The user's natural language question
4. Instructions to output only valid SPARQL

The ontology is small enough to fit comfortably in the context window.

### Notebook Interface

A Jupyter notebook (`query_assistant.ipynb`) with:
- A helper function `ask(question: str)` that runs the full pipeline
- Results displayed as pandas DataFrames
- Interpretation displayed as Markdown
- The generated SPARQL shown in a collapsible cell for transparency

This is more practical than a custom web UI for the initial version. The notebook gives you iteration, history, and visualization for free.

---

## 9. Phased Build Plan

### Phase 1: Foundation (Target: working graph for one NBA game)

1. **Capture test fixtures** — fetch and save one game per league
2. **Pydantic models** — model the ESPN summary response with validation
3. **Ontology module** — namespace definitions + TBox generation
4. **Core mapper** — JSON → rdflib.Graph for game, rosters, plays, event chain
5. **CLI** — `fetch`, `map`, `ontology` commands
6. **Tests** — model parsing, mapper output verification via SPARQL

Deliverable: `pbprdf fetch && pbprdf map && load into RDF4J && run V1-style queries`

### Phase 2: Breadth and Depth

1. **Multi-league validation** — verify mapper against WNBA, NCAAM, NCAAW fixtures
2. **Batch fetching** — season-level fetch with date iteration and rate limiting
3. **Win probability timeline** mapping
4. **Venue and officials** mapping
5. **Coordinate/spatial** verification across leagues
6. **Boxscore stat entities** (if demand warrants)

### Phase 3: AI Query Interface

1. **SPARQL generation** — Claude-based natural language → SPARQL
2. **Result interpretation** — Claude-based result → narrative
3. **Jupyter notebook** — `ask()` helper function
4. **Example query library** — curated queries demonstrating V2 capabilities

### Phase 4: Quality (Stretch)

1. Identity reconciliation (same player across seasons/teams)
2. Provenance metadata (which triples came from which source field)
3. Inference rules (shot taxonomy, foul classification)
4. Performance optimization for large graphs

---

## 10. What This Spec Intentionally Does NOT Cover

- **RDF4J server setup** — The V1 README already covers this; unchanged for V2.
- **Deployment/hosting** — This is a local CLI tool and notebook. No web server needed.
- **ESPN API stability guarantees** — The API is undocumented. The Pydantic models are the defense against schema drift. The fixture-based tests catch regressions.
- **V1 migration tooling** — V2 is a clean break. V1 and V2 graphs can coexist in RDF4J but use different IRI schemes.
