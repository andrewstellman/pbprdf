# pbprdf

Generate RDF triples from ESPN basketball play-by-play data. Fetches game summaries from ESPN's JSON API, maps every play, event, roster entry, venue, official, and win-probability data point into an [RDF](https://www.w3.org/RDF/) graph using a purpose-built [OWL ontology](data/rdf/ontology.ttl), and serializes the result as [Turtle](https://www.w3.org/TR/turtle/).

Supports NBA, WNBA, men's college basketball, and women's college basketball.

## Background

This is a Python rewrite of the [original Scala version](https://github.com/andrewstellman/pbprdf/tree/v1-final) that has been in development since 2014. The Python version reads from ESPN's JSON summary API (the Scala version scraped HTML play-by-play pages) and produces a superset of the original ontology — adding win probability, officials, venue details, and shot coordinates.

Here's an article that gives more detail about the original project: [NBA analytics and RDF graphs: Game, data, and metadata evolution, and Occam's razor](https://www.zdnet.com/article/nba-analytics-and-rdf-graphs-game-data-and-metadata-evolution-and-occams-razor/)

Here's an example of an analysis you can do with pbprdf: [Analysis: 3-point shot percentage after other team makes or misses](https://gist.github.com/andrewstellman/4872dbb9dc7593e56abddbe8b998b509)

## Quick start

Requires Python 3.10+.

```bash
git clone https://github.com/andrewstellman/pbprdf.git
cd pbprdf
python -m venv .venv
source .venv/bin/activate
pip install -e .
```

### Fetch and convert a game

```bash
# Fetch an NBA game and write Turtle to stdout
pbprdf fetch 401810770

# Fetch a WNBA game and write to a file
pbprdf fetch 401820329 --league wnba -o wnba_game.ttl

# Generate just the ontology
pbprdf ontology -o ontology.ttl
```

### Load into a triplestore and query

```bash
# Load a Turtle file into a SPARQL endpoint
pbprdf load game.ttl --endpoint http://localhost:8080/rdf4j-server --repo pbprdf-db

# Run a SPARQL query
pbprdf query --endpoint http://localhost:8080/rdf4j-server --repo pbprdf-db \
  "SELECT ?player ?points WHERE { ?player pbprdf:points ?points } ORDER BY DESC(?points) LIMIT 10"
```

### Run the tests

```bash
pip install pytest
pytest tests/ -v
```

## Project structure

```
src/pbprdf/
  cli.py              # Typer CLI (fetch, ontology, load, query)
  config.py            # League configs and ESPN URL patterns
  fetcher.py           # HTTP client for ESPN JSON API
  ontology.py          # OWL ontology generation (24 classes, 122+ properties)
  models/espn.py       # Pydantic v2 models for ESPN API responses
  mapper/
    core.py            # Orchestration: JSON → RDF graph
    game.py            # Game-level triples (teams, scores, status)
    roster.py          # Team rosters and player entities
    venue.py           # Venue, attendance, officials
    plays.py           # Play-by-play type classification and mapping
    events.py          # Non-play events (period starts/ends)
    winprob.py         # Win probability timeline
    ids.py             # Deterministic IRI generation
  query/endpoint.py    # SPARQLWrapper-based query layer

specs/                 # Intent specifications (V1 spec, V2 architecture, ontology delta)
tests/                 # pytest suite (models, mappers, ontology, integration, query)
data/raw/              # Cached ESPN JSON fixtures
data/rdf/              # Reference Turtle files and generated ontology
```

## Ontology

The pbprdf ontology defines 24 OWL classes and 122+ properties covering games, plays, players, teams, rosters, venues, officials, and win probability. Key design decisions:

- Every play is typed to a specific OWL class (Shot, Rebound, Foul, Turnover, Block, JumpBall, Timeout, Ejection, etc.)
- The `involvedPlayer` superproperty is explicitly materialized on every play for SPARQL convenience
- Shot coordinates use ESPN's normalized 0–100 coordinate system with sentinel value detection (x=0, y=0 means missing)
- Win probability entries are linked to their corresponding plays via play IDs

See [data/rdf/ontology.ttl](data/rdf/ontology.ttl) for the full ontology in Turtle format.

## V1 (Scala)

The original Scala/sbt version is preserved at tag [`v1-final`](https://github.com/andrewstellman/pbprdf/tree/v1-final). It reads from ESPN's HTML play-by-play pages (which are no longer reliably available) and generates a compatible but smaller ontology. The Python version is a full rewrite with no shared code.

## License

MIT — see [LICENSE](LICENSE).
