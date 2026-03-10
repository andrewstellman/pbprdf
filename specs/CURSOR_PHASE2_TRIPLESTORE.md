# Cursor Task: Phase 2 — Triplestore Integration + Breadth

## Context

You are continuing work on `pbprdf-v2` (in `v2/pbprdf-v2/`), which converts ESPN basketball play-by-play JSON data into RDF triples serialized as Turtle.

Phase 0 (fixture capture) and Phase 1 (core mapper) are complete. The mapper produces valid Turtle output from ESPN JSON for all four basketball leagues (NBA, WNBA, NCAAM, NCAAW). All 21 existing tests pass.

Read the attached specs for full context:
- `specs/V2_ARCHITECTURE_SPEC.md` — Architecture decisions, especially **Section 8: Triplestore Strategy** and **Section 10: Phased Build Plan**
- `specs/V2_TARGET_ONTOLOGY_DELTA.md` — V2 ontology (Sections 5, 6 are relevant to this phase: win probability, venue/officials)
- `specs/V1_SPEC.md` — V1 baseline

Also review the existing Phase 1 code in `src/pbprdf/` and `tests/` to understand what's already built.

## Your Tasks

### 1. Add SPARQLWrapper Dependency

In `pyproject.toml`, add `"sparqlwrapper>=2.0"` to `dependencies`.

### 2. Implement the Query Layer

Create `src/pbprdf/query/__init__.py` and `src/pbprdf/query/endpoint.py`:

```python
"""
Triplestore-agnostic SPARQL query layer.

Uses SPARQLWrapper to talk to any SPARQL 1.1 compliant endpoint
over HTTP. Works identically against Fuseki, Blazegraph, or Virtuoso.
"""
```

#### `endpoint.py`

Implement these functions:

**`get_endpoint() -> str`**
- Read from `PBPRDF_SPARQL_ENDPOINT` environment variable
- Default: `"http://localhost:3030/pbprdf/sparql"` (Fuseki default)

**`get_update_endpoint() -> str`**
- Read from `PBPRDF_SPARQL_UPDATE_ENDPOINT` environment variable
- Fall back to `get_endpoint()` if not set (most stores use same endpoint)

**`get_graph_store_endpoint() -> str`**
- Read from `PBPRDF_GRAPH_STORE_ENDPOINT` environment variable
- Fall back to deriving from the SPARQL endpoint:
  - Fuseki: replace `/sparql` with `/data` (e.g., `http://localhost:3030/pbprdf/data`)
  - Default: just use the SPARQL endpoint (some stores support graph store on the same URL)

**`query(sparql_query: str) -> list[dict]`**
- Create `SPARQLWrapper` with `get_endpoint()`
- Set query, return format JSON
- Execute and return `results["results"]["bindings"]`
- Each binding dict has keys like `{"var_name": {"type": "uri", "value": "http://..."}}` — keep this raw format, let callers extract values

**`query_ask(sparql_query: str) -> bool`**
- For ASK queries, return the boolean result

**`update(sparql_update: str) -> None`**
- For SPARQL UPDATE operations (INSERT DATA, DROP GRAPH, etc.)
- Use `get_update_endpoint()`
- Set method to POST

