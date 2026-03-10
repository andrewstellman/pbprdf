from __future__ import annotations

import json
import time
from dataclasses import dataclass
from datetime import UTC, date, datetime, timedelta
from pathlib import Path
from typing import Any

import httpx
import typer
from pydantic import ValidationError

# Ensure local package imports work when running as:
# python scripts/capture_fixtures.py
PROJECT_ROOT = Path(__file__).resolve().parents[1]
SRC_PATH = PROJECT_ROOT / "src"
import sys

if str(SRC_PATH) not in sys.path:
    sys.path.insert(0, str(SRC_PATH))

from pbprdf.models.espn import SummaryResponse  # noqa: E402


USER_AGENT = "pbprdf-v2-fixture-capture/0.1 (+https://github.com/andrewstellman/pbprdf)"
REQUEST_DELAY_SECONDS = 1.2
TIMEOUT_SECONDS = 30.0

SCOREBOARD_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/{slug}/scoreboard"
SUMMARY_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/{slug}/summary"


@dataclass(frozen=True)
class LeagueConfig:
    key: str
    slug: str
    seed_dates: list[date]
    max_lookback_days: int


@dataclass
class CaptureResult:
    league_key: str
    slug: str
    game_id: str
    fixture_path: Path
    game_label: str
    game_date: str | None
    top_level_keys: list[str]
    format_regulation: dict[str, Any] | None
    play_count: int
    plays_with_coordinates: int
    plays_with_participants: int
    unique_play_types: list[str]
    unique_participant_types: list[str]
    sample_scoring_play: dict[str, Any] | None
    validation_ok: bool
    validation_errors: str | None
    top_level_extra_fields: list[str]
    play_level_extra_fields: list[str]


LEAGUES: list[LeagueConfig] = [
    LeagueConfig(
        key="nba",
        slug="nba",
        seed_dates=[date(2026, 3, 7)],
        max_lookback_days=14,
    ),
    LeagueConfig(
        key="wnba",
        slug="wnba",
        seed_dates=[date(2025, 10, 10), date(2025, 9, 20)],
        max_lookback_days=45,
    ),
    LeagueConfig(
        key="ncaam",
        slug="mens-college-basketball",
        seed_dates=[date(2026, 3, 8), date(2026, 3, 7)],
        max_lookback_days=14,
    ),
    LeagueConfig(
        key="ncaaw",
        slug="womens-college-basketball",
        seed_dates=[date(2026, 3, 8), date(2026, 3, 7)],
        max_lookback_days=14,
    ),
]


def _fmt_yyyymmdd(d: date) -> str:
    return d.strftime("%Y%m%d")


def _iter_dates_backward(seed: date, max_days: int):
    for offset in range(max_days + 1):
        yield seed - timedelta(days=offset)


def _sleep():
    time.sleep(REQUEST_DELAY_SECONDS)


def _request_json(client: httpx.Client, url: str, params: dict[str, Any]) -> dict[str, Any]:
    response = client.get(url, params=params)
    response.raise_for_status()
    data = response.json()
    _sleep()
    return data


def _extract_completed_game_id(scoreboard: dict[str, Any]) -> str | None:
    for event in scoreboard.get("events", []):
        event_id = event.get("id")
        competitions = event.get("competitions", [])
        comp0 = competitions[0] if competitions else {}
        completed = (
            comp0.get("status", {}).get("type", {}).get("completed")
            if isinstance(comp0, dict)
            else None
        )
        if completed is None:
            completed = event.get("status", {}).get("type", {}).get("completed")
        if completed and event_id:
            return str(event_id)
    return None


def _discover_completed_game_id(client: httpx.Client, league: LeagueConfig) -> tuple[str, date]:
    for seed in league.seed_dates:
        for probe_date in _iter_dates_backward(seed, league.max_lookback_days):
            scoreboard = _request_json(
                client,
                SCOREBOARD_URL.format(slug=league.slug),
                {"dates": _fmt_yyyymmdd(probe_date)},
            )
            game_id = _extract_completed_game_id(scoreboard)
            if game_id:
                return game_id, probe_date

    raise RuntimeError(
        f"Could not find a completed game for {league.key} after searching "
        f"{len(league.seed_dates)} seed date(s) with lookback {league.max_lookback_days} days."
    )


def _extract_game_label(summary_data: dict[str, Any]) -> tuple[str, str | None]:
    header = summary_data.get("header", {})
    competitions = header.get("competitions", []) if isinstance(header, dict) else []
    comp0 = competitions[0] if competitions else {}
    competitors = comp0.get("competitors", []) if isinstance(comp0, dict) else []

    away_name = "Unknown Away"
    home_name = "Unknown Home"

    for comp in competitors:
        home_away = comp.get("homeAway")
        team = comp.get("team", {})
        name = team.get("displayName") or team.get("name") or "Unknown Team"
        if home_away == "away":
            away_name = name
        elif home_away == "home":
            home_name = name

    game_date = comp0.get("date") if isinstance(comp0, dict) else None
    return f"{away_name} at {home_name}", game_date


