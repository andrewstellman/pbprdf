from __future__ import annotations

import json
import time
from datetime import date, datetime, timedelta
from pathlib import Path

import httpx

from pbprdf.config import DATA_RAW_DIR, LEAGUE_SLUGS, SCOREBOARD_URL, SUMMARY_URL, USER_AGENT


def _client() -> httpx.Client:
    return httpx.Client(timeout=30.0, headers={"User-Agent": USER_AGENT}, trust_env=False)


def _scoreboard_completed_event_ids(client: httpx.Client, slug: str, yyyymmdd: str) -> list[str]:
    resp = client.get(SCOREBOARD_URL.format(slug=slug), params={"dates": yyyymmdd})
    resp.raise_for_status()
    payload = resp.json()
    ids: list[str] = []
    for event in payload.get("events", []):
        eid = event.get("id")
        completed = event.get("status", {}).get("type", {}).get("completed")
        if completed and eid:
            ids.append(str(eid))
    return ids


def _date_range(start: date, end: date) -> list[date]:
    days = (end - start).days
    return [start + timedelta(days=i) for i in range(days + 1)]


def fetch_summary(league: str, event_id: str, output_path: Path | None = None, force: bool = False) -> Path:
    if league not in LEAGUE_SLUGS:
        raise ValueError(f"Unsupported league '{league}'. Expected one of: {sorted(LEAGUE_SLUGS.keys())}")

    slug = LEAGUE_SLUGS[league]
    url = SUMMARY_URL.format(slug=slug)
    params = {"event": event_id}

    with _client() as client:
        response = client.get(url, params=params)
        response.raise_for_status()
        payload = response.json()

    if output_path is None:
        DATA_RAW_DIR.mkdir(parents=True, exist_ok=True)
        output_path = DATA_RAW_DIR / f"{league}_{event_id}.json"
    else:
        output_path.parent.mkdir(parents=True, exist_ok=True)

    if output_path.exists() and not force:
        return output_path
    output_path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n")
    return output_path


def fetch_date(league: str, date_value: str, force: bool = False, delay_seconds: float = 1.0) -> list[Path]:
    if league not in LEAGUE_SLUGS:
        raise ValueError(f"Unsupported league '{league}'. Expected one of: {sorted(LEAGUE_SLUGS.keys())}")
    slug = LEAGUE_SLUGS[league]
    yyyymmdd = date_value.replace("-", "")
    DATA_RAW_DIR.mkdir(parents=True, exist_ok=True)

    paths: list[Path] = []
    with _client() as client:
        event_ids = _scoreboard_completed_event_ids(client, slug, yyyymmdd)
        for idx, event_id in enumerate(event_ids):
            out = DATA_RAW_DIR / f"{league}_{event_id}.json"
            if out.exists() and not force:
                paths.append(out)
                continue
            response = client.get(SUMMARY_URL.format(slug=slug), params={"event": event_id})
            response.raise_for_status()
            out.write_text(json.dumps(response.json(), indent=2, sort_keys=True) + "\n")
            paths.append(out)
            if idx + 1 < len(event_ids):
                time.sleep(delay_seconds)
    return paths


def fetch_season(
    league: str,
    season: int,
    season_type: int = 2,
    force: bool = False,
    delay_seconds: float = 1.0,
) -> list[Path]:
    # season_type currently affects only expected date windows.
    if league == "nba":
        start, end = date(season - 1, 10, 20), date(season, 4, 15)
    elif league == "wnba":
        start, end = date(season, 5, 15), date(season, 9, 15)
    else:
        start, end = date(season - 1, 11, 1), date(season, 4, 10)
    if season_type == 3:
        # Playoffs: narrower spring/fall windows.
        if league == "nba":
            start, end = date(season, 4, 15), date(season, 6, 30)
        elif league == "wnba":
            start, end = date(season, 9, 1), date(season, 10, 31)
        else:
            start, end = date(season, 3, 1), date(season, 4, 15)

    all_paths: list[Path] = []
    for d in _date_range(start, end):
        date_str = d.strftime("%Y-%m-%d")
        paths = fetch_date(league=league, date_value=date_str, force=force, delay_seconds=delay_seconds)
        all_paths.extend(paths)
        time.sleep(delay_seconds)
    return all_paths