**`load_turtle(turtle_path: Path, graph_uri: str | None = None) -> None`**
- Load a Turtle file into the triplestore via SPARQL Graph Store Protocol
- HTTP POST the file content to the graph store endpoint
- Content-Type: `text/turtle`
- If `graph_uri` is provided, append `?graph=<uri>` to the URL
- If `graph_uri` is None, use `?default` (loads into default graph)
- Use `httpx` for this (SPARQLWrapper doesn't support graph store protocol)
- Raise a clear error if the POST fails (include HTTP status and response body)

**`load_rdflib_graph(graph: Graph, graph_uri: str | None = None) -> None`**
- Serialize an rdflib Graph to Turtle bytes, then load via the same graph store protocol
- Useful for loading the ontology graph directly without writing to disk first

### 3. Add CLI Commands

In `src/pbprdf/cli.py`, add two new commands:

#### `load` command
```bash
pbprdf load INPUT --graph GRAPH_URI
```

- `INPUT` is a Turtle file path or a directory of `.ttl` files
- `--graph` is optional; if provided, load into that named graph
- If INPUT is a directory, load each `.ttl` file separately (print progress)
- Print the triplestore endpoint being used
- Print success/failure for each file

#### `query` command (basic version)
```bash
pbprdf query "SELECT * WHERE { ?s ?p ?o } LIMIT 10"
```

- Takes a SPARQL query string as a positional argument
- Executes against the configured endpoint
- Pretty-prints the results as a table
- For simple output, format as tab-separated values with a header row
- If the query is an ASK query, print "true" or "false"

### 4. Implement Win Probability Mapping

Create `src/pbprdf/mapper/winprob.py`:

The ESPN response has a `winprobability` array at the top level:
```json
{
  "winprobability": [
    {
      "playId": "4018107702",
      "homeWinPercentage": 0.514,
      "tiePercentage": 0,
      "secondsLeft": 2880
    },
    ...
  ]
}
```

#### Pydantic model additions

In `src/pbprdf/models/espn.py`, add:

```python
class WinProbabilityEntry(BaseModel):
    playId: str
    homeWinPercentage: float
    tiePercentage: float = 0.0
    secondsLeft: Optional[int] = None
```

Add to `SummaryResponse`:
```python
winprobability: list[WinProbabilityEntry] = []
```

Note: The `SummaryResponse` model uses `model_config = {"extra": "allow"}`, so `winprobability` may already be captured as extra data. You need to promote it to an explicit field with the typed model.

#### Mapper

**`map_win_probability(game_iri, plays, winprob_entries, graph) -> None`**

For each `WinProbabilityEntry`:
- Create a blank node (or IRI) for the snapshot
- `rdf:type pbprdf:WinProbabilitySnapshot`
- `pbprdf:homeWinProbability` — the `homeWinPercentage` as `xsd:decimal`
- `pbprdf:tieProbability` — the `tiePercentage` as `xsd:decimal`
- `pbprdf:snapshotForPlay` — link to the play node using `playId` → look up the play IRI from the plays that were already mapped. Build a dict from `espnPlayId` → play IRI for lookup.
- `(game, pbprdf:hasWinProbabilitySnapshot, snapshot)`

If a `playId` in the win probability data doesn't match any mapped play, skip that entry silently (this can happen for pre-game entries).

#### Integration with core mapper

In `src/pbprdf/mapper/core.py`, after mapping plays:
1. Build a `play_id_to_iri` dict from the mapped plays
2. Call `map_win_probability()` if `winprobability` data is present in the response

### 5. Implement Venue and Officials Mapping

Create `src/pbprdf/mapper/venue.py`:

The ESPN response has venue and officials in `gameInfo`:
```json
{
  "gameInfo": {
    "venue": {
      "id": "1844",
      "fullName": "Target Center",
      "address": {
        "city": "Minneapolis",
        "state": "MN"
      }
    },
    "attendance": 17136,
    "officials": [
      {
        "fullName": "Mark Ayotte",
        "displayName": "Mark Ayotte",
        "position": {
          "name": "referee",
          "displayName": "Referee"
        },
        "order": 1
      }
    ]
  }
}
```

#### Mapper

**`map_venue(game_iri, game_info, graph) -> None`**

For the venue:
- IRI: `http://stellman-greene.com/pbprdf/venues/{venue_id}`
- `rdf:type pbprdf:Venue`
- `rdfs:label` — the `fullName`
- `pbprdf:venueName` — the `fullName`
- `pbprdf:venueId` — the `id` as string
- `pbprdf:venueAddress` — formatted as `"city, state"` if both present, just city or state if only one
- `(game, pbprdf:venue, venue_iri)`
- `(game, pbprdf:gameLocation, "fullName, city, state")` — V1 compatibility (keep the existing flat string)

For attendance:
- `(game, pbprdf:attendance, attendance_value)` as `xsd:int`

For each official:
- Create a blank node
- `rdf:type pbprdf:Official`
- `rdfs:label` — the `displayName`
- `pbprdf:officialName` — the `fullName`
- `pbprdf:officialPosition` — from `position.displayName` if present
- `(game, pbprdf:hasOfficial, official_node)`

Handle missing data gracefully — not all games have venue, attendance, or officials.

#### Ontology updates

In `src/pbprdf/ontology.py`, add the new classes and properties if not already present:
- Classes: `Venue`, `Official`
- Properties: `venue`, `venueName`, `venueId`, `venueAddress`, `attendance`, `hasOfficial`, `officialName`, `officialPosition`

Also add `WinProbabilitySnapshot` class and its properties if not already present:
- `hasWinProbabilitySnapshot`, `snapshotForPlay`, `homeWinProbability`, `tieProbability`

Check what's already defined in `ontology.py` before adding — Phase 1 may have defined some of these.

#### Integration with core mapper

In `src/pbprdf/mapper/core.py`, call `map_venue()` after mapping game-level triples.

### 6. Implement Batch Fetching

Extend `src/pbprdf/fetcher.py` with:

**`fetch_date(league: str, date: str) -> list[Path]`**
- Hit the scoreboard endpoint for the given date
- Find all completed games (`status.type.completed == true`)
- Fetch the summary for each completed game
- Save each to `data/raw/{league}_{game_id}.json`
- Skip files that already exist (unless force=True)
- Return list of saved paths
- Add 1-2 second delay between requests

**`fetch_season(league: str, season: int, season_type: int = 2) -> list[Path]`**
- Season type 2 = regular season, 3 = playoffs
- Iterate dates for the season:
  - NBA regular season: ~Oct 20 to ~Apr 15
  - WNBA regular season: ~May 15 to ~Sep 15
  - College: ~Nov 1 to ~Apr 10
- For each date, call `fetch_date()`
- Print progress (date being fetched, games found)
- Return all saved paths

Add CLI commands:

```bash
# Fetch all games from a specific date
pbprdf fetch --league nba --date 2026-03-07

# Fetch a full season (use with caution — many requests!)
pbprdf fetch --league nba --season 2026 --season-type 2
```

The `--event` option should still work for fetching a single game.

### 7. Write Tests

#### `tests/test_query.py` — Query layer unit tests

These should NOT require a live triplestore. Test the configuration logic:

```python
def test_get_endpoint_default():
    """Default endpoint is Fuseki."""
    # Clear env var if set, verify default

def test_get_endpoint_from_env(monkeypatch):
    """Endpoint reads from environment."""
    monkeypatch.setenv("PBPRDF_SPARQL_ENDPOINT", "http://example.com/sparql")
    assert get_endpoint() == "http://example.com/sparql"

def test_get_graph_store_endpoint_fuseki(monkeypatch):
    """Fuseki graph store derived from SPARQL endpoint."""
    monkeypatch.setenv("PBPRDF_SPARQL_ENDPOINT", "http://localhost:3030/pbprdf/sparql")
    # Verify it derives /data endpoint
```

#### `tests/test_integration.py` — Live triplestore tests

All tests in this file should be skipped when no endpoint is configured:

```python
import os
import pytest

pytestmark = pytest.mark.skipif(
    not os.environ.get("PBPRDF_SPARQL_ENDPOINT"),
    reason="No triplestore endpoint configured (set PBPRDF_SPARQL_ENDPOINT)"
)
```

Tests:
- **`test_load_ontology`**: Generate ontology, load into triplestore, query to verify classes exist
- **`test_load_and_query_game`**: Map NBA fixture, load into triplestore, run a shot count query, verify results
- **`test_win_probability_loaded`**: Load NBA fixture graph (with win probability), query for snapshots
- **`test_venue_loaded`**: Load NBA fixture graph, query for venue information
- **`test_cross_league_load`**: Load all four fixtures, verify game count query returns 4

Each integration test should:
1. Use a unique named graph (e.g., `http://stellman-greene.com/pbprdf/test/{test_name}`)
2. Clean up after itself (DROP GRAPH)
3. Handle connection errors gracefully (skip with a message, don't fail the test suite)

#### `tests/test_mapper_winprob.py` — Win probability mapping tests

Using the NBA fixture (which has win probability data):

```python
def test_win_probability_snapshots_exist(nba_graph):
    """Win probability snapshots are generated."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?snap) AS ?count) WHERE {
            ?snap a pbprdf:WinProbabilitySnapshot .
        }
    """)
    count = int(list(results)[0][0])
    assert count > 0  # NBA games have hundreds of win prob entries

def test_win_probability_linked_to_game(nba_graph):
    """Snapshots are linked to the game."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?game WHERE {
            ?game pbprdf:hasWinProbabilitySnapshot ?snap .
        }
    """)
    games = set(str(r[0]) for r in results)
    assert len(games) == 1  # All snapshots link to the same game

def test_win_probability_linked_to_play(nba_graph):
    """At least some snapshots are linked to plays."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?snap) AS ?count) WHERE {
            ?snap a pbprdf:WinProbabilitySnapshot .
            ?snap pbprdf:snapshotForPlay ?play .
            ?play a pbprdf:Play .
        }
    """)
    count = int(list(results)[0][0])
    assert count > 0

def test_win_probability_values_range(nba_graph):
    """Win probabilities are between 0 and 1."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?prob WHERE {
            ?snap pbprdf:homeWinProbability ?prob .
        }
    """)
    for row in results:
        prob = float(row[0])
        assert 0.0 <= prob <= 1.0
```

#### `tests/test_mapper_venue.py` — Venue and officials mapping tests

```python
def test_venue_exists(nba_graph):
    """NBA game has a venue."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?venue ?name WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:venue ?venue .
            ?venue pbprdf:venueName ?name .
        }
    """)
    rows = list(results)
    assert len(rows) == 1
    # The NBA fixture is Orlando at Minnesota, so venue should be Target Center
    assert "Target Center" in str(rows[0][1])

def test_officials_exist(nba_graph):
    """NBA game has officials."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?off) AS ?count) WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:hasOfficial ?off .
            ?off a pbprdf:Official .
        }
    """)
    count = int(list(results)[0][0])
    assert count >= 1  # NBA games typically have 3 officials

def test_attendance(nba_graph):
    """NBA game has attendance."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?att WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:attendance ?att .
        }
    """)
    rows = list(results)
    assert len(rows) == 1
    assert int(rows[0][0]) > 0

def test_game_location_v1_compat(nba_graph):
    """V1-compatible gameLocation string still present."""
    results = nba_graph.query("""
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?loc WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:gameLocation ?loc .
        }
    """)
    rows = list(results)
    assert len(rows) == 1
```

#### Update `tests/conftest.py`

The existing conftest loads fixtures and creates graphs. Make sure the graph fixtures now include the new mappers (win probability, venue/officials). The `nba_graph` fixture should produce a graph with all Phase 2 features.

If the existing conftest uses `map_game()` and that function now calls the new mappers internally, the fixtures should just work. Verify this.

### 8. Update Ontology Tests

In `tests/test_ontology.py`, add tests for the new Phase 2 classes and properties:

```python
def test_venue_class_exists(ontology_graph):
    assert (PBPRDF.Venue, RDF.type, OWL.Class) in ontology_graph

def test_official_class_exists(ontology_graph):
    assert (PBPRDF.Official, RDF.type, OWL.Class) in ontology_graph

def test_win_probability_class_exists(ontology_graph):
    assert (PBPRDF.WinProbabilitySnapshot, RDF.type, OWL.Class) in ontology_graph

def test_venue_properties(ontology_graph):
    assert (PBPRDF.venueName, RDF.type, OWL.DatatypeProperty) in ontology_graph
    assert (PBPRDF.venueId, RDF.type, OWL.DatatypeProperty) in ontology_graph

def test_win_probability_properties(ontology_graph):
    assert (PBPRDF.homeWinProbability, RDF.type, OWL.DatatypeProperty) in ontology_graph
    assert (PBPRDF.snapshotForPlay, RDF.type, OWL.ObjectProperty) in ontology_graph
```

### 9. Verify End-to-End

After implementation, run these verification steps:

1. **All existing tests still pass**: `pytest tests/ -v` — this is critical. Phase 2 must not break Phase 1.

2. **New tests pass**: All new tests in `test_query.py`, `test_mapper_winprob.py`, `test_mapper_venue.py` pass.

3. **Triple count increases**: Map the NBA fixture and verify the triple count is higher than Phase 1's ~18,000 (should be significantly more with win probability snapshots — probably 20,000+).

4. **Sample SPARQL queries work**: Run these against the in-memory rdflib graph to verify the new data:

```python
# Win probability at end of game
results = graph.query("""
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    SELECT ?prob WHERE {
        ?snap a pbprdf:WinProbabilitySnapshot .
        ?snap pbprdf:homeWinProbability ?prob .
        ?snap pbprdf:snapshotForPlay ?play .
        ?play pbprdf:period 4 .
    }
    ORDER BY DESC(?prob)
    LIMIT 1
""")

# Venue query
results = graph.query("""
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?game ?venue ?attendance WHERE {
        ?game a pbprdf:Game .
        ?game pbprdf:venue ?v .
        ?v pbprdf:venueName ?venue .
        ?game pbprdf:attendance ?attendance .
    }
""")

# Officials for a game
results = graph.query("""
    PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?name ?position WHERE {
        ?game a pbprdf:Game .
        ?game pbprdf:hasOfficial ?off .
        ?off pbprdf:officialName ?name .
        OPTIONAL { ?off pbprdf:officialPosition ?position }
    }
""")
```

5. **CLI commands work**:
```bash
# Load command (will fail without triplestore, but should fail gracefully)
python -m pbprdf.cli load data/rdf/nba_game.ttl --graph http://stellman-greene.com/pbprdf/test

# Query command (will fail without triplestore, but should fail gracefully)
python -m pbprdf.cli query "SELECT (COUNT(*) AS ?c) WHERE { ?s ?p ?o }"
```

## What NOT to Do

- Do NOT implement the AI query interface yet (Phase 3).
- Do NOT implement boxscore stat entities yet — only if trivially easy alongside venue/officials.
- Do NOT break existing Phase 1 tests.
- Do NOT add vendor-specific triplestore libraries — use only SPARQLWrapper and httpx.
- Do NOT hardcode triplestore URLs — always read from environment/config.
- Do NOT make integration tests fail when no triplestore is running — they should skip.

## Expected Deliverables

After this phase, the following should work:

```bash
# All existing + new tests pass (without a triplestore running)
pytest tests/ -v

# Map a game — now includes win probability + venue/officials
python -m pbprdf.cli map tests/fixtures/nba_401810770.json --output data/rdf/nba_game.ttl

# Generate and load ontology (requires a running triplestore)
python -m pbprdf.cli ontology --output data/rdf/ontology.ttl
python -m pbprdf.cli load data/rdf/ontology.ttl

# Load game data
python -m pbprdf.cli load data/rdf/nba_game.ttl --graph http://stellman-greene.com/pbprdf/nba

# Query the triplestore
python -m pbprdf.cli query "SELECT (COUNT(*) AS ?triples) WHERE { ?s ?p ?o }"

# Fetch all games from a date
python -m pbprdf.cli fetch --league nba --date 2026-03-07

# Integration tests (only if PBPRDF_SPARQL_ENDPOINT is set)
PBPRDF_SPARQL_ENDPOINT=http://localhost:3030/pbprdf/sparql pytest tests/test_integration.py -v
```