def _collect_league_summary(
    league: LeagueConfig,
    game_id: str,
    fixture_path: Path,
    summary_data: dict[str, Any],
) -> CaptureResult:
    top_level_keys = sorted(summary_data.keys())
    plays = summary_data.get("plays", [])
    plays = plays if isinstance(plays, list) else []

    plays_with_coordinates = sum(
        1 for play in plays if isinstance(play.get("coordinate"), dict)
    )
    plays_with_participants = sum(
        1
        for play in plays
        if isinstance(play.get("participants"), list) and len(play.get("participants")) > 0
    )

    unique_play_types = sorted(
        {
            str(play.get("type", {}).get("text"))
            for play in plays
            if isinstance(play.get("type"), dict) and play.get("type", {}).get("text") is not None
        }
    )

    unique_participant_types = sorted(
        {
            str(participant.get("type"))
            for play in plays
            if isinstance(play.get("participants"), list)
            for participant in play.get("participants", [])
            if isinstance(participant, dict) and participant.get("type") is not None
        }
    )

    sample_scoring_play = next(
        (play for play in plays if isinstance(play, dict) and play.get("scoringPlay")),
        plays[0] if plays else None,
    )

    game_label, game_date = _extract_game_label(summary_data)

    format_regulation = None
    fmt = summary_data.get("format")
    if isinstance(fmt, dict) and isinstance(fmt.get("regulation"), dict):
        format_regulation = fmt["regulation"]

    validation_ok = True
    validation_errors = None
    top_level_extra_fields: list[str] = []
    play_level_extra_fields: list[str] = []

    try:
        parsed = SummaryResponse.model_validate(summary_data)
        top_level_extra_fields = sorted((parsed.model_extra or {}).keys())
        play_extra = set()
        for play_model in parsed.plays:
            play_extra.update((play_model.model_extra or {}).keys())
        play_level_extra_fields = sorted(play_extra)
    except ValidationError as exc:
        validation_ok = False
        validation_errors = str(exc)

    return CaptureResult(
        league_key=league.key,
        slug=league.slug,
        game_id=game_id,
        fixture_path=fixture_path,
        game_label=game_label,
        game_date=game_date,
        top_level_keys=top_level_keys,
        format_regulation=format_regulation,
        play_count=len(plays),
        plays_with_coordinates=plays_with_coordinates,
        plays_with_participants=plays_with_participants,
        unique_play_types=unique_play_types,
        unique_participant_types=unique_participant_types,
        sample_scoring_play=sample_scoring_play if isinstance(sample_scoring_play, dict) else None,
        validation_ok=validation_ok,
        validation_errors=validation_errors,
        top_level_extra_fields=top_level_extra_fields,
        play_level_extra_fields=play_level_extra_fields,
    )


