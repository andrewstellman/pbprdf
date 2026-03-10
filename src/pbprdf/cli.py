from __future__ import annotations

from pathlib import Path

import typer
from rdflib import Graph

from pbprdf.fetcher import fetch_date, fetch_season, fetch_summary
from pbprdf.mapper.core import map_game
from pbprdf.ontology import generate_ontology
from pbprdf.query.endpoint import get_endpoint, load_turtle, query as run_query, query_ask

app = typer.Typer(no_args_is_help=True)


@app.command()
def fetch(
    league: str = typer.Option(...),
    event: str | None = typer.Option(None),
    date: str | None = typer.Option(None),
    season: int | None = typer.Option(None),
    season_type: int = typer.Option(2),
) -> None:
    """Download summary JSON to data/raw."""
    provided = sum([1 if event else 0, 1 if date else 0, 1 if season is not None else 0])
    if provided != 1:
        raise typer.BadParameter("Provide exactly one of --event, --date, or --season")

    if event:
        output = fetch_summary(league=league, event_id=event)
        typer.echo(f"Saved {output}")
        return
    if date:
        paths = fetch_date(league=league, date_value=date)
        typer.echo(f"Fetched {len(paths)} game(s) for {league} on {date}")
        for path in paths:
            typer.echo(f"- {path}")
        return
    assert season is not None
    paths = fetch_season(league=league, season=season, season_type=season_type)
    typer.echo(f"Fetched {len(paths)} game(s) for {league} season={season} season_type={season_type}")


@app.command()
def ontology(output: Path = typer.Option(...)) -> None:
    """Generate ontology TBox Turtle."""
    output.parent.mkdir(parents=True, exist_ok=True)
    graph = generate_ontology()
    graph.serialize(destination=str(output), format="turtle")
    typer.echo(f"Wrote ontology {output}")


@app.command()
def map(input: Path = typer.Argument(...), output: Path = typer.Option(...)) -> None:
    """Map one JSON file or a directory of JSON files to Turtle."""
    output.parent.mkdir(parents=True, exist_ok=True)
    graph = Graph()
    if input.is_file():
        graph += map_game(input)
    elif input.is_dir():
        for json_file in sorted(input.glob("*.json")):
            graph += map_game(json_file)
    else:
        raise typer.BadParameter(f"Input path not found: {input}")

    graph.serialize(destination=str(output), format="turtle")
    typer.echo(f"Wrote {len(graph)} triples to {output}")


@app.command()
def load(input: Path = typer.Argument(...), graph: str | None = typer.Option(None)) -> None:
    """Load Turtle file(s) into configured triplestore graph store endpoint."""
    typer.echo(f"Using triplestore endpoint: {get_endpoint()}")
    ttl_files: list[Path]
    if input.is_file():
        ttl_files = [input]
    elif input.is_dir():
        ttl_files = sorted(input.glob("*.ttl"))
    else:
        raise typer.BadParameter(f"Input path not found: {input}")

    for ttl_file in ttl_files:
        try:
            load_turtle(ttl_file, graph_uri=graph)
            typer.echo(f"OK: {ttl_file}")
        except Exception as exc:
            typer.echo(f"FAILED: {ttl_file} -> {exc}")


@app.command()
def query(sparql: str = typer.Argument(...)) -> None:
    """Run SPARQL against configured endpoint and print tabular output."""
    try:
        if sparql.strip().lower().startswith("ask"):
            answer = query_ask(sparql)
            typer.echo("true" if answer else "false")
            return

        bindings = run_query(sparql)
        if not bindings:
            typer.echo("(no results)")
            return

        columns = sorted({k for row in bindings for k in row.keys()})
        typer.echo("\t".join(columns))
        for row in bindings:
            values = [row.get(col, {}).get("value", "") for col in columns]
            typer.echo("\t".join(values))
    except Exception as exc:
        typer.echo(f"Query failed: {exc}")


if __name__ == "__main__":
    app()