def _render_report(results: list[CaptureResult]) -> str:
    lines: list[str] = []
    lines.append("# Schema Report")
    lines.append("")
    lines.append(f"Generated: {datetime.now(UTC).isoformat()}")
    lines.append("")

    all_key_sets = [set(result.top_level_keys) for result in results]
    common_keys = sorted(set.intersection(*all_key_sets)) if all_key_sets else []
    any_keys = sorted(set.union(*all_key_sets)) if all_key_sets else []
    some_not_all = sorted(set(any_keys) - set(common_keys))

    lines.append("## Per-League Capture Summary")
    lines.append("")
    for result in results:
        lines.append(f"### {result.league_key.upper()} (`{result.slug}`)")
        lines.append("")
        lines.append(f"- Fixture: `{result.fixture_path}`")
        lines.append(f"- Game ID: `{result.game_id}`")
        lines.append(f"- Game: {result.game_label}")
        lines.append(f"- Date: `{result.game_date}`")
        lines.append(f"- Sections present: {', '.join(result.top_level_keys)}")
        lines.append(f"- Play count: `{result.play_count}`")
        lines.append(f"- Plays with coordinates: `{result.plays_with_coordinates}`")
        lines.append(f"- Plays with participants: `{result.plays_with_participants}`")
        lines.append(
            f"- format.regulation: `{json.dumps(result.format_regulation, sort_keys=True) if result.format_regulation else 'missing'}`"
        )
        lines.append(
            f"- Unique `play.type.text` values ({len(result.unique_play_types)}): "
            + ", ".join(f"`{v}`" for v in result.unique_play_types[:50])
            + (" ..." if len(result.unique_play_types) > 50 else "")
        )
        lines.append(
            f"- Unique `participant.type` values ({len(result.unique_participant_types)}): "
            + (", ".join(f"`{v}`" for v in result.unique_participant_types) if result.unique_participant_types else "_none_")
        )
        lines.append(
            f"- Validation: {'PASS' if result.validation_ok else 'FAIL'}"
        )
        if result.top_level_extra_fields:
            lines.append(
                "- Top-level fields not modeled explicitly (captured via `extra=allow`): "
                + ", ".join(f"`{k}`" for k in result.top_level_extra_fields)
            )
        if result.play_level_extra_fields:
            lines.append(
                "- Play-level fields not modeled explicitly (captured via `extra=allow`): "
                + ", ".join(f"`{k}`" for k in result.play_level_extra_fields)
            )
        if not result.validation_ok and result.validation_errors:
            lines.append("")
            lines.append("#### Validation Errors")
            lines.append("")
            lines.append("```text")
            lines.append(result.validation_errors)
            lines.append("```")
        lines.append("")
        lines.append("#### Sample Play (First Scoring Play)")
        lines.append("")
        lines.append("```json")
        lines.append(json.dumps(result.sample_scoring_play or {}, indent=2, sort_keys=True))
        lines.append("```")
        lines.append("")

    lines.append("## Cross-League Comparison")
    lines.append("")
    lines.append(
        "- Top-level fields present in all four leagues: "
        + (", ".join(f"`{k}`" for k in common_keys) if common_keys else "_none_")
    )
    lines.append(
        "- Top-level fields present in some but not all leagues: "
        + (", ".join(f"`{k}`" for k in some_not_all) if some_not_all else "_none_")
    )

    all_top_level_extra = sorted(
        {key for result in results for key in result.top_level_extra_fields}
    )
    all_play_level_extra = sorted(
        {key for result in results for key in result.play_level_extra_fields}
    )
    lines.append(
        "- Fields present in JSON but not explicitly in Pydantic top-level model: "
        + (", ".join(f"`{k}`" for k in all_top_level_extra) if all_top_level_extra else "_none_")
    )
    lines.append(
        "- Fields present in JSON but not explicitly in Pydantic `Play` model: "
        + (", ".join(f"`{k}`" for k in all_play_level_extra) if all_play_level_extra else "_none_")
    )
    lines.append("")

    return "\n".join(lines)


def _print_console_summary(result: CaptureResult) -> None:
    typer.echo(f"[{result.league_key}] Captured game {result.game_id} -> {result.fixture_path.name}")
    typer.echo(f"  Game: {result.game_label} ({result.game_date})")
    typer.echo(f"  Sections: {', '.join(result.top_level_keys)}")
    typer.echo(
        f"  Plays: {result.play_count} | Coordinates: {result.plays_with_coordinates} | Participants: {result.plays_with_participants}"
    )
    typer.echo(
        "  format.regulation: "
        + (json.dumps(result.format_regulation, sort_keys=True) if result.format_regulation else "missing")
    )
    typer.echo(f"  Validation: {'PASS' if result.validation_ok else 'FAIL'}")
    if not result.validation_ok:
        typer.echo("  Validation errors captured in SCHEMA_REPORT.md")
    typer.echo("")


def main() -> None:
    fixtures_dir = PROJECT_ROOT / "tests" / "fixtures"
    raw_cache_dir = PROJECT_ROOT / "data" / "raw"
    fixtures_dir.mkdir(parents=True, exist_ok=True)
    raw_cache_dir.mkdir(parents=True, exist_ok=True)

    results: list[CaptureResult] = []
    headers = {"User-Agent": USER_AGENT}

    typer.echo("Capturing one completed game per league...")
    typer.echo("")
    with httpx.Client(timeout=TIMEOUT_SECONDS, headers=headers, trust_env=False) as client:
        for league in LEAGUES:
            typer.echo(f"Discovering completed game for {league.key} ({league.slug})...")
            game_id, discovered_from_date = _discover_completed_game_id(client, league)
            typer.echo(f"  Found game id {game_id} using scoreboard date {discovered_from_date}")

            summary = _request_json(
                client,
                SUMMARY_URL.format(slug=league.slug),
                {"event": game_id},
            )

            fixture_path = fixtures_dir / f"{league.key}_{game_id}.json"
            fixture_path.write_text(json.dumps(summary, indent=2, sort_keys=True) + "\n")

            # Raw cache copy (same payload, named by slug)
            cache_path = raw_cache_dir / f"{league.slug}_{game_id}.json"
            cache_path.write_text(json.dumps(summary, indent=2, sort_keys=True) + "\n")

            result = _collect_league_summary(league, game_id, fixture_path, summary)
            results.append(result)
            _print_console_summary(result)

    report = _render_report(results)
    report_path = fixtures_dir / "SCHEMA_REPORT.md"
    report_path.write_text(report + "\n")
    typer.echo(f"Schema report written: {report_path}")


if __name__ == "__main__":
    main()
